package com.bubblelevel.pocket.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubblelevel.pocket.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    private val _state = MutableStateFlow(SplashUiState())
    val state = _state.asStateFlow()
    private var delivered = false
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            _state.update { it.copy(finished = true) }
        }
    }
    fun consume(): Boolean = if (_finished.value && !delivered) { delivered = true; true } else false
}
