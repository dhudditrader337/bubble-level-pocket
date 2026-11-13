package com.bubblelevel.pocket.domain.measurement

import com.bubblelevel.pocket.domain.model.*
import kotlin.math.*

object CalibrationEngine {
    fun solve(vectors: List<Vector3>, mode: InstrumentMode, edge: EdgeReference, time: Long, source: String): Calibration? {
        if (vectors.size < 30 || vectors.any { !it.finite }) return null
        val mean = vectors.fold(Vector3(0.0, 0.0, 0.0)) { sum, value -> sum + value }.unit()
        if (mean.magnitude < 0.9) return null
        val angles = vectors.map { MeasurementEngine.edgeAngle(it, edge) }
        val offset = if (mode == InstrumentMode.EDGE) MeasurementEngine.circularMean(angles) else 0.0
        if (!offset.isFinite()) return null
        if (mode == InstrumentMode.SURFACE && (mean.z <= 0.0 || MeasurementEngine.degrees(atan2(hypot(mean.x, mean.y), mean.z)) > 10.0)) return null
        if (mode == InstrumentMode.EDGE && abs(offset) > 10.0) return null
        return Calibration(calibrationSlot(mode, edge), mean, offset, time, source)
    }
}
