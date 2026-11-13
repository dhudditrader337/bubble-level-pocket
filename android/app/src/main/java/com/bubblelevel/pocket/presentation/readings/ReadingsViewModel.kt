package com.bubblelevel.pocket.presentation.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReadingsViewModel(private val measurements: MeasurementRepository, settings: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(ReadingsUiState())
    val state = _state.asStateFlow()
    private var retryIds: Set<String>? = null
    private var retryRecovery = false
    init {
        viewModelScope.launch {
            measurements.history.collect { history -> _state.update { it.copy(loading = !history.loaded, entries = history.entries, corrupt = history.corrupt, readOnly = history.readOnly) } }
        }
        viewModelScope.launch { settings.settings.collect { prefs -> _state.update { it.copy(unit = prefs.unit) } } }
    }
    fun filter(value: InstrumentMode?) { _state.update { it.copy(filter = value) } }
    fun confirmDelete(ids: Set<String>?) { if (!_state.value.busy) _state.update { it.copy(pendingDelete = ids) } }
    fun recoverDialog(value: Boolean) { if (!_state.value.busy) _state.update { it.copy(recoverDialog = value) } }
    fun delete() {
        val ids = _state.value.pendingDelete ?: retryIds ?: return
        if (_state.value.busy) return
        retryIds = ids
        retryRecovery = false
        _state.update { it.copy(busy = true, pendingDelete = null, error = false) }
        viewModelScope.launch {
            val success = measurements.delete(ids)
            _state.update { it.copy(busy = false, error = !success) }
            if (success) retryIds = null
        }
    }
    fun recover() {
        if (_state.value.busy) return
        retryRecovery = true
        _state.update { it.copy(busy = true, recoverDialog = false, error = false) }
        viewModelScope.launch {
            val success = measurements.recover()
            _state.update { it.copy(busy = false, error = !success) }
            if (success) retryRecovery = false
        }
    }
    fun retry() {
        when {
            retryRecovery -> recover()
            retryIds != null -> delete()
            else -> viewModelScope.launch { measurements.load() }
        }
    }
}
