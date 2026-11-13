package com.bubblelevel.pocket.core.navigation

import com.bubblelevel.pocket.domain.model.EdgeReference
import com.bubblelevel.pocket.domain.model.InstrumentMode

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val session: Long, val surface: Boolean = false) : Screen
    data class AngleLock(val id: String, val readOnly: Boolean = false) : Screen
    data class GameOver(val resultId: String) : Screen
    data class Calibration(val mode: InstrumentMode = InstrumentMode.SURFACE, val edge: EdgeReference = EdgeReference.SHORT, val visit: String = java.util.UUID.randomUUID().toString()) : Screen
    data object Readings : Screen
    data object Settings : Screen
}
