package com.bubblelevel.pocket.domain.repository

import com.bubblelevel.pocket.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface CalibrationRepository {
    val calibrations: StateFlow<CalibrationState>
    suspend fun put(calibration: Calibration): Boolean
    suspend fun reset(slot: CalibrationSlot): Boolean
}
