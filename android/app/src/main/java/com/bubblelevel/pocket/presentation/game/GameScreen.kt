package com.bubblelevel.pocket.presentation.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
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
import kotlin.math.abs
import kotlin.math.hypot

@Composable
fun GameRoute(destination: Screen.Game, active: Boolean, onEvent: (AppEvent) -> Unit) {
    val model: GameViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    ScreenEvents(model.events, active, onEvent)
    SensorLifecycle(active, { model.enter(destination.session, destination.surface) }, model::pause)
    GameScreen(state, model::mode, model::edge, model::unit, model::lock, model::calibrate, onEvent)
}

@Composable
fun GameScreen(state: GameUiState, onMode: (InstrumentMode) -> Unit, onEdge: (EdgeReference) -> Unit, onUnit: (AngleUnit) -> Unit, onLock: () -> Unit, onCalibrate: () -> Unit, onEvent: (AppEvent) -> Unit) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    ToolBackdrop(BackdropVariant.LIVE) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            val compact = maxHeight < 600.dp || largeText
            Column(Modifier.widthIn(max = 640.dp).fillMaxSize().then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onEvent(AppEvent.Back) }) { Text(stringResource(R.string.back)) }
                    TextButton(onClick = { onEvent(AppEvent.Navigate(Screen.Readings)) }) { Text(stringResource(R.string.readings)) }
                }
                Text(stringResource(if (state.mode == InstrumentMode.SURFACE) R.string.surface_level else R.string.edge_level), Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
                ModeSelector(state.mode, onMode)
                Column(if (compact) Modifier.fillMaxWidth() else Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LiveInstrument(state)
                    if (state.mode == InstrumentMode.EDGE) EdgeSelector(state.edge, onEdge)
                }
                InstrumentCard {
                    UnitSelector(state.unit, onUnit)
                    Text(stringResource(R.string.lock_hint), style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        SecondaryButton(R.string.calibrate, onCalibrate, Modifier.weight(1f))
                        val disabledReason = stringResource(R.string.sensor_unavailable)
                        PrimaryButton(R.string.lock, onLock, Modifier.weight(1f).semantics { if (state.failure == CaptureFailure.UNAVAILABLE) stateDescription = disabledReason }, enabled = state.failure != CaptureFailure.UNAVAILABLE)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveInstrument(state: GameUiState) {
    val reading = state.reading
    val status = when {
        state.failure != null -> failureResource(state.failure)
        reading?.level == true -> R.string.level
        else -> R.string.tilted
    }
    val description = stringResource(R.string.instrument_description,
        stringResource(if (state.mode == InstrumentMode.SURFACE) R.string.surface_level else R.string.edge_level),
        angleText(reading?.angle, state.unit, state.mode == InstrumentMode.EDGE), stringResource(status))
    InstrumentCard(strong = true) {
        Column(Modifier.fillMaxWidth().semantics(mergeDescendants = true) { stateDescription = description }, horizontalAlignment = Alignment.CenterHorizontally) {
            Crossfade(state.mode, animationSpec = tween(180), label = "instrument") { mode ->
                if (mode == InstrumentMode.SURFACE) SurfaceVial(reading?.x ?: 0.0, reading?.y ?: 0.0, reading != null, reading?.level == true, reading?.displayRotation ?: 0)
                else EdgeVial(reading?.angle ?: 0.0, reading != null, reading?.level == true, Modifier.padding(vertical = 24.dp))
            }
            AngleReadout(reading?.angle, state.unit, signed = state.mode == InstrumentMode.EDGE)
            if (state.mode == InstrumentMode.SURFACE && reading != null) Text(stringResource(R.string.axis_values, angleText(reading.x, state.unit, true), angleText(reading.y, state.unit, true)), style = MaterialTheme.typography.bodyMedium)
        }
        StatusBadge(status)
        if (reading == null && state.failure == CaptureFailure.WAITING) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (reading != null && state.failure != null) Text(stringResource(R.string.stale_reading), style = MaterialTheme.typography.labelSmall)
        if (reading != null && (if (state.mode == InstrumentMode.SURFACE) hypot(reading.x, reading.y) > 15.0 else abs(reading.angle) > 10.0)) Text(stringResource(R.string.out_of_range), style = MaterialTheme.typography.bodyMedium)
        if (reading?.level == true) Text(stringResource(R.string.level_tolerance), style = MaterialTheme.typography.bodyMedium)
        if (reading?.basicMotion == true) Text(stringResource(R.string.basic_motion), style = MaterialTheme.typography.labelSmall)
        if (state.mode == InstrumentMode.EDGE && reading != null && abs(reading.angle) > 0.05) Text(stringResource(if (reading.angle > 0.0) R.string.clockwise else R.string.counterclockwise), style = MaterialTheme.typography.bodyMedium)
        if (state.recalibrate) Text(stringResource(R.string.recalibration_needed), style = MaterialTheme.typography.bodyMedium)
    }
}
