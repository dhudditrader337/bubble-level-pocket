package com.bubblelevel.pocket.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.domain.model.*
import com.bubblelevel.pocket.domain.repository.SettingsRepository
import com.bubblelevel.pocket.presentation.app.SessionStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameOverViewModel(private val sessions: SessionStore, settings: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(GameOverUiState())
    val state = _state.asStateFlow()
    init { viewModelScope.launch { settings.settings.collect { prefs -> _state.update { it.copy(unit = prefs.unit) } } } }
    fun open(id: String) { _state.update { it.copy(result = sessions.results[id] ?: MeasurementResult(failure = CaptureFailure.UNAVAILABLE)) } }
}
