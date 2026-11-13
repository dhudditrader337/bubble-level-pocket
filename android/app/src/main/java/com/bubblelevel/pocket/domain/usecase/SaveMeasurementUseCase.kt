package com.bubblelevel.pocket.domain.usecase

import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.MeasurementRepository

class SaveMeasurementUseCase(private val measurements: MeasurementRepository, private val epoch: () -> Long) {
    suspend operator fun invoke(measurement: Measurement): WriteOutcome = measurements.save(measurement.copy(savedAt = epoch()))
}
