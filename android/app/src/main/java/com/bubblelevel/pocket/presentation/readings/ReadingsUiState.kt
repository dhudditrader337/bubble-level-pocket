package com.bubblelevel.pocket.presentation.readings

import com.bubblelevel.pocket.domain.model.*

data class ReadingsUiState(
    val loading: Boolean = true,
    val entries: List<Measurement> = emptyList(),
    val filter: InstrumentMode? = null,
    val unit: AngleUnit = AngleUnit.DEGREES,
    val corrupt: Boolean = false,
    val readOnly: Boolean = false,
    val pendingDelete: Set<String>? = null,
    val recoverDialog: Boolean = false,
    val busy: Boolean = false,
    val error: Boolean = false
)
