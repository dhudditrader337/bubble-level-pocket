package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.domain.model.*

fun labelResource(label: MeasurementLabel): Int = when (label) {
    MeasurementLabel.MEASUREMENT -> R.string.label_measurement
    MeasurementLabel.SHELF -> R.string.label_shelf
    MeasurementLabel.FRAME -> R.string.label_frame
    MeasurementLabel.WORKTOP -> R.string.label_worktop
    MeasurementLabel.REFERENCE -> R.string.label_reference
}

@Composable
fun MeasurementDetails(measurement: Measurement, unit: AngleUnit) {
    Text(stringResource(labelResource(measurement.label)), style = MaterialTheme.typography.titleLarge)
    AngleReadout(measurement.reading.angle, unit, signed = measurement.mode == InstrumentMode.EDGE)
    Text(stringResource(if (measurement.mode == InstrumentMode.SURFACE) R.string.surface_level else R.string.edge_level), style = MaterialTheme.typography.bodyLarge)
    if (measurement.mode == InstrumentMode.SURFACE) {
        Text(stringResource(R.string.axis_values, angleText(measurement.reading.x, unit, true), angleText(measurement.reading.y, unit, true)))
    } else Text(stringResource(if (measurement.edge == EdgeReference.SHORT) R.string.short_edge_description else R.string.long_edge_description))
    Text(stringResource(R.string.captured_on, AngleFormatter.date(measurement.capturedAt)), style = MaterialTheme.typography.bodyMedium)
    Text(measurement.reading.calibration?.let { stringResource(R.string.reference_dated, AngleFormatter.date(it.timestamp)) } ?: stringResource(R.string.factory_zero), style = MaterialTheme.typography.bodyMedium)
    Text(stringResource(R.string.original_unit, stringResource(if (measurement.originalUnit == AngleUnit.DEGREES) R.string.degrees_name else R.string.percent_name)), style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun ReadingCard(measurement: Measurement, unit: AngleUnit, onOpen: () -> Unit, onDelete: () -> Unit, enabled: Boolean) {
    InstrumentCard {
        Text(stringResource(labelResource(measurement.label)), style = MaterialTheme.typography.titleLarge)
        AngleReadout(measurement.reading.angle, unit, signed = measurement.mode == InstrumentMode.EDGE, compact = true)
        Text(stringResource(if (measurement.mode == InstrumentMode.SURFACE) R.string.surface_level else R.string.edge_level))
        Text(stringResource(R.string.captured_on, AngleFormatter.date(measurement.capturedAt)), style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(R.string.open, onOpen, Modifier.weight(1f))
            SecondaryButton(R.string.delete, onDelete, Modifier.weight(1f), enabled = enabled)
        }
    }
}
