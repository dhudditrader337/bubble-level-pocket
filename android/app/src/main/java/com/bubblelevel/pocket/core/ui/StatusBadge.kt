package com.bubblelevel.pocket.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.theme.*
import com.bubblelevel.pocket.domain.model.*

fun failureResource(failure: CaptureFailure?): Int = when (failure) {
    CaptureFailure.WAITING -> R.string.waiting_sensor
    CaptureFailure.MOVING -> R.string.hold_still
    CaptureFailure.STALE -> R.string.stale_reading
    CaptureFailure.FACE_DOWN -> R.string.screen_up
    CaptureFailure.UPRIGHT -> R.string.screen_upright
    CaptureFailure.UNAVAILABLE -> R.string.sensor_unavailable_hint
    CaptureFailure.CLOSER -> R.string.move_closer
    CaptureFailure.STORAGE -> R.string.setting_failed
    null -> R.string.ready
}

@Composable
fun StatusBadge(labelRes: Int, modifier: Modifier = Modifier) {
    val description = stringResource(labelRes)
    Surface(modifier.semantics { stateDescription = description; liveRegion = LiveRegionMode.Polite }, shape = RoundedCornerShape(10.dp), color = VialGreen.copy(alpha = 0.65f), border = BorderStroke(1.dp, Brass.copy(alpha = 0.4f))) {
        Crossfade(labelRes, animationSpec = tween(150), label = "status") { res -> Text(stringResource(res), Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge) }
    }
}
