package com.bubblelevel.pocket.presentation.app

import android.app.Activity
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubblelevel.pocket.core.di.ServiceLocator
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.presentation.splash.SplashRoute
import com.bubblelevel.pocket.presentation.menu.MenuRoute
import com.bubblelevel.pocket.presentation.game.GameRoute
import com.bubblelevel.pocket.presentation.anglelock.AngleLockRoute
import com.bubblelevel.pocket.presentation.gameover.GameOverRoute
import com.bubblelevel.pocket.presentation.calibration.CalibrationRoute
import com.bubblelevel.pocket.presentation.readings.ReadingsRoute
import com.bubblelevel.pocket.presentation.settings.SettingsRoute
import kotlinx.coroutines.flow.collect

@Composable
fun AppRoot() {
    val model: AppViewModel = viewModel(factory = ServiceLocator.factory)
    val screen by model.screen.collectAsStateWithLifecycle()
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    BackHandler { if (!state.busy) model.back() }
    LaunchedEffect(Unit) {
        model.events.collect { event ->
            when (event) {
                AppEvent.Finish -> (context as? Activity)?.finish()
                AppEvent.Haptic -> view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                else -> model.handle(event)
            }
        }
    }
    AnimatedContent(targetState = screen, transitionSpec = {
        if (initialState == Screen.Splash) fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        else (fadeIn(tween(180)) + slideInVertically(tween(180)) { 8 }) togetherWith fadeOut(tween(180))
    }, label = "navigation") { destination ->
        when (destination) {
            Screen.Splash -> SplashRoute(model::splashFinished)
            Screen.Menu -> MenuRoute(model::handle)
            is Screen.Game -> GameRoute(destination, screen == destination, model::handle)
            is Screen.AngleLock -> AngleLockRoute(destination, screen == destination, model::busy, model::handle)
            is Screen.GameOver -> GameOverRoute(destination, model::handle)
            is Screen.Calibration -> CalibrationRoute(destination, screen == destination, model::busy, model::handle)
            Screen.Readings -> ReadingsRoute(model::handle)
            Screen.Settings -> SettingsRoute(model::handle)
        }
    }
}
