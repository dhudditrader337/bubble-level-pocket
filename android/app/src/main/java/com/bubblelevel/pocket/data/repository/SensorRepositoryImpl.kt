package com.bubblelevel.pocket.data.repository

import com.bubblelevel.pocket.data.sensor.AndroidSensorSource
import com.bubblelevel.pocket.domain.repository.SensorRepository

class SensorRepositoryImpl(private val source: AndroidSensorSource) : SensorRepository {
    override fun observations() = source.observations()
    override fun now() = source.now()
}
