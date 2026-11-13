package com.bubblelevel.pocket.data.sensor

import com.bubblelevel.pocket.domain.model.Vector3

object SensorCoordinateMapper {
    fun portrait(vector: Vector3, naturalLandscape: Boolean): Vector3 = if (naturalLandscape) Vector3(-vector.y, vector.x, vector.z) else vector
    fun portraitRotation(displayRotation: Int, naturalLandscape: Boolean): Int = (displayRotation + if (naturalLandscape) 1 else 0) % 4
}
