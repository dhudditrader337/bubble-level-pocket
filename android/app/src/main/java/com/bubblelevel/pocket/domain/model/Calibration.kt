package com.bubblelevel.pocket.domain.model

data class Calibration(
    val slot: CalibrationSlot,
    val reference: Vector3,
    val offset: Double,
    val timestamp: Long,
    val sensor: String,
    val basis: String = "portrait_v1",
    val version: Int = 1
)

data class CalibrationState(
    val entries: Map<CalibrationSlot, Calibration> = emptyMap(),
    val invalidSlots: Set<CalibrationSlot> = emptySet()
)
