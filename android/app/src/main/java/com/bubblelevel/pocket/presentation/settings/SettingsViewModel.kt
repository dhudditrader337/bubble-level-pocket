package com.bubblelevel.pocket.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsRepository, calibrations: CalibrationRepository, measurements: MeasurementRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()
    private var pending: AppSettings? = null
    init {
        viewModelScope.launch { measurements.history.collect { history -> _state.update { it.copy(loading = !history.loaded) } } }
        viewModelScope.launch { settings.settings.collect { value -> _state.update { it.copy(settings = value) } } }
        viewModelScope.launch { calibrations.calibrations.collect { value -> _state.update { it.copy(calibrations = value) } } }
    }
    fun unit(value: AngleUnit) = save(_state.value.settings.copy(unit = value))
    fun sound(value: Boolean) = save(_state.value.settings.copy(sound = value))
    fun haptics(value: Boolean) = save(_state.value.settings.copy(haptics = value))
    fun retry() { pending?.let(::save) }
    private fun save(value: AppSettings) {
        if (_state.value.busy) return
        pending = value
        _state.update { it.copy(busy = true, error = false) }
        viewModelScope.launch {
            val success = settings.set(value)
            _state.update { it.copy(busy = false, error = !success) }
            if (success) pending = null
        }
    }
}
