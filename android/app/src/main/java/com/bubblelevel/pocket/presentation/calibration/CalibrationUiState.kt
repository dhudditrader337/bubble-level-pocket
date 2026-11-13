package com.bubblelevel.pocket.presentation.calibration

import com.bubblelevel.pocket.domain.model.*

enum class CalibrationPhase { ACQUIRING, READY, CAPTURING, SAVING, COMPLETE, ERROR }
data class CalibrationUiState(
    val mode: InstrumentMode = InstrumentMode.SURFACE,
    val edge: EdgeReference = EdgeReference.SHORT,
    val phase: CalibrationPhase = CalibrationPhase.ACQUIRING,
    val reading: LiveReading? = null,
    val error: CaptureFailure? = null,
    val resetDialog: Boolean = false,
    val previous: Calibration? = null
)
