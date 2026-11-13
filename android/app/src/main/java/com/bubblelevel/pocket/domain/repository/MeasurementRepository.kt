package com.bubblelevel.pocket.domain.repository

import com.bubblelevel.pocket.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface MeasurementRepository {
    val history: StateFlow<HistoryState>
    suspend fun load()
    suspend fun save(measurement: Measurement): WriteOutcome
    suspend fun delete(ids: Set<String>): Boolean
    suspend fun recover(): Boolean
}
