package com.bubblelevel.pocket.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.di.ServiceLocator
import com.bubblelevel.pocket.core.ui.*
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.domain.model.AngleUnit
import com.bubblelevel.pocket.presentation.app.AppEvent

@Composable
fun MenuRoute(onEvent: (AppEvent) -> Unit) {
    val model: MenuViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    MenuScreen(state, onEvent)
}

@Composable
fun MenuScreen(state: MenuUiState, onEvent: (AppEvent) -> Unit) {
    ToolPage(Modifier.safeDrawingPadding(), BackdropVariant.MENU) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { onEvent(AppEvent.Navigate(Screen.Settings)) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.settings)) }
        }
        InstrumentCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Image(painterResource(R.drawable.logo), stringResource(R.string.tool_illustration), Modifier.size(88.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.menu_headline), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.menu_description), style = MaterialTheme.typography.bodyMedium)
                }
            }
            val preview = stringResource(R.string.sample_description)
            Column(Modifier.semantics(mergeDescendants = true) { contentDescription = preview }) {
                Text(stringResource(R.string.sample_preview), style = MaterialTheme.typography.labelSmall)
                EdgeVial(0.0, true, true, Modifier.padding(vertical = 8.dp))
                AngleReadout(state.sampleAngle, AngleUnit.DEGREES, compact = true)
            }
        }
        Text(stringResource(R.string.place_phone), style = MaterialTheme.typography.bodyLarge)
        PrimaryButton(R.string.play, { onEvent(AppEvent.Play) })
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth < 320.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(R.string.calibrate, { onEvent(AppEvent.Navigate(Screen.Calibration())) }, Modifier.fillMaxWidth())
                    SecondaryButton(R.string.readings, { onEvent(AppEvent.Navigate(Screen.Readings)) }, Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(R.string.calibrate, { onEvent(AppEvent.Navigate(Screen.Calibration())) }, Modifier.weight(1f))
                    SecondaryButton(R.string.readings, { onEvent(AppEvent.Navigate(Screen.Readings)) }, Modifier.weight(1f))
                }
            }
        }
        InstrumentCard {
            if (state.loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            Text(state.calibration?.let { stringResource(R.string.reference_dated, AngleFormatter.date(it.timestamp)) } ?: stringResource(if (state.recalibrate) R.string.recalibration_needed else R.string.factory_zero), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.units_summary, stringResource(if (state.unit == AngleUnit.DEGREES) R.string.degrees_name else R.string.percent_name)), style = MaterialTheme.typography.bodyMedium)
            if (state.savedCount > 0) Text(stringResource(R.string.readings_count, state.savedCount))
            if (state.corrupt) {
                StatusBadge(R.string.corrupt_history)
                SecondaryButton(R.string.open_readings, { onEvent(AppEvent.Navigate(Screen.Readings)) })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Image(painterResource(R.drawable.sprite_1), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
            Image(painterResource(R.drawable.sprite_2), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
            Image(painterResource(R.drawable.sprite_3), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
            Image(painterResource(R.drawable.sprite_4), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
            Image(painterResource(R.drawable.sprite_5), null, Modifier.size(40.dp), contentScale = ContentScale.Fit)
        }
        Text(stringResource(R.string.offline_footer), style = MaterialTheme.typography.bodyMedium)
    }
}
