package com.bubblelevel.pocket.domain.repository

import com.bubblelevel.pocket.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    suspend fun set(settings: AppSettings): Boolean
}
