package com.bubblelevel.pocket.data.repository

import com.bubblelevel.pocket.data.local.PreferencesStore
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.MeasurementRepository

class MeasurementRepositoryImpl(private val store: PreferencesStore) : MeasurementRepository {
    override val history = store.history
    override suspend fun load() = store.load(force = true)
    override suspend fun save(measurement: Measurement) = store.save(measurement)
    override suspend fun delete(ids: Set<String>) = store.delete(ids)
    override suspend fun recover() = store.recover()
}
