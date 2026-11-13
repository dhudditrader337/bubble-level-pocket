package com.bubblelevel.pocket.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.core.config.GameConfig
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.domain.feedback.FeedbackController
import com.bubblelevel.pocket.domain.measurement.MeasurementEngine
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.*
import com.bubblelevel.pocket.presentation.app.AppEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.UUID

class GameViewModel(
    private val sensors: SensorRepository,
    private val settings: SettingsRepository,
    private val calibrations: CalibrationRepository,
    private val feedback: FeedbackController,
    private val epoch: () -> Long
) : ViewModel() {
    private val _state = MutableStateFlow(GameUiState())
    val state = _state.asStateFlow()
    private val channel = Channel<AppEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private val engine = MeasurementEngine()
    private var sampling: Job? = null
    private var timeout: Job? = null
    private var freshness: Job? = null
    private var latest = Evaluation(failure = CaptureFailure.WAITING)
    private var lastPublish = 0L
    private var session = -1L
    private var ended = false
    private var lastTone = -2000L
    private var leftLevel = 0L
    private var wasLevel = false
    init {
        viewModelScope.launch { settings.settings.collect { prefs -> _state.update { it.copy(unit = prefs.unit, sound = prefs.sound) } } }
    }
    fun enter(id: Long, surface: Boolean) {
        if (session != id) {
            session = id
            if (surface) _state.update { it.copy(mode = InstrumentMode.SURFACE, edge = EdgeReference.SHORT) }
        }
        ended = false
        resume()
    }
    fun resume() {
        if (sampling?.isActive == true || ended) return
        engine.reset()
        latest = Evaluation(failure = CaptureFailure.WAITING)
        lastPublish = 0L
        wasLevel = false
        leftLevel = sensors.now()
        _state.update { it.copy(active = true, reading = null, failure = CaptureFailure.WAITING) }
        sampling = viewModelScope.launch {
            sensors.observations().collect { observation ->
                if (ended) return@collect
                val s = _state.value
                val slot = calibrationSlot(s.mode, s.edge)
                val zero = calibrations.calibrations.value.entries[slot]
                latest = engine.evaluate(observation, sensors.now(), s.mode, s.edge, zero)
                if (observation.unavailable) { finish(CaptureFailure.UNAVAILABLE); return@collect }
                val level = latest.reading?.level == true && latest.failure == null
                val now = sensors.now()
                if (!level && wasLevel) leftLevel = now
                if (level && !wasLevel && s.sound && now - lastTone >= 2000L && now - leftLevel >= 500L) {
                    feedback.levelTone()
                    lastTone = now
                }
                wasLevel = level
                if (now - lastPublish >= 100L) {
                    lastPublish = now
                    _state.update { it.copy(reading = latest.reading ?: it.reading, failure = latest.failure, recalibrate = slot in calibrations.calibrations.value.invalidSlots || (zero != null && zero.sensor != observation.source)) }
                }
            }
        }
        timeout = viewModelScope.launch {
            delay(GameConfig.ACQUISITION_TIMEOUT_MS)
            if (latest.reading == null && !ended) finish(latest.failure?.takeIf { it == CaptureFailure.FACE_DOWN || it == CaptureFailure.UPRIGHT } ?: CaptureFailure.UNAVAILABLE)
        }
        freshness = viewModelScope.launch {
            while (isActive) {
                delay(100L)
                val reading = latest.reading
                if (reading != null && sensors.now() - reading.timestamp > 300L) {
                    latest = Evaluation(failure = CaptureFailure.STALE)
                    engine.reset()
                    _state.update { it.copy(failure = CaptureFailure.STALE) }
                }
            }
        }
    }
    fun pause() {
        sampling?.cancel()
        timeout?.cancel()
        freshness?.cancel()
        sampling = null
        feedback.release()
        _state.update { it.copy(active = false) }
    }
    fun mode(value: InstrumentMode) { if (value != _state.value.mode) { pause(); _state.update { it.copy(mode = value) }; resume() } }
    fun edge(value: EdgeReference) { if (value != _state.value.edge) { pause(); _state.update { it.copy(edge = value) }; resume() } }
    fun unit(value: AngleUnit) {
        viewModelScope.launch {
            if (!settings.set(settings.settings.value.copy(unit = value))) _state.update { it.copy(failure = CaptureFailure.STORAGE) }
        }
    }
    fun lock() {
        if (ended) return
        val reading = latest.reading
        val failure = when {
            reading == null -> latest.failure ?: CaptureFailure.WAITING
            sensors.now() - reading.timestamp > 300L -> CaptureFailure.STALE
            latest.failure != null -> latest.failure
            !reading.stable -> CaptureFailure.MOVING
            else -> null
        }
        if (failure != null || reading == null) { finish(failure ?: CaptureFailure.MOVING); return }
        ended = true
        val s = _state.value
        val capture = Measurement(UUID.randomUUID().toString(), epoch(), MeasurementLabel.MEASUREMENT, s.mode, s.edge, reading, s.unit)
        pause()
        channel.trySend(AppEvent.Captured(capture))
    }
    private fun finish(failure: CaptureFailure) {
        if (ended) return
        ended = true
        pause()
        channel.trySend(AppEvent.Result(MeasurementResult(failure = failure)))
    }
    fun calibrate() { val s = _state.value; pause(); channel.trySend(AppEvent.Navigate(Screen.Calibration(s.mode, s.edge))) }
    override fun onCleared() { pause() }
}
