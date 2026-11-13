package com.bubblelevel.pocket.data.repository

import com.bubblelevel.pocket.data.local.PreferencesStore
import com.bubblelevel.pocket.domain.model.AppSettings
import com.bubblelevel.pocket.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val store: PreferencesStore) : SettingsRepository {
    override val settings = store.settings
    override suspend fun set(settings: AppSettings) = store.setSettings(settings)
}
