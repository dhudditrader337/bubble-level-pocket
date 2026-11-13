package com.bubblelevel.pocket.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bubblelevel.pocket.presentation.app.AppViewModel
import com.bubblelevel.pocket.presentation.splash.SplashViewModel
import com.bubblelevel.pocket.presentation.menu.MenuViewModel
import com.bubblelevel.pocket.presentation.game.GameViewModel
import com.bubblelevel.pocket.presentation.anglelock.AngleLockViewModel
import com.bubblelevel.pocket.presentation.gameover.GameOverViewModel
import com.bubblelevel.pocket.presentation.calibration.CalibrationViewModel
import com.bubblelevel.pocket.presentation.readings.ReadingsViewModel
import com.bubblelevel.pocket.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val d: Dependencies) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(d.measurements, d.sessions)
        SplashViewModel::class.java -> SplashViewModel()
        MenuViewModel::class.java -> MenuViewModel(d.measurements, d.settings, d.calibrations, d.sampleAngle)
        GameViewModel::class.java -> GameViewModel(d.sensors, d.settings, d.calibrations, d.feedback, d.epoch)
        AngleLockViewModel::class.java -> AngleLockViewModel(d.sessions, d.measurements, d.settings, d.save)
        GameOverViewModel::class.java -> GameOverViewModel(d.sessions, d.settings)
        CalibrationViewModel::class.java -> CalibrationViewModel(d.sensors, d.calibrations, d.settings, d.epoch)
        ReadingsViewModel::class.java -> ReadingsViewModel(d.measurements, d.settings)
        SettingsViewModel::class.java -> SettingsViewModel(d.settings, d.calibrations, d.measurements)
        else -> error(modelClass.name)
    } as T
}
