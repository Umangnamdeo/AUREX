package com.umang.aurex.service

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.umang.aurex.audio.AudioController
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.Side
import com.umang.aurex.ui.components.EdgePopBar
import com.umang.aurex.ui.components.IslandPill
import com.umang.aurex.ui.components.MixerPanel
import com.umang.aurex.ui.theme.AurexTheme

class IslandUi {
    var visible by mutableStateOf(false)
    var level by mutableFloatStateOf(0f)
    var muted by mutableStateOf(false)
    var settings by mutableStateOf(AurexSettings())
    var side by mutableStateOf(Side.Right)
}

class PopUi(val up: Boolean) {
    var visible by mutableStateOf(false)
    var settings by mutableStateOf(AurexSettings())
    var side by mutableStateOf(Side.Right)
}

class MixerUi { var visible by mutableStateOf(false) }

@Composable
fun IslandOverlay(
    ui: IslandUi,
    onLevel: (Float) -> Unit, onInteract: () -> Unit,
    onTap: () -> Unit, onDouble: () -> Unit, onLong: () -> Unit,
) {
    val s = ui.settings
    val state = remember { MutableTransitionState(false) }.also { it.targetState = ui.visible }
    val right = ui.side == Side.Right
    val ms = if (s.reducedMotion) 0 else s.animMs
    val enterSpec: FiniteAnimationSpec<IntOffset> =
        if (s.reducedMotion) snap() else spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
    AnimatedVisibility(
        visibleState = state,
        enter = slideInHorizontally(enterSpec) { if (right) it else -it } + fadeIn(tween(ms)),
        exit = slideOutHorizontally(tween(ms)) { if (right) it else -it } + fadeOut(tween(ms)),
    ) {
        Box(Modifier.padding(6.dp)) {
            IslandPill(
                s = s, level = ui.level, muted = ui.muted,
                onLevel = onLevel, onInteract = onInteract,
                onTap = onTap, onDoubleTap = onDouble, onLongPress = onLong,
            )
        }
    }
}

@Composable
fun PopOverlay(ui: PopUi) {
    val s = ui.settings
    val state = remember { MutableTransitionState(false) }.also { it.targetState = ui.visible }
    val right = ui.side == Side.Right
    val ms = if (s.reducedMotion) 0 else s.animMs
    AnimatedVisibility(
        visibleState = state,
        enter = slideInHorizontally(tween(ms)) { if (right) it else -it } + fadeIn(tween(ms)),
        exit = slideOutHorizontally(tween(ms)) { if (right) it else -it } + fadeOut(tween(ms)),
    ) {
        EdgePopBar(s, ui.up)
    }
}

@Composable
fun MixerOverlay(ui: MixerUi, audio: AudioController, accent: Color, onDismiss: () -> Unit) {
    val state = remember { MutableTransitionState(false) }.also { it.targetState = ui.visible }
    AurexTheme(accent) {
        AnimatedVisibility(visibleState = state, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(visibleState = state, enter = scaleIn(initialScale = 0.9f) + fadeIn(), exit = scaleOut(targetScale = 0.9f) + fadeOut()) {
                    Box(
                        Modifier
                            .width(320.dp)
                            .background(Color(0xE60C0E12), RoundedCornerShape(28.dp))
                            .padding(16.dp)
                            .pointerInput(Unit) { detectTapGestures { } }
                    ) {
                        MixerPanel(audio)
                    }
                }
            }
        }
    }
}
