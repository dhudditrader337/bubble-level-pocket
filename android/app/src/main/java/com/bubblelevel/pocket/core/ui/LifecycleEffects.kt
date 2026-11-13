package com.bubblelevel.pocket.core.ui

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bubblelevel.pocket.presentation.app.AppEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Composable
fun ScreenEvents(events: Flow<AppEvent>, active: Boolean, onEvent: (AppEvent) -> Unit) {
    val current by rememberUpdatedState(onEvent)
    val visible by rememberUpdatedState(active)
    LaunchedEffect(Unit) { events.collect { if (visible) current(it) } }
}

@Composable
fun SensorLifecycle(active: Boolean, resume: () -> Unit, pause: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val start by rememberUpdatedState(resume)
    val stop by rememberUpdatedState(pause)
    DisposableEffect(owner, active) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && active) start()
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) stop()
        }
        owner.lifecycle.addObserver(observer)
        if (active && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) start() else stop()
        onDispose { owner.lifecycle.removeObserver(observer); stop() }
    }
}
