package com.bubblelevel.pocket.domain.model

import kotlin.math.sqrt

data class Vector3(val x: Double, val y: Double, val z: Double) {
    val magnitude: Double get() = sqrt(x * x + y * y + z * z)
    val finite: Boolean get() = x.isFinite() && y.isFinite() && z.isFinite()
    fun unit(): Vector3 = if (magnitude > 0.000001) this * (1.0 / magnitude) else this
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun times(scale: Double) = Vector3(x * scale, y * scale, z * scale)
    fun cross(other: Vector3) = Vector3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
}

data class LiveReading(
    val x: Double,
    val y: Double,
    val angle: Double,
    val vector: Vector3,
    val timestamp: Long,
    val stable: Boolean,
    val level: Boolean,
    val windowMs: Long,
    val source: String,
    val basicMotion: Boolean,
    val displayRotation: Int,
    val calibration: Calibration? = null
)

data class Measurement(
    val id: String,
    val capturedAt: Long,
    val label: MeasurementLabel,
    val mode: InstrumentMode,
    val edge: EdgeReference,
    val reading: LiveReading,
    val originalUnit: AngleUnit,
    val savedAt: Long = 0L,
    val schema: Int = 1
)

data class HistoryState(
    val loaded: Boolean = false,
    val entries: List<Measurement> = emptyList(),
    val corrupt: Boolean = false,
    val readOnly: Boolean = false
)

enum class WriteOutcome { SUCCESS, FAILED, CAPACITY, READ_ONLY }
