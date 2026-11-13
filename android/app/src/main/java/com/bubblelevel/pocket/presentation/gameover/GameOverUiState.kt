package com.bubblelevel.pocket.presentation.gameover

import com.bubblelevel.pocket.domain.model.*

data class GameOverUiState(val result: MeasurementResult = MeasurementResult(failure = CaptureFailure.UNAVAILABLE), val unit: AngleUnit = AngleUnit.DEGREES)
