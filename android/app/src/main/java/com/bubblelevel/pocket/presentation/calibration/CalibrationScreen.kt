package com.bubblelevel.pocket.presentation.calibration

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
fun CalibrationRoute(destination: Screen.Calibration, active: Boolean, onBusy: (Boolean) -> Unit, onEvent: (AppEvent) -> Unit) {
    val model: CalibrationViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    SensorLifecycle(active, { model.open(destination.visit, destination.mode, destination.edge) }, model::pause)
    ScreenEvents(model.events, active, onEvent)
    SideEffect { if (active) onBusy(state.phase == CalibrationPhase.SAVING) }
    DisposableEffect(Unit) { onDispose { onBusy(false) } }
    CalibrationScreen(state, model::select, model::zero, model::retry, model::resetDialog, model::reset, onEvent)
}

@Composable
fun CalibrationScreen(state: CalibrationUiState, onSelect: (InstrumentMode, EdgeReference) -> Unit, onZero: () -> Unit, onRetry: () -> Unit, onResetDialog: (Boolean) -> Unit, onReset: () -> Unit, onEvent: (AppEvent) -> Unit) {
    val busy = state.phase == CalibrationPhase.SAVING
    val complete = state.phase == CalibrationPhase.COMPLETE
    ToolPage(Modifier.safeDrawingPadding(), BackdropVariant.CALIBRATION) {
        ScreenHeader(R.string.calibration_title, { onEvent(AppEvent.Back) }, !busy)
        InstrumentCard {
            Image(painterResource(R.drawable.logo), null, Modifier.size(96.dp), contentScale = ContentScale.Fit)
            Text(stringResource(if (state.mode == InstrumentMode.SURFACE) R.string.calibration_surface_help else R.string.calibration_edge_help), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.calibration_explanation), style = MaterialTheme.typography.bodyMedium)
        }
        ModeSelector(state.mode, { onSelect(it, state.edge) }, !busy)
        if (state.mode == InstrumentMode.EDGE) EdgeSelector(state.edge, { onSelect(state.mode, it) }, !busy)
        if (!complete) {
            InstrumentCard(strong = true) {
                if (state.mode == InstrumentMode.SURFACE) SurfaceVial(state.reading?.x ?: 0.0, state.reading?.y ?: 0.0, state.reading != null, false, state.reading?.displayRotation ?: 0, Modifier.size(208.dp))
                else EdgeVial(state.reading?.angle ?: 0.0, state.reading != null, false)
                AngleReadout(state.reading?.angle, AngleUnit.DEGREES, signed = state.mode == InstrumentMode.EDGE, compact = true)
            }
        }
        val status = when {
            state.error != null -> failureResource(state.error)
            complete -> if (state.previous == null) R.string.factory_zero else R.string.reference_saved
            busy -> R.string.saving_reference
            state.phase == CalibrationPhase.CAPTURING -> R.string.collecting_reference
            state.phase == CalibrationPhase.READY -> R.string.ready_zero
            else -> R.string.hold_still
        }
        StatusBadge(status)
        if (busy || state.phase == CalibrationPhase.CAPTURING || state.phase == CalibrationPhase.ACQUIRING) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (complete) PrimaryButton(R.string.done, { onEvent(AppEvent.Back) })
        else {
            Text(stringResource(R.string.zero_hint), style = MaterialTheme.typography.bodyMedium)
            val reason = stringResource(if (state.phase == CalibrationPhase.READY) R.string.ready_zero else R.string.zero_wait)
            PrimaryButton(R.string.zero, onZero, Modifier.semantics { stateDescription = reason }, enabled = state.phase == CalibrationPhase.READY, loading = busy)
            if (state.phase == CalibrationPhase.ERROR) SecondaryButton(R.string.retry, onRetry, Modifier.fillMaxWidth())
        }
        SecondaryButton(R.string.reset_zero, { onResetDialog(true) }, Modifier.fillMaxWidth(), enabled = !busy)
        Text(state.previous?.let { stringResource(R.string.reference_dated, AngleFormatter.date(it.timestamp)) } ?: stringResource(R.string.factory_zero), style = MaterialTheme.typography.bodyMedium)
    }
    if (state.resetDialog) ConfirmDeleteDialog(R.string.reset_reference_title, R.string.reset_reference_message, R.string.reset_zero, busy, onReset, { onResetDialog(false) })
}
