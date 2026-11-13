package com.bubblelevel.pocket.core.di

import android.content.Context
import com.bubblelevel.pocket.data.feedback.AndroidFeedbackController
import com.bubblelevel.pocket.data.local.PreferencesStore
import com.bubblelevel.pocket.data.repository.*
import com.bubblelevel.pocket.data.sensor.AndroidSensorSource
import com.bubblelevel.pocket.data.sample.SampleData
import com.bubblelevel.pocket.domain.feedback.FeedbackController
import com.bubblelevel.pocket.domain.repository.*
import com.bubblelevel.pocket.domain.usecase.SaveMeasurementUseCase
import com.bubblelevel.pocket.presentation.app.SessionStore

object ServiceLocator {
    private var dependencies: Dependencies? = null
    fun initialize(context: Context) {
        if (dependencies == null) dependencies = Dependencies(context.applicationContext)
    }
    val factory: AppViewModelFactory get() = AppViewModelFactory(checkNotNull(dependencies))
}

class Dependencies(context: Context) {
    private val store = PreferencesStore(context)
    val measurements: MeasurementRepository = MeasurementRepositoryImpl(store)
    val settings: SettingsRepository = SettingsRepositoryImpl(store)
    val calibrations: CalibrationRepository = CalibrationRepositoryImpl(store)
    val sensors: SensorRepository = SensorRepositoryImpl(AndroidSensorSource(context))
    val feedback: FeedbackController = AndroidFeedbackController(context)
    val sessions = SessionStore()
    val sampleAngle = SampleData.examples.first().reading.angle
    val epoch: () -> Long = System::currentTimeMillis
    val save = SaveMeasurementUseCase(measurements, epoch)
}
