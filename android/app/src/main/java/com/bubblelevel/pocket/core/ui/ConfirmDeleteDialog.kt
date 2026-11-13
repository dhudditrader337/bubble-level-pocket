package com.bubblelevel.pocket.core.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bubblelevel.pocket.R

@Composable
fun ConfirmDeleteDialog(titleRes: Int, messageRes: Int, confirmRes: Int = R.string.delete, busy: Boolean = false, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(stringResource(titleRes)) }, text = { Text(stringResource(messageRes)) },
        confirmButton = { TextButton(onClick = onConfirm, enabled = !busy) { Text(stringResource(confirmRes)) } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(stringResource(R.string.cancel)) } })
}
