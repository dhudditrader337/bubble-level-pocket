package com.bubblelevel.pocket.domain.model

enum class InstrumentMode { SURFACE, EDGE }
enum class EdgeReference { SHORT, LONG }
enum class AngleUnit { DEGREES, PERCENT }
enum class MeasurementLabel { MEASUREMENT, SHELF, FRAME, WORKTOP, REFERENCE }
enum class CalibrationSlot { SURFACE, EDGE_SHORT, EDGE_LONG }

fun calibrationSlot(mode: InstrumentMode, edge: EdgeReference): CalibrationSlot = when {
    mode == InstrumentMode.SURFACE -> CalibrationSlot.SURFACE
    edge == EdgeReference.SHORT -> CalibrationSlot.EDGE_SHORT
    else -> CalibrationSlot.EDGE_LONG
}
