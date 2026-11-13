package com.bubblelevel.pocket.domain.measurement

import com.bubblelevel.pocket.domain.model.*
import kotlin.math.*

class MeasurementEngine {
    private data class Point(val time: Long, val x: Double, val y: Double, val angle: Double, val vector: Vector3, val acceleration: Double?)
    private val points = mutableListOf<Point>()
    private var smoothed: Vector3? = null
    private var previousTime = 0L
    private var levelSince = 0L
    private var onLevel = false

    fun reset() {
        points.clear()
        smoothed = null
        previousTime = 0L
        levelSince = 0L
        onLevel = false
    }

    fun references(): List<Vector3> = points.map { it.vector }

    fun evaluate(observation: SensorObservation, now: Long, mode: InstrumentMode, edge: EdgeReference, calibration: Calibration?, calibrating: Boolean = false): Evaluation {
        val raw = observation.gravity
        val failure = when {
            observation.unavailable -> CaptureFailure.UNAVAILABLE
            previousTime != 0L && observation.timestamp <= previousTime -> CaptureFailure.STALE
            now - observation.timestamp !in 0L..300L -> CaptureFailure.STALE
            !raw.finite || raw.magnitude !in 7.0..12.5 -> CaptureFailure.MOVING
            mode == InstrumentMode.SURFACE && (raw.z <= 0.0 || degrees(atan2(hypot(raw.x, raw.y), raw.z)) > 80.0) -> CaptureFailure.FACE_DOWN
            mode == InstrumentMode.EDGE && hypot(raw.x, raw.y) / raw.magnitude < 0.75 -> CaptureFailure.UPRIGHT
            else -> null
        }
        if (failure != null) {
            reset()
            return Evaluation(failure = failure)
        }
        val compatible = calibration?.takeIf { it.sensor == observation.source && it.basis == "portrait_v1" }
        val unit = raw.unit()
        val corrected = if (mode == InstrumentMode.SURFACE && compatible != null) rotateToUp(unit, compatible.reference) else unit
        if (mode == InstrumentMode.SURFACE && corrected.z <= 0.0) {
            reset()
            return Evaluation(failure = CaptureFailure.FACE_DOWN)
        }
        val x = degrees(atan2(corrected.x, corrected.z))
        val y = degrees(atan2(corrected.y, corrected.z))
        val rawAngle = if (mode == InstrumentMode.SURFACE) degrees(atan2(hypot(corrected.x, corrected.y), corrected.z)) else edgeAngle(unit, edge)
        val angle = if (mode == InstrumentMode.EDGE) normalizeEdge(rawAngle - (compatible?.offset ?: 0.0)) else rawAngle
        if (calibrating && abs(rawAngle) > 10.0) {
            reset()
            return Evaluation(failure = CaptureFailure.CLOSER)
        }
        points.add(Point(observation.timestamp, x, y, angle, unit, observation.accelerationMagnitude))
        val targetWindow = if (calibrating) 1500L else 750L
        while (points.size > 1 && points[1].time <= observation.timestamp - targetWindow) points.removeAt(0)
        val span = observation.timestamp - points.first().time
        val limit = if (calibrating) 0.10 else 0.15
        val motionLimit = if (calibrating) 0.35 else 0.6
        val meanAngle = if (mode == InstrumentMode.EDGE) circularMean(points.map { it.angle }) else points.map { it.angle }.average()
        fun deviation(values: List<Double>): Double {
            val mean = values.average()
            return sqrt(values.sumOf { (it - mean).pow(2) } / values.size)
        }
        val spread = if (mode == InstrumentMode.SURFACE) hypot(deviation(points.map { it.x }), deviation(points.map { it.y })) else sqrt(points.sumOf { normalizeEdge(it.angle - meanAngle).pow(2) } / points.size)
        val first = points.first()
        val speed = if (span > 0L) {
            val delta = if (mode == InstrumentMode.SURFACE) hypot(x - first.x, y - first.y) else abs(normalizeEdge(angle - first.angle))
            delta * 1000.0 / span
        } else Double.POSITIVE_INFINITY
        val stable = points.size >= (if (calibrating) 30 else 15) && span >= targetWindow && spread <= limit && speed <= 1.0 && points.all { it.acceleration == null || abs(it.acceleration - 9.80665) <= motionLimit }
        if (!stable || abs(angle) > 0.7) {
            levelSince = 0L
            onLevel = false
        } else if (abs(angle) <= 0.5) {
            if (levelSince == 0L) levelSince = observation.timestamp
            if (observation.timestamp - levelSince >= 600L) onLevel = true
        }
        val dt = if (previousTime == 0L) 0.0 else ((observation.timestamp - previousTime) / 1000.0).coerceIn(0.0, 0.5)
        val alpha = 1.0 - exp(-dt / 0.12)
        val display = smoothed?.let { it * (1.0 - alpha) + corrected * alpha } ?: corrected
        smoothed = display
        previousTime = observation.timestamp
        val displayAngle = if (mode == InstrumentMode.SURFACE) degrees(atan2(hypot(display.x, display.y), display.z)) else normalizeEdge(edgeAngle(display, edge) - (compatible?.offset ?: 0.0))
        return Evaluation(
            reading = LiveReading(degrees(atan2(display.x, display.z)), degrees(atan2(display.y, display.z)), displayAngle, unit, observation.timestamp, stable, onLevel, span, observation.source, observation.accelerationMagnitude == null, observation.displayRotation, compatible),
            failure = if (stable) null else CaptureFailure.MOVING,
            calibrationReady = calibrating && stable
        )
    }

    companion object {
        fun degrees(radians: Double): Double = radians * 180.0 / PI
        fun normalizeEdge(angle: Double): Double = ((angle + 90.0) % 180.0 + 180.0) % 180.0 - 90.0
        fun edgeAngle(vector: Vector3, edge: EdgeReference): Double = normalizeEdge(degrees(atan2(-vector.x, vector.y)) - if (edge == EdgeReference.LONG) 90.0 else 0.0)
        fun circularMean(angles: List<Double>): Double = degrees(atan2(angles.sumOf { sin(it * PI / 90.0) }, angles.sumOf { cos(it * PI / 90.0) })) / 2.0
        fun rotateToUp(vector: Vector3, reference: Vector3): Vector3 {
            val from = reference.unit()
            val axis = from.cross(Vector3(0.0, 0.0, 1.0))
            val dot = from.z
            if (dot > 0.999999999) return vector
            if (dot <= -0.999999) return vector
            return vector + axis.cross(vector) + axis.cross(axis.cross(vector)) * (1.0 / (1.0 + dot))
        }
        fun percent(angle: Double): Double = 100.0 * tan(angle * PI / 180.0)
    }
}
