package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SettingRow(titleRes: Int, descriptionRes: Int, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val label = stringResource(titleRes)
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(descriptionRes), style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked, onChange, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = label }, thumbContent = null)
    }
}
