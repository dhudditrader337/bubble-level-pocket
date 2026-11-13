package com.bubblelevel.pocket.presentation.anglelock

import com.bubblelevel.pocket.domain.model.*

data class AngleLockUiState(
    val measurement: Measurement? = null,
    val readOnly: Boolean = false,
    val saved: Boolean = false,
    val saving: Boolean = false,
    val labelPicker: Boolean = false,
    val deleteDialog: Boolean = false,
    val unit: AngleUnit = AngleUnit.DEGREES,
    val outcome: WriteOutcome? = null,
    val capacity: Boolean = false
)
