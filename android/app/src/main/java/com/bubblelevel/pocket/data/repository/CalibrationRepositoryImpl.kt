package com.bubblelevel.pocket.data.repository

import com.bubblelevel.pocket.data.local.PreferencesStore
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.CalibrationRepository

class CalibrationRepositoryImpl(private val store: PreferencesStore) : CalibrationRepository {
    override val calibrations = store.calibrations
    override suspend fun put(calibration: Calibration) = store.setCalibration(calibration.slot, calibration)
    override suspend fun reset(slot: CalibrationSlot) = store.setCalibration(slot, null)
}
