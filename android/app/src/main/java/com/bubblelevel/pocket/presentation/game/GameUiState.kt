package com.bubblelevel.pocket.presentation.game

import com.bubblelevel.pocket.domain.model.*

data class GameUiState(
    val mode: InstrumentMode = InstrumentMode.SURFACE,
    val edge: EdgeReference = EdgeReference.SHORT,
    val unit: AngleUnit = AngleUnit.DEGREES,
    val reading: LiveReading? = null,
    val failure: CaptureFailure? = CaptureFailure.WAITING,
    val active: Boolean = false,
    val sound: Boolean = false,
    val recalibrate: Boolean = false
)
