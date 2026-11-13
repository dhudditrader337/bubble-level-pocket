package com.bubblelevel.pocket.presentation.readings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
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
fun ReadingsRoute(onEvent: (AppEvent) -> Unit) {
    val model: ReadingsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    ReadingsScreen(state, model::filter, model::confirmDelete, model::delete, model::recoverDialog, model::recover, model::retry, onEvent)
}

@Composable
fun ReadingsScreen(state: ReadingsUiState, onFilter: (InstrumentMode?) -> Unit, onDeleteDialog: (Set<String>?) -> Unit, onDelete: () -> Unit, onRecoverDialog: (Boolean) -> Unit, onRecover: () -> Unit, onRetry: () -> Unit, onEvent: (AppEvent) -> Unit) {
    val visible = state.entries.filter { state.filter == null || it.mode == state.filter }
    ToolBackdrop(BackdropVariant.READINGS) {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                item { ScreenHeader(R.string.readings_title, { onEvent(AppEvent.Back) }, !state.busy) }
                if (state.entries.isNotEmpty()) item { Text(stringResource(R.string.readings_count, state.entries.size)) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(R.string.all, { onFilter(null) }, Modifier.weight(1f), state.filter == null)
                        SecondaryButton(R.string.surface, { onFilter(InstrumentMode.SURFACE) }, Modifier.weight(1f), state.filter == InstrumentMode.SURFACE)
                        SecondaryButton(R.string.edge, { onFilter(InstrumentMode.EDGE) }, Modifier.weight(1f), state.filter == InstrumentMode.EDGE)
                    }
                }
                if (state.loading || state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (state.corrupt) item {
                    InstrumentCard {
                        StatusBadge(R.string.corrupt_history)
                        Text(stringResource(R.string.recovery_help))
                        SecondaryButton(R.string.retry, onRetry, enabled = !state.busy)
                        SecondaryButton(R.string.reset_unreadable, { onRecoverDialog(true) }, enabled = !state.busy)
                    }
                }
                if (state.error) item {
                    InstrumentCard {
                        StatusBadge(R.string.delete_failed)
                        SecondaryButton(R.string.retry, onRetry, enabled = !state.busy)
                    }
                }
                if (!state.loading && visible.isEmpty() && !state.corrupt) item {
                    InstrumentCard {
                        if (state.entries.isEmpty()) Image(painterResource(R.drawable.sprite_5), null, Modifier.size(144.dp), contentScale = ContentScale.Fit)
                        Text(stringResource(if (state.entries.isEmpty()) R.string.empty_readings else if (state.filter == InstrumentMode.EDGE) R.string.empty_edge else R.string.empty_surface), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.empty_readings_hint))
                        if (state.filter != null) SecondaryButton(R.string.show_all, { onFilter(null) })
                        PrimaryButton(R.string.play, { onEvent(AppEvent.Play) })
                    }
                }
                items(visible, key = { it.id }) { measurement ->
                    ReadingCard(measurement, state.unit, { onEvent(AppEvent.Navigate(Screen.AngleLock(measurement.id, readOnly = true))) }, { onDeleteDialog(setOf(measurement.id)) }, !state.busy && !state.readOnly)
                }
                if (state.entries.isNotEmpty() && !state.readOnly) item {
                    SecondaryButton(R.string.clear_all, { onDeleteDialog(state.entries.map { it.id }.toSet()) }, Modifier.fillMaxWidth(), enabled = !state.busy)
                }
            }
        }
    }
    if (state.pendingDelete != null) ConfirmDeleteDialog(if (state.pendingDelete.size > 1) R.string.delete_all_title else R.string.delete_reading_title, R.string.delete_reading_message, busy = state.busy, onConfirm = onDelete, onDismiss = { onDeleteDialog(null) })
    if (state.recoverDialog) ConfirmDeleteDialog(R.string.reset_unreadable_title, R.string.reset_unreadable_message, R.string.reset_unreadable, state.busy, onRecover, { onRecoverDialog(false) })
}
