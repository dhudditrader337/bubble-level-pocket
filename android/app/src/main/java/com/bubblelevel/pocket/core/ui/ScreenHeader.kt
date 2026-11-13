package com.bubblelevel.pocket.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R

@Composable
fun ScreenHeader(titleRes: Int, onBack: () -> Unit, enabled: Boolean = true) {
    Column(Modifier.fillMaxWidth()) {
        TextButton(onClick = onBack, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp).align(Alignment.Start)) {
            Image(painterResource(R.drawable.icon_home), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.back))
        }
        Text(stringResource(titleRes), Modifier.fillMaxWidth().semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
    }
}
