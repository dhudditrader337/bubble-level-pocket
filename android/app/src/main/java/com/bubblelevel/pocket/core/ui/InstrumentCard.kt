package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.core.theme.*

@Composable
fun InstrumentCard(modifier: Modifier = Modifier, strong: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = White.copy(alpha = 0.94f), border = BorderStroke(if (strong) 2.dp else 1.dp, Brass.copy(alpha = if (strong) 0.6f else 0.25f)), shadowElevation = 3.dp) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
