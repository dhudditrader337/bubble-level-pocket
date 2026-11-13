package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.theme.*

enum class BackdropVariant { SPLASH, MENU, LIVE, HOLD, RESULT, CALIBRATION, READINGS, SETTINGS }

@Composable
fun ToolBackdrop(variant: BackdropVariant, content: @Composable BoxScope.() -> Unit) {
    val colors = when (variant) {
        BackdropVariant.SPLASH -> listOf(Brass.copy(alpha = 0.97f), VialGreen.copy(alpha = 0.94f))
        BackdropVariant.RESULT -> listOf(VialGreen.copy(alpha = 0.96f), Paper.copy(alpha = 0.98f), White.copy(alpha = 0.98f))
        BackdropVariant.LIVE -> listOf(Paper.copy(alpha = 0.97f), Paper.copy(alpha = 0.95f), VialGreen.copy(alpha = 0.95f))
        BackdropVariant.HOLD -> listOf(White.copy(alpha = 0.97f), Paper.copy(alpha = 0.97f), VialGreen.copy(alpha = 0.93f))
        else -> listOf(Paper.copy(alpha = 0.97f), White.copy(alpha = 0.96f), VialGreen.copy(alpha = 0.96f))
    }
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        Canvas(Modifier.fillMaxSize()) {
            val gap = 24.dp.toPx()
            var x = 0f
            while (x < size.width) { drawLine(Ink.copy(alpha = 0.035f), Offset(x, 0f), Offset(x, size.height)); x += gap }
            var y = 0f
            while (y < size.height) { drawLine(Ink.copy(alpha = 0.035f), Offset(0f, y), Offset(size.width, y)); y += gap }
        }
        content()
    }
}

@Composable
fun ToolPage(modifier: Modifier, variant: BackdropVariant, content: @Composable ColumnScope.() -> Unit) {
    ToolBackdrop(variant) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, content = content)
        }
    }
}
