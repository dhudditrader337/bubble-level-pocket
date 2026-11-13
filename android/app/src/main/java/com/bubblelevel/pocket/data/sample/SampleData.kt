package com.bubblelevel.pocket.data.sample

import com.bubblelevel.pocket.domain.model.*
import kotlin.math.*
import java.time.LocalDateTime
import java.time.ZoneId

object SampleData {
    private fun time(minute: Int) = LocalDateTime.of(2026, 11, 12, 9, minute).atZone(ZoneId.of("Europe/Berlin")).toInstant().toEpochMilli()
    private val surfaceTotal = atan(hypot(tan(0.1 * PI / 180.0), tan(0.2 * PI / 180.0))) * 180.0 / PI
    val examples = listOf(
        Measurement("DEMO_SHELF", time(15), MeasurementLabel.SHELF, InstrumentMode.SURFACE, EdgeReference.SHORT, reading(0.1, 0.2, surfaceTotal), AngleUnit.DEGREES),
        Measurement("DEMO_FRAME", time(21), MeasurementLabel.FRAME, InstrumentMode.EDGE, EdgeReference.SHORT, reading(0.0, 0.0, -0.4), AngleUnit.DEGREES),
        Measurement("DEMO_WORKTOP", time(28), MeasurementLabel.WORKTOP, InstrumentMode.EDGE, EdgeReference.LONG, reading(0.0, 0.0, 1.2), AngleUnit.DEGREES)
    )
    private fun reading(x: Double, y: Double, angle: Double) = LiveReading(x, y, angle, Vector3(0.0, 0.0, 1.0), 0L, true, false, 750L, "DEMO", false, 0)
}
