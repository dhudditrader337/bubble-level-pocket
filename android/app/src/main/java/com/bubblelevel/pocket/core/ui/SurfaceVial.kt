package com.bubblelevel.pocket.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.core.theme.*
import kotlin.math.*

@Composable
fun SurfaceVial(x: Double, y: Double, valid: Boolean, onLevel: Boolean, rotation: Int = 0, modifier: Modifier = Modifier) {
    val targetX = when (rotation) { 1 -> y; 2 -> -x; 3 -> -y; else -> x }
    val targetY = when (rotation) { 1 -> x; 2 -> y; 3 -> -x; else -> -y }
    val dx by animateFloatAsState(targetX.toFloat(), tween(80), label = "bubbleX")
    val dy by animateFloatAsState(targetY.toFloat(), tween(80), label = "bubbleY")
    Canvas(modifier.widthIn(max = 300.dp).fillMaxWidth().aspectRatio(1f)) {
        val radius = size.minDimension / 2f - 3.dp.toPx()
        val liquid = radius - 10.dp.toPx()
        drawCircle(Brass, radius)
        drawCircle(Ink.copy(alpha = 0.22f), radius, style = Stroke(1.dp.toPx()))
        drawCircle(Brush.radialGradient(listOf(White, VialGreen), center, liquid), liquid)
        drawCircle(Ink.copy(alpha = 0.3f), liquid, style = Stroke(1.dp.toPx()))
        val line = Ink.copy(alpha = 0.25f)
        drawLine(line, Offset(center.x - liquid + 8.dp.toPx(), center.y), Offset(center.x + liquid - 8.dp.toPx(), center.y), 1.dp.toPx())
        drawLine(line, Offset(center.x, center.y - liquid + 8.dp.toPx()), Offset(center.x, center.y + liquid - 8.dp.toPx()), 1.dp.toPx())
        drawCircle(ActionBlue.copy(alpha = 0.8f), 26.dp.toPx(), style = Stroke(if (onLevel) 2.dp.toPx() else 1.dp.toPx()))
        drawCircle(Ink.copy(alpha = 0.18f), 47.dp.toPx(), style = Stroke(1.dp.toPx()))
        for (i in 0 until 36) {
            val angle = i * PI / 18.0
            val inner = radius - if (i % 3 == 0) 7.dp.toPx() else 4.dp.toPx()
            drawLine(Ink.copy(alpha = 0.45f), center + Offset((cos(angle) * inner).toFloat(), (sin(angle) * inner).toFloat()), center + Offset((cos(angle) * (radius - 2.dp.toPx())).toFloat(), (sin(angle) * (radius - 2.dp.toPx())).toFloat()), 1.dp.toPx())
        }
        if (valid) {
            val bubbleRadius = 17.dp.toPx()
            val travel = (liquid - bubbleRadius - 12.dp.toPx()).coerceAtLeast(0f)
            val length = hypot(dx, dy)
            val factor = if (length > 15f) 15f / length else 1f
            val position = center + Offset(dx * factor / 15f * travel, dy * factor / 15f * travel)
            drawCircle(Brush.radialGradient(listOf(White, VialGreen, Brass.copy(alpha = 0.75f)), position - Offset(4.dp.toPx(), 5.dp.toPx()), bubbleRadius * 1.4f), bubbleRadius, position)
            drawCircle(Ink, bubbleRadius, position, style = Stroke(2.dp.toPx()))
            drawCircle(White.copy(alpha = 0.9f), 4.dp.toPx(), position - Offset(5.dp.toPx(), 5.dp.toPx()))
            if (length > 15f) {
                val direction = Offset(dx / length, dy / length)
                val end = center + direction * (liquid - 3.dp.toPx())
                val start = end - direction * 8.dp.toPx()
                val side = Offset(-direction.y, direction.x) * 4.dp.toPx()
                drawLine(ActionBlue, start + side, end, 2.dp.toPx())
                drawLine(ActionBlue, start - side, end, 2.dp.toPx())
            }
        }
    }
}
