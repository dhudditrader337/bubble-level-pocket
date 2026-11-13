package com.bubblelevel.pocket.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(measurements: MeasurementRepository, settings: SettingsRepository, calibrations: CalibrationRepository, sampleAngle: Double) : ViewModel() {
    private val _state = MutableStateFlow(MenuUiState(sampleAngle = sampleAngle))
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            combine(measurements.history, settings.settings, calibrations.calibrations) { history, prefs, zero ->
                MenuUiState(!history.loaded, history.entries.size, history.corrupt, prefs.unit, zero.entries[CalibrationSlot.SURFACE], CalibrationSlot.SURFACE in zero.invalidSlots, sampleAngle)
            }.collect { value -> _state.update { value } }
        }
    }
}
