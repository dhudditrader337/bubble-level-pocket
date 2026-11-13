package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.domain.model.*

@Composable
fun ModeSelector(mode: InstrumentMode, onMode: (InstrumentMode) -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(R.string.surface, { onMode(InstrumentMode.SURFACE) }, Modifier.weight(1f), mode == InstrumentMode.SURFACE, enabled)
        SecondaryButton(R.string.edge, { onMode(InstrumentMode.EDGE) }, Modifier.weight(1f), mode == InstrumentMode.EDGE, enabled)
    }
}

@Composable
fun UnitSelector(unit: AngleUnit, onUnit: (AngleUnit) -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(R.string.degrees, { onUnit(AngleUnit.DEGREES) }, Modifier.weight(1f), unit == AngleUnit.DEGREES, enabled)
        SecondaryButton(R.string.percent, { onUnit(AngleUnit.PERCENT) }, Modifier.weight(1f), unit == AngleUnit.PERCENT, enabled)
    }
}

@Composable
fun EdgeSelector(edge: EdgeReference, onEdge: (EdgeReference) -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(R.string.short_edge, { onEdge(EdgeReference.SHORT) }, Modifier.weight(1f), edge == EdgeReference.SHORT, enabled)
        SecondaryButton(R.string.long_edge, { onEdge(EdgeReference.LONG) }, Modifier.weight(1f), edge == EdgeReference.LONG, enabled)
    }
    Text(stringResource(if (edge == EdgeReference.SHORT) R.string.short_edge_description else R.string.long_edge_description))
}
