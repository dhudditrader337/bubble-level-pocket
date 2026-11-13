package com.bubblelevel.pocket.core.ui

import com.bubblelevel.pocket.domain.measurement.MeasurementEngine
import com.bubblelevel.pocket.domain.model.AngleUnit
import java.text.NumberFormat
import java.text.DateFormat
import java.util.Date
import kotlin.math.abs
import kotlin.math.round

object AngleFormatter {
    fun number(value: Double): String {
        val format = NumberFormat.getNumberInstance().apply { minimumFractionDigits = 1; maximumFractionDigits = 1; isGroupingUsed = false }
        val rounded = round(value * 10.0) / 10.0
        return format.format(if (rounded == 0.0) 0.0 else rounded)
    }
    fun displayed(angle: Double, unit: AngleUnit): Double = if (unit == AngleUnit.DEGREES) angle else MeasurementEngine.percent(angle)
    fun steep(angle: Double, unit: AngleUnit): Boolean = unit == AngleUnit.PERCENT && (abs(angle) >= 89.0 || abs(MeasurementEngine.percent(angle)) > 999.9)
    fun date(epoch: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epoch))
}
