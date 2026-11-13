package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.domain.model.MeasurementLabel

@Composable
fun ChoiceDialog(selected: MeasurementLabel, onSelect: (MeasurementLabel) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.choose_label)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MeasurementLabel.entries.forEach { label -> SecondaryButton(labelResource(label), { onSelect(label) }, Modifier.fillMaxWidth().heightIn(min = 56.dp), selected = selected == label) }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
