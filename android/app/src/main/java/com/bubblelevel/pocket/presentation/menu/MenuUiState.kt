package com.bubblelevel.pocket.presentation.menu

import com.bubblelevel.pocket.domain.model.*

data class MenuUiState(
    val loading: Boolean = true,
    val savedCount: Int = 0,
    val corrupt: Boolean = false,
    val unit: AngleUnit = AngleUnit.DEGREES,
    val calibration: Calibration? = null,
    val recalibrate: Boolean = false,
    val sampleAngle: Double = 0.0
)
