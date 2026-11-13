package com.bubblelevel.pocket.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.core.navigation.Screen
import com.bubblelevel.pocket.domain.repository.MeasurementRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(private val measurements: MeasurementRepository, private val sessions: SessionStore) : ViewModel() {
    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = _screen.asStateFlow()
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()
    private val channel = Channel<AppEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var session = 0L
    init { viewModelScope.launch { measurements.load() } }

    fun splashFinished() {
        if (_screen.value == Screen.Splash) _screen.value = Screen.Menu
    }
    fun busy(value: Boolean) { _state.update { it.copy(busy = value) } }
    fun navigate(screen: Screen) {
        val current = _screen.value
        if (current == screen) return
        _state.update { it.copy(backStack = if (current == Screen.Splash) it.backStack else it.backStack + current) }
        _screen.value = screen
    }
    fun back() {
        if (_state.value.busy) return
        if (_screen.value == Screen.Menu || _screen.value == Screen.Splash) {
            channel.trySend(AppEvent.Finish)
            return
        }
        val stack = _state.value.backStack
        _screen.value = stack.lastOrNull() ?: Screen.Menu
        _state.update { it.copy(backStack = stack.dropLast(1)) }
    }
    fun handle(event: AppEvent) {
        when (event) {
            is AppEvent.Navigate -> navigate(event.screen)
            is AppEvent.Captured -> {
                sessions.captures[event.measurement.id] = event.measurement
                navigate(Screen.AngleLock(event.measurement.id))
            }
            is AppEvent.Result -> {
                val id = UUID.randomUUID().toString()
                event.result.measurement?.let { sessions.captures[it.id] = it }
                sessions.results[id] = event.result
                navigate(Screen.GameOver(id))
            }
            AppEvent.Back -> back()
            AppEvent.Menu -> {
                _state.update { it.copy(backStack = emptyList()) }
                _screen.value = Screen.Menu
            }
            AppEvent.Play -> {
                session++
                navigate(Screen.Game(session, surface = true))
            }
            AppEvent.Again -> {
                session++
                _state.update { it.copy(backStack = listOf(Screen.Menu)) }
                _screen.value = Screen.Game(session)
            }
            AppEvent.Resume -> {
                val stack = _state.value.backStack
                val index = stack.indexOfLast { it is Screen.Game }
                if (index >= 0) {
                    _screen.value = stack[index]
                    _state.update { it.copy(backStack = stack.take(index)) }
                } else back()
            }
            AppEvent.Haptic, AppEvent.Finish -> channel.trySend(event)
        }
    }
}
