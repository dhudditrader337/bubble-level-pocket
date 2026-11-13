package com.bubblelevel.pocket.domain.model

enum class CaptureFailure { WAITING, MOVING, STALE, FACE_DOWN, UPRIGHT, UNAVAILABLE, CLOSER, STORAGE }
data class MeasurementResult(val measurement: Measurement? = null, val failure: CaptureFailure? = null)
data class Evaluation(val reading: LiveReading? = null, val failure: CaptureFailure? = null, val calibrationReady: Boolean = false)
