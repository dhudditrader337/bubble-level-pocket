package com.bubblelevel.pocket.presentation.app

import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.domain.model.*

data class AppUiState(val backStack: List<Screen> = emptyList(), val busy: Boolean = false)

sealed interface AppEvent {
    data class Navigate(val screen: Screen) : AppEvent
    data class Captured(val measurement: Measurement) : AppEvent
    data class Result(val result: MeasurementResult) : AppEvent
    data object Back : AppEvent
    data object Menu : AppEvent
    data object Play : AppEvent
    data object Again : AppEvent
    data object Resume : AppEvent
    data object Haptic : AppEvent
    data object Finish : AppEvent
}

class SessionStore {
    val captures = mutableMapOf<String, Measurement>()
    val results = mutableMapOf<String, MeasurementResult>()
}
