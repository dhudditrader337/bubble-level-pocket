package com.bubblelevel.pocket.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.di.ServiceLocator
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.core.ui.*
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.presentation.app.AppEvent

@Composable
fun SettingsRoute(onEvent: (AppEvent) -> Unit) {
    val model: SettingsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    SettingsScreen(state, model::unit, model::sound, model::haptics, model::retry, onEvent)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onUnit: (AngleUnit) -> Unit, onSound: (Boolean) -> Unit, onHaptics: (Boolean) -> Unit, onRetry: () -> Unit, onEvent: (AppEvent) -> Unit) {
    ToolPage(Modifier.safeDrawingPadding(), BackdropVariant.SETTINGS) {
        ScreenHeader(R.string.settings_title, { onEvent(AppEvent.Back) })
        if (state.busy || state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.error) InstrumentCard { StatusBadge(R.string.setting_failed); SecondaryButton(R.string.retry, onRetry, enabled = !state.busy) }
        InstrumentCard {
            Text(stringResource(R.string.measurement_units), style = MaterialTheme.typography.titleLarge)
            UnitSelector(state.settings.unit, onUnit, !state.busy && !state.loading)
            Text(stringResource(R.string.percent_explanation), style = MaterialTheme.typography.bodyMedium)
        }
        InstrumentCard { SettingRow(R.string.sound_title, R.string.sound_description, state.settings.sound, !state.busy && !state.loading, onSound) }
        InstrumentCard { SettingRow(R.string.haptics_title, R.string.haptics_description, state.settings.haptics, !state.busy && !state.loading, onHaptics) }
        InstrumentCard {
            Text(stringResource(R.string.calibration_title), style = MaterialTheme.typography.titleLarge)
            CalibrationSlot.entries.forEach { slot ->
                val label = stringResource(when (slot) { CalibrationSlot.SURFACE -> R.string.surface_level; CalibrationSlot.EDGE_SHORT -> R.string.short_edge_description; CalibrationSlot.EDGE_LONG -> R.string.long_edge_description })
                val calibration = state.calibrations.entries[slot]
                val summary = calibration?.let { stringResource(R.string.reference_dated, AngleFormatter.date(it.timestamp)) } ?: stringResource(if (slot in state.calibrations.invalidSlots) R.string.recalibration_needed else R.string.factory_zero)
                Text(stringResource(R.string.calibration_summary, label, summary), style = MaterialTheme.typography.bodyMedium)
            }
            SecondaryButton(R.string.calibrate, { onEvent(AppEvent.Navigate(Screen.Calibration())) }, Modifier.fillMaxWidth())
        }
        InstrumentCard {
            Text(stringResource(R.string.device_only), style = MaterialTheme.typography.bodyLarge)
            SecondaryButton(R.string.readings, { onEvent(AppEvent.Navigate(Screen.Readings)) }, Modifier.fillMaxWidth())
        }
    }
}
