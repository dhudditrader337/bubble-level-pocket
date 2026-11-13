package com.bubblelevel.pocket.presentation.anglelock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.di.ServiceLocator
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.core.ui.*
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.presentation.app.AppEvent

@Composable
fun AngleLockRoute(destination: Screen.AngleLock, active: Boolean, onBusy: (Boolean) -> Unit, onEvent: (AppEvent) -> Unit) {
    val model: AngleLockViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    DisposableEffect(destination.id, destination.readOnly) { model.open(destination.id, destination.readOnly); onDispose { } }
    SideEffect { if (active) onBusy(state.saving) }
    DisposableEffect(Unit) { onDispose { onBusy(false) } }
    ScreenEvents(model.events, active, onEvent)
    AngleLockScreen(state, model::save, model::picker, model::label, model::confirmDelete, model::delete, onEvent)
}

@Composable
fun AngleLockScreen(state: AngleLockUiState, onSave: () -> Unit, onPicker: (Boolean) -> Unit, onLabel: (MeasurementLabel) -> Unit, onDeleteDialog: (Boolean) -> Unit, onDelete: () -> Unit, onEvent: (AppEvent) -> Unit) {
    ToolPage(Modifier.safeDrawingPadding(), BackdropVariant.HOLD) {
        ScreenHeader(if (state.readOnly) R.string.saved_reading else R.string.angle_locked, { onEvent(AppEvent.Back) }, !state.saving)
        val m = state.measurement
        if (m != null) {
            val held = stringResource(if (state.saved) R.string.saved_status else R.string.held_reading)
            InstrumentCard(Modifier.semantics { stateDescription = held }, strong = true) {
                Image(painterResource(R.drawable.icon_pause), stringResource(R.string.held_reading), Modifier.size(40.dp), contentScale = ContentScale.Fit)
                StatusBadge(if (state.saved) R.string.saved_status else R.string.held_reading)
                if (m.mode == InstrumentMode.SURFACE) SurfaceVial(m.reading.x, m.reading.y, true, m.reading.level, m.reading.displayRotation, Modifier.size(208.dp)) else EdgeVial(m.reading.angle, true, m.reading.level)
                MeasurementDetails(m, state.unit)
                Text(stringResource(R.string.stable_capture), style = MaterialTheme.typography.bodyMedium)
            }
            if (!state.readOnly && !state.saved) SecondaryButton(R.string.choose_label, { onPicker(true) }, Modifier.fillMaxWidth(), enabled = !state.saving)
            if (state.outcome == WriteOutcome.FAILED || state.outcome == WriteOutcome.READ_ONLY) StatusBadge(R.string.save_failed)
            if (state.readOnly) {
                SecondaryButton(R.string.return_action, { onEvent(AppEvent.Back) }, Modifier.fillMaxWidth(), enabled = !state.saving)
                SecondaryButton(R.string.delete, { onDeleteDialog(true) }, Modifier.fillMaxWidth(), enabled = !state.saving)
            } else {
                Text(stringResource(if (state.capacity && !state.saved) R.string.capacity_hint else R.string.save_hint), style = MaterialTheme.typography.bodyLarge)
                if (state.capacity && !state.saved) PrimaryButton(R.string.open_readings, { onEvent(AppEvent.Navigate(Screen.Readings)) })
                else PrimaryButton(if (state.saving) R.string.saving else if (state.saved) R.string.saved else if (state.outcome == WriteOutcome.FAILED) R.string.retry else R.string.save, onSave, enabled = !state.saved, loading = state.saving)
                SecondaryButton(R.string.resume, { onEvent(AppEvent.Resume) }, Modifier.fillMaxWidth(), enabled = !state.saving)
            }
        } else {
            StatusBadge(R.string.reading_missing)
            SecondaryButton(R.string.return_action, { onEvent(AppEvent.Back) })
        }
    }
    if (state.labelPicker) ChoiceDialog(state.measurement?.label ?: MeasurementLabel.MEASUREMENT, onLabel, { onPicker(false) })
    if (state.deleteDialog) ConfirmDeleteDialog(R.string.delete_reading_title, R.string.delete_reading_message, busy = state.saving, onConfirm = onDelete, onDismiss = { onDeleteDialog(false) })
}
