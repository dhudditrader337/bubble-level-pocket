package com.bubblelevel.pocket.data.sensor

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.view.WindowManager
import com.bubblelevel.pocket.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlin.math.exp

class AndroidSensorSource(private val context: Context) {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windows = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @Suppress("DEPRECATION")
    fun observations() = callbackFlow {
        val gravitySensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val primary = gravitySensor ?: accelerometer
        if (primary == null) {
            trySend(SensorObservation(Vector3(0.0, 0.0, 0.0), now(), null, "", 0, unavailable = true))
            close()
            awaitClose { }
            return@callbackFlow
        }
        val rotation = windows.defaultDisplay.rotation
        val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val naturalLandscape = if (rotation % 2 == 0) landscape else !landscape
        var acceleration: Double? = null
        var accelerationTime = 0L
        var filtered: Vector3? = null
        var previous = 0L
        val source = "${primary.type}:${primary.name}"
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            override fun onSensorChanged(event: SensorEvent) {
                val time = event.timestamp / 1_000_000L
                val raw = Vector3(event.values[0].toDouble(), event.values[1].toDouble(), event.values[2].toDouble())
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    acceleration = raw.magnitude
                    accelerationTime = time
                }
                if (event.sensor.type != primary.type) return
                val vector = if (gravitySensor == null) {
                    val dt = if (previous == 0L) 0.0 else ((time - previous) / 1000.0).coerceIn(0.0, 1.0)
                    val alpha = 1.0 - exp(-dt / 0.45)
                    val value = filtered?.let { it * (1.0 - alpha) + raw * alpha } ?: raw
                    filtered = value
                    previous = time
                    value
                } else raw
                trySend(SensorObservation(
                    SensorCoordinateMapper.portrait(vector, naturalLandscape), time,
                    if (accelerometer == null) null else if (time - accelerationTime in 0L..300L) acceleration else Double.NaN,
                    source, SensorCoordinateMapper.portraitRotation(windows.defaultDisplay.rotation, naturalLandscape)
                ))
            }
        }
        val registered = manager.registerListener(listener, primary, SensorManager.SENSOR_DELAY_GAME)
        if (gravitySensor != null && accelerometer != null) manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        if (!registered) trySend(SensorObservation(Vector3(0.0, 0.0, 0.0), now(), null, source, 0, unavailable = true))
        awaitClose { manager.unregisterListener(listener) }
    }.conflate()

    fun now(): Long = SystemClock.elapsedRealtime()
}
