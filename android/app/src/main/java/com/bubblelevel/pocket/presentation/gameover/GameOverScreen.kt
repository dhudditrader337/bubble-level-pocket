package com.bubblelevel.pocket.presentation.gameover

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.di.ServiceLocator
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.core.theme.*
import com.bubblelevel.pocket.core.ui.*
import com.bubblelevel.pocket.domain.model.CaptureFailure
import com.bubblelevel.pocket.presentation.app.AppEvent

@Composable
fun GameOverRoute(destination: Screen.GameOver, onEvent: (AppEvent) -> Unit) {
    val model: GameOverViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    DisposableEffect(destination.resultId) { model.open(destination.resultId); onDispose { } }
    GameOverScreen(state, onEvent)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onEvent: (AppEvent) -> Unit) {
    val success = state.result.measurement != null
    var reveal by remember { mutableStateOf(false) }
    SideEffect { reveal = true }
    val scale by animateFloatAsState(if (reveal) 1f else 0.9f, spring(dampingRatio = 0.85f, stiffness = 400f), label = "resultReveal")
    val stroke by animateFloatAsState(if (reveal) 1f else 0f, tween(180), label = "checkReveal")
    ToolPage(Modifier.safeDrawingPadding(), BackdropVariant.RESULT) {
        ScreenHeader(R.string.result_title, { onEvent(AppEvent.Back) })
        Spacer(Modifier.height(12.dp))
        Canvas(Modifier.size(80.dp).graphicsLayer { scaleX = scale; scaleY = scale }) {
            drawCircle(White.copy(alpha = 0.9f), size.minDimension / 2f)
            drawCircle(ActionBlue, size.minDimension / 2f - 2.dp.toPx(), style = Stroke(2.dp.toPx()))
            if (success) {
                val a = Offset(size.width * 0.27f, size.height * 0.52f)
                val b = Offset(size.width * 0.44f, size.height * 0.68f)
                val c = Offset(size.width * 0.75f, size.height * 0.32f)
                drawLine(ActionBlue, a, a + (b - a) * stroke, 4.dp.toPx())
                drawLine(ActionBlue, b, b + (c - b) * stroke, 4.dp.toPx())
            } else {
                drawCircle(ActionBlue, 2.5.dp.toPx(), Offset(center.x, size.height * 0.32f))
                drawLine(ActionBlue, Offset(center.x, size.height * 0.45f), Offset(center.x, size.height * 0.7f), 4.dp.toPx())
            }
        }
        val title = if (success) R.string.measurement_saved else if (state.result.failure == CaptureFailure.UNAVAILABLE) R.string.sensor_unavailable else R.string.reading_not_held
        Text(stringResource(title), Modifier.semantics { liveRegion = LiveRegionMode.Polite; heading() }, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        state.result.measurement?.let { measurement ->
            InstrumentCard { MeasurementDetails(measurement, state.unit) }
            Text(stringResource(R.string.data_stays), style = MaterialTheme.typography.bodyMedium)
        }
        if (!success) {
            StatusBadge(R.string.nothing_saved)
            InstrumentCard {
                Text(stringResource(failureResource(state.result.failure)), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
        }
        Text(stringResource(R.string.next_surface), style = MaterialTheme.typography.bodyLarge)
        PrimaryButton(R.string.play_again, { onEvent(AppEvent.Again) })
        SecondaryButton(R.string.menu, { onEvent(AppEvent.Menu) }, Modifier.fillMaxWidth())
        if (success) TextButton(onClick = { onEvent(AppEvent.Navigate(Screen.Readings)) }) { Text(stringResource(R.string.readings)) }
    }
}
