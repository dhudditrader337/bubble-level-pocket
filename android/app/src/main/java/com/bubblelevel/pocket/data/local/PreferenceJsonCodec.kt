package com.bubblelevel.pocket.data.local

import com.bubblelevel.pocket.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

object PreferenceJsonCodec {
    private fun vector(value: Vector3) = JSONArray().put(value.x).put(value.y).put(value.z)
    private fun readVector(value: JSONArray): Vector3 {
        val result = Vector3(value.getDouble(0), value.getDouble(1), value.getDouble(2))
        require(result.finite && result.magnitude in 0.9..1.1)
        return result
    }

    fun calibration(value: Calibration): JSONObject = JSONObject()
        .put("slot", value.slot.name).put("vector", vector(value.reference)).put("offset", value.offset)
        .put("time", value.timestamp).put("sensor", value.sensor).put("basis", value.basis).put("version", value.version)

    fun readCalibration(value: JSONObject): Calibration {
        require(value.getInt("version") == 1 && value.getString("basis") == "portrait_v1")
        val result = Calibration(CalibrationSlot.valueOf(value.getString("slot")), readVector(value.getJSONArray("vector")), value.getDouble("offset"), value.getLong("time"), value.getString("sensor"))
        require(result.offset.isFinite() && kotlin.math.abs(result.offset) <= 10.0 && result.timestamp > 0L && result.sensor.isNotBlank())
        if (result.slot == CalibrationSlot.SURFACE) require(result.reference.z >= kotlin.math.cos(Math.toRadians(10.0)))
        return result
    }

    fun settings(value: AppSettings): String = JSONObject().put("schema", 1).put("unit", value.unit.name).put("sound", value.sound).put("haptics", value.haptics).toString()
    fun readSettings(raw: String?): AppSettings {
        val value = runCatching { JSONObject(raw ?: "{}") }.getOrElse { JSONObject() }
        return AppSettings(
            unit = runCatching { AngleUnit.valueOf(value.getString("unit")) }.getOrDefault(AngleUnit.DEGREES),
            sound = runCatching { value.getBoolean("sound") }.getOrDefault(false),
            haptics = runCatching { value.getBoolean("haptics") }.getOrDefault(true)
        )
    }

    fun calibrations(values: Map<CalibrationSlot, Calibration>): String {
        val result = JSONObject().put("schema", 1)
        values.forEach { (slot, value) -> result.put(slot.name, calibration(value)) }
        return result.toString()
    }

    fun readCalibrations(raw: String?): CalibrationState {
        if (raw == null) return CalibrationState()
        val result = runCatching { JSONObject(raw).also { require(it.getInt("schema") == 1) } }.getOrNull() ?: return CalibrationState(invalidSlots = CalibrationSlot.entries.toSet())
        val records = mutableMapOf<CalibrationSlot, Calibration>()
        val invalid = mutableSetOf<CalibrationSlot>()
        CalibrationSlot.entries.forEach { slot ->
            if (result.has(slot.name)) {
                val decoded = runCatching { readCalibration(result.getJSONObject(slot.name)).also { require(it.slot == slot) } }.getOrNull()
                if (decoded == null) invalid.add(slot) else records[slot] = decoded
            }
        }
        return CalibrationState(records.toMap(), invalid.toSet())
    }

    fun history(values: List<Measurement>): String {
        val array = JSONArray()
        values.forEach { m ->
            val r = m.reading
            array.put(JSONObject().put("schema", m.schema).put("id", m.id).put("captured", m.capturedAt)
                .put("label", m.label.name).put("mode", m.mode.name).put("edge", m.edge.name)
                .put("x", r.x).put("y", r.y).put("angle", r.angle).put("vector", vector(r.vector))
                .put("window", r.windowMs).put("source", r.source).put("basic", r.basicMotion)
                .put("calibration", r.calibration?.let(::calibration) ?: JSONObject.NULL)
                .put("unit", m.originalUnit.name).put("saved", m.savedAt))
        }
        return JSONObject().put("schema", 1).put("entries", array).toString()
    }

    fun readHistory(raw: String?): HistoryState {
        if (raw == null) return HistoryState(loaded = true)
        val array = runCatching { JSONObject(raw).also { require(it.getInt("schema") == 1) }.getJSONArray("entries") }.getOrNull() ?: return HistoryState(loaded = true, corrupt = true)
        val valid = mutableListOf<Measurement>()
        var corrupt = false
        for (index in 0 until array.length()) {
            val measurement = runCatching {
                val value = array.getJSONObject(index)
                require(value.getInt("schema") == 1)
                val id = value.getString("id")
                val captured = value.getLong("captured")
                val saved = value.getLong("saved")
                require(id.isNotBlank() && valid.none { it.id == id } && captured > 0L && saved > 0L)
                val x = value.getDouble("x")
                val y = value.getDouble("y")
                val angle = value.getDouble("angle")
                require(listOf(x, y, angle).all { it.isFinite() && kotlin.math.abs(it) <= 180.0 })
                val mode = InstrumentMode.valueOf(value.getString("mode"))
                require(if (mode == InstrumentMode.SURFACE) angle in 0.0..90.0 else angle in -90.0..90.0)
                val window = value.getLong("window")
                require(window >= 750L)
                val reference = if (value.isNull("calibration")) null else readCalibration(value.getJSONObject("calibration"))
                if (reference != null) require(reference.slot == calibrationSlot(mode, EdgeReference.valueOf(value.getString("edge"))) && reference.sensor == value.getString("source"))
                val reading = LiveReading(x, y, angle, readVector(value.getJSONArray("vector")), 0L, true, kotlin.math.abs(angle) <= 0.5, window, value.getString("source"), value.getBoolean("basic"), 0, reference)
                Measurement(id, captured, MeasurementLabel.valueOf(value.getString("label")), mode, EdgeReference.valueOf(value.getString("edge")), reading, AngleUnit.valueOf(value.getString("unit")), saved)
            }.getOrNull()
            if (measurement != null && valid.size < 100) valid.add(measurement) else corrupt = true
        }
        return HistoryState(true, valid.sortedWith(compareByDescending<Measurement> { it.capturedAt }.thenBy { it.id }), corrupt)
    }
}
