package com.bubblelevel.pocket.presentation.settings

import com.bubblelevel.pocket.domain.model.*

data class SettingsUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val calibrations: CalibrationState = CalibrationState(),
    val busy: Boolean = false,
    val error: Boolean = false
)
