package com.bubblelevel.pocket.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.core.theme.*

@Composable
fun EdgeVial(angle: Double, valid: Boolean, onLevel: Boolean, modifier: Modifier = Modifier) {
    val target by animateFloatAsState(angle.coerceIn(-10.0, 10.0).toFloat(), tween(80), label = "edgeBubble")
    Canvas(modifier.widthIn(max = 300.dp).fillMaxWidth().height(106.dp)) {
        val corner = CornerRadius(36.dp.toPx())
        drawRoundRect(Brass, cornerRadius = corner)
        val inset = 8.dp.toPx()
        drawRoundRect(Brush.verticalGradient(listOf(White, VialGreen, VialGreen)), Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2), corner)
        var x = 20.dp.toPx()
        while (x < size.width - 16.dp.toPx()) {
            drawLine(Ink.copy(alpha = 0.3f), Offset(x, inset), Offset(x, inset + 8.dp.toPx()), 1.dp.toPx())
            x += 12.dp.toPx()
        }
        for (sign in listOf(-1, 1)) {
            val markerX = center.x + sign * 27.dp.toPx()
            drawLine(if (onLevel) ActionBlue else Ink, Offset(markerX, 17.dp.toPx()), Offset(markerX, size.height - 17.dp.toPx()), if (onLevel) 2.dp.toPx() else 1.dp.toPx())
        }
        if (valid) {
            val radius = 19.dp.toPx()
            val position = Offset(center.x + target / 10f * (size.width / 2f - radius - 20.dp.toPx()).coerceAtLeast(0f), center.y)
            drawCircle(Brush.radialGradient(listOf(White, VialGreen, Brass), position, radius * 1.3f), radius, position)
            drawCircle(Ink, radius, position, style = Stroke(2.dp.toPx()))
            drawCircle(White, 4.dp.toPx(), position - Offset(6.dp.toPx(), 5.dp.toPx()))
        }
    }
}
