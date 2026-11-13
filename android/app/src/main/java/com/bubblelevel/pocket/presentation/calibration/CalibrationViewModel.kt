package com.bubblelevel.pocket.presentation.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.domain.measurement.*
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.*
import com.bubblelevel.pocket.presentation.app.AppEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class CalibrationViewModel(
    private val sensors: SensorRepository,
    private val calibrations: CalibrationRepository,
    private val settings: SettingsRepository,
    private val epoch: () -> Long
) : ViewModel() {
    private val _state = MutableStateFlow(CalibrationUiState())
    val state = _state.asStateFlow()
    private val channel = Channel<AppEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private val engine = MeasurementEngine()
    private var sampling: Job? = null
    private var timeout: Job? = null
    private var freshness: Job? = null
    private var lastPublish = 0L
    private var lastObservation = 0L
    private var freshWindow = false
    private var visit: String? = null

    fun open(id: String, mode: InstrumentMode, edge: EdgeReference) {
        if (visit == id) { resume(); return }
        visit = id
        _state.update { CalibrationUiState(mode = mode, edge = edge, previous = calibrations.calibrations.value.entries[calibrationSlot(mode, edge)]) }
        resume()
    }
    fun resume() {
        if (sampling?.isActive == true || _state.value.phase == CalibrationPhase.SAVING || _state.value.phase == CalibrationPhase.COMPLETE) return
        engine.reset()
        freshWindow = false
        lastObservation = 0L
        _state.update { it.copy(phase = CalibrationPhase.ACQUIRING, reading = null) }
        sampling = viewModelScope.launch {
            sensors.observations().collect { observation ->
                val s = _state.value
                lastObservation = observation.timestamp
                val evaluation = engine.evaluate(observation, sensors.now(), s.mode, s.edge, null, calibrating = true)
                if (observation.unavailable) {
                    pause()
                    _state.update { it.copy(phase = CalibrationPhase.ERROR, error = CaptureFailure.UNAVAILABLE) }
                    return@collect
                }
                if (s.phase == CalibrationPhase.CAPTURING && evaluation.failure != null && evaluation.failure != CaptureFailure.MOVING) {
                    freshWindow = false
                    _state.update { it.copy(phase = CalibrationPhase.ACQUIRING, error = evaluation.failure) }
                }
                if (s.phase == CalibrationPhase.CAPTURING && evaluation.reading != null && freshWindow && evaluation.reading.windowMs >= 1500L) {
                    if (evaluation.calibrationReady) {
                        val record = CalibrationEngine.solve(engine.references(), s.mode, s.edge, epoch(), observation.source)
                        if (record != null) { persist(record); return@collect }
                    }
                    freshWindow = false
                    engine.reset()
                    _state.update { it.copy(phase = CalibrationPhase.ACQUIRING, error = CaptureFailure.MOVING) }
                }
                val now = sensors.now()
                if (now - lastPublish >= 100L) {
                    lastPublish = now
                    _state.update {
                        it.copy(reading = evaluation.reading ?: it.reading,
                            phase = if (it.phase == CalibrationPhase.CAPTURING) it.phase else if (evaluation.calibrationReady) CalibrationPhase.READY else CalibrationPhase.ACQUIRING,
                            error = evaluation.failure?.takeIf { failure -> failure != CaptureFailure.MOVING })
                    }
                }
            }
        }
        timeout = viewModelScope.launch {
            delay(1500L)
            if (lastObservation == 0L || _state.value.reading == null) {
                pause()
                _state.update { it.copy(phase = CalibrationPhase.ERROR, error = it.error ?: CaptureFailure.UNAVAILABLE) }
            }
        }
        freshness = viewModelScope.launch {
            while (isActive) {
                delay(100L)
                if (lastObservation != 0L && sensors.now() - lastObservation > 300L) {
                    engine.reset()
                    freshWindow = false
                    _state.update { it.copy(phase = CalibrationPhase.ACQUIRING, error = CaptureFailure.STALE) }
                }
            }
        }
    }
    fun pause() {
        sampling?.cancel()
        timeout?.cancel()
        freshness?.cancel()
        sampling = null
        if (_state.value.phase == CalibrationPhase.CAPTURING || _state.value.phase == CalibrationPhase.READY) _state.update { it.copy(phase = CalibrationPhase.ACQUIRING) }
    }
    fun select(mode: InstrumentMode, edge: EdgeReference) {
        if (_state.value.phase == CalibrationPhase.SAVING) return
        pause()
        _state.update { CalibrationUiState(mode = mode, edge = edge, previous = calibrations.calibrations.value.entries[calibrationSlot(mode, edge)]) }
        resume()
    }
    fun zero() {
        if (_state.value.phase != CalibrationPhase.READY) return
        if (sensors.now() - lastObservation > 300L) {
            engine.reset()
            _state.update { it.copy(phase = CalibrationPhase.ACQUIRING, error = CaptureFailure.STALE) }
            return
        }
        engine.reset()
        freshWindow = true
        _state.update { it.copy(phase = CalibrationPhase.CAPTURING, error = null) }
    }
    private fun persist(value: Calibration) {
        _state.update { it.copy(phase = CalibrationPhase.SAVING) }
        pause()
        viewModelScope.launch {
            val success = calibrations.put(value)
            _state.update { it.copy(phase = if (success) CalibrationPhase.COMPLETE else CalibrationPhase.ERROR, previous = if (success) value else it.previous, error = if (success) null else CaptureFailure.STORAGE) }
            if (success && settings.settings.value.haptics) channel.send(AppEvent.Haptic)
        }
    }
    fun resetDialog(value: Boolean) { _state.update { it.copy(resetDialog = value) } }
    fun reset() {
        val slot = calibrationSlot(_state.value.mode, _state.value.edge)
        _state.update { it.copy(phase = CalibrationPhase.SAVING, resetDialog = false) }
        pause()
        viewModelScope.launch {
            val success = calibrations.reset(slot)
            _state.update { it.copy(phase = if (success) CalibrationPhase.COMPLETE else CalibrationPhase.ERROR, previous = if (success) null else it.previous, error = if (success) null else CaptureFailure.STORAGE) }
        }
    }
    fun retry() { pause(); _state.update { it.copy(phase = CalibrationPhase.ACQUIRING, error = null) }; resume() }
    override fun onCleared() { pause() }
}
