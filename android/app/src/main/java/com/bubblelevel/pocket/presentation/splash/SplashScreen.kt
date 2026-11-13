package com.bubblelevel.pocket.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubblelevel.pocket.R
import com.bubblelevel.pocket.core.di.ServiceLocator
import com.bubblelevel.pocket.core.theme.*
import com.bubblelevel.pocket.core.ui.*

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = ServiceLocator.factory)
    val finished by model.finished.collectAsStateWithLifecycle()
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished && model.consume()) onFinished() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    var entered by remember { mutableStateOf(false) }
    SideEffect { entered = true }
    val reveal by animateFloatAsState(if (entered) 1f else 0f, tween(450), label = "emblemEntry")
    val loop = rememberInfiniteTransition(label = "loaderDecor")
    val float by loop.animateFloat(-4f, 4f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "float")
    val halo by loop.animateFloat(0.16f, 0.28f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "halo")
    val opening = stringResource(R.string.opening)
    ToolBackdrop(BackdropVariant.SPLASH) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(224.dp).background(Brush.radialGradient(listOf(White.copy(alpha = halo), White.copy(alpha = 0f))), CircleShape).border(1.dp, Ink.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.logo), null, Modifier.size(176.dp).graphicsLayer { translationY = if (state.animationEnabled) float.dp.toPx() else 0f; alpha = reveal; scaleX = 0.94f + reveal * 0.06f; scaleY = scaleX }, contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.app_name), Modifier.widthIn(max = 300.dp).semantics { heading() }, style = MaterialTheme.typography.displaySmall, color = Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodyLarge, color = Ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))
            LinearProgressIndicator(Modifier.width(160.dp).height(4.dp).semantics { contentDescription = opening }, color = ActionBlue, trackColor = White.copy(alpha = 0.7f))
        }
    }
}
