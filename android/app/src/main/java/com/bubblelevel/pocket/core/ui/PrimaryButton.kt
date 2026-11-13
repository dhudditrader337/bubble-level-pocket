package com.bubblelevel.pocket.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.core.theme.*

@Composable
fun PrimaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, if (pressed) tween(100) else spring(dampingRatio = 0.85f, stiffness = 500f), label = "press")
    val shape = RoundedCornerShape(16.dp)
    val active = enabled && !loading
    Button(
        onClick = onClick, enabled = active, interactionSource = interaction, shape = shape,
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (active) 5.dp else 0.dp, shape, ambientColor = ActionBlue.copy(alpha = 0.25f), spotColor = ActionBlue.copy(alpha = 0.3f))
            .background(Brush.verticalGradient(if (active) listOf(ActionBlue, ActionBlue.copy(alpha = 0.90f)) else listOf(VialGreen.copy(alpha = 0.65f), VialGreen.copy(alpha = 0.65f))), shape),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = White, disabledContainerColor = Color.Transparent, disabledContentColor = Ink.copy(alpha = 0.65f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (loading) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Ink); Spacer(Modifier.width(12.dp)) }
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}
