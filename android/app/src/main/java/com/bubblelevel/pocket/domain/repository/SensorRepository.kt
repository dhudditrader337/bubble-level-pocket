package com.bubblelevel.pocket.domain.repository

import com.bubblelevel.pocket.domain.model.SensorObservation
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    fun observations(): Flow<SensorObservation>
    fun now(): Long
}
