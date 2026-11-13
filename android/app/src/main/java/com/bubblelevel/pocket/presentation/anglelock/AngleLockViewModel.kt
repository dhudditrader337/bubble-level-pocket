package com.bubblelevel.pocket.presentation.anglelock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.*
import com.bubblelevel.pocket.domain.usecase.SaveMeasurementUseCase
import com.bubblelevel.pocket.presentation.app.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AngleLockViewModel(
    private val sessions: SessionStore,
    private val measurements: MeasurementRepository,
    private val settings: SettingsRepository,
    private val saveMeasurement: SaveMeasurementUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AngleLockUiState())
    val state = _state.asStateFlow()
    private val channel = Channel<AppEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var currentId: String? = null
    init {
        viewModelScope.launch { settings.settings.collect { prefs -> _state.update { it.copy(unit = prefs.unit) } } }
        viewModelScope.launch {
            measurements.history.collect { history ->
                val persisted = history.entries.find { it.id == currentId }
                _state.update { it.copy(measurement = persisted ?: it.measurement, saved = persisted != null, capacity = history.entries.size >= 100 || history.readOnly) }
            }
        }
    }
    fun open(id: String, readOnly: Boolean) {
        if (currentId == id && _state.value.readOnly == readOnly) return
        currentId = id
        val persisted = measurements.history.value.entries.find { it.id == id }
        val held = sessions.captures[id]
        _state.update { AngleLockUiState(persisted ?: held, readOnly, persisted != null, unit = settings.settings.value.unit, capacity = measurements.history.value.entries.size >= 100 || measurements.history.value.readOnly) }
    }
    fun picker(value: Boolean) { _state.update { it.copy(labelPicker = value) } }
    fun label(value: MeasurementLabel) {
        if (_state.value.saved || _state.value.saving) return
        _state.update { it.copy(measurement = it.measurement?.copy(label = value), labelPicker = false) }
        _state.value.measurement?.let { sessions.captures[it.id] = it }
    }
    fun save() {
        val s = _state.value
        val capture = s.measurement ?: return
        if (s.saving || s.saved || s.readOnly) return
        _state.update { it.copy(saving = true, outcome = null) }
        viewModelScope.launch {
            val outcome = saveMeasurement(capture)
            val persisted = measurements.history.value.entries.find { it.id == capture.id }
            _state.update { it.copy(saving = false, outcome = outcome, saved = outcome == WriteOutcome.SUCCESS, measurement = persisted ?: capture) }
            if (outcome == WriteOutcome.SUCCESS && persisted != null) {
                sessions.captures[capture.id] = persisted
                if (settings.settings.value.haptics) channel.send(AppEvent.Haptic)
                channel.send(AppEvent.Result(MeasurementResult(measurement = persisted)))
            }
        }
    }
    fun confirmDelete(value: Boolean) { if (!_state.value.saving) _state.update { it.copy(deleteDialog = value) } }
    fun delete() {
        val m = _state.value.measurement ?: return
        if (_state.value.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val success = measurements.delete(setOf(m.id))
            _state.update { it.copy(saving = false, deleteDialog = false, outcome = if (success) null else WriteOutcome.FAILED) }
            if (success) channel.send(AppEvent.Back)
        }
    }
}
