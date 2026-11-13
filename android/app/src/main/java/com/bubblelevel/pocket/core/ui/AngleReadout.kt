package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.domain.model.AngleUnit

@Composable
fun angleText(angle: Double?, unit: AngleUnit, signed: Boolean = false): String {
    if (angle == null) return stringResource(R.string.no_value)
    if (AngleFormatter.steep(angle, unit)) return stringResource(R.string.steep)
    val value = AngleFormatter.displayed(angle, unit)
    val number = AngleFormatter.number(value)
    val withSign = if (signed && value >= 0.05) stringResource(R.string.positive_value, number) else number
    return stringResource(if (unit == AngleUnit.DEGREES) R.string.degree_value else R.string.percent_value, withSign)
}

@Composable
fun AngleReadout(angle: Double?, unit: AngleUnit, modifier: Modifier = Modifier, signed: Boolean = false, compact: Boolean = false) {
    val text = angleText(angle, unit, signed)
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayLarge, fontSize = if (!compact && text.length > 7) 48.sp else if (compact) 28.sp else 64.sp, textAlign = TextAlign.Center)
        if (angle != null && AngleFormatter.steep(angle, unit)) Text(angleText(angle, AngleUnit.DEGREES, signed), style = MaterialTheme.typography.bodyLarge)
    }
}
