package com.bubblelevel.pocket.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.theme.*

@Composable
fun SecondaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false, enabled: Boolean = true) {
    val fill by animateColorAsState(if (selected) VialGreen else White.copy(alpha = 0.92f), tween(150), label = "selection")
    val state = stringResource(if (!enabled) R.string.disabled else if (selected) R.string.selected else R.string.available)
    OutlinedButton(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp).semantics { stateDescription = state },
        shape = RoundedCornerShape(14.dp), border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) ActionBlue else Brass.copy(alpha = 0.55f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = fill, contentColor = Ink, disabledContainerColor = VialGreen.copy(alpha = 0.65f), disabledContentColor = Ink.copy(alpha = 0.65f)),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) { Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge) }
}
