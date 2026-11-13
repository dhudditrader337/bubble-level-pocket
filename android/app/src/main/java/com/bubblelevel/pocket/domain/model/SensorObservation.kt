package com.bubblelevel.pocket.domain.model

data class SensorObservation(
    val gravity: Vector3,
    val timestamp: Long,
    val accelerationMagnitude: Double?,
    val source: String,
    val displayRotation: Int,
    val unavailable: Boolean = false
)
