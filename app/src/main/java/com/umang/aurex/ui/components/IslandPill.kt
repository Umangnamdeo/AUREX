package com.umang.aurex.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.IslandStyle

/**
 * The Edge Volume Island: a vertical pill with a rising fill. Used both by the real overlay
 * and by the live previews, so what you see in the studio is what the overlay draws.
 */
@Composable
fun IslandPill(
    s: AurexSettings,
    level: Float,
    muted: Boolean,
    modifier: Modifier = Modifier,
    onLevel: (Float) -> Unit = {},
    onInteract: () -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
) {
    val shape = RoundedCornerShape(s.cornerDp.dp)
    val pad = if (s.style == IslandStyle.Minimal) 3f else 4f
    val innerShape = RoundedCornerShape((s.cornerDp - pad).coerceAtLeast(2f).dp)
    val spec: AnimationSpec<Float> =
        if (s.reducedMotion) snap() else spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
    val shown by animateFloatAsState(level.coerceIn(0f, 1f), spec, label = "islandFill")

    val bg = Color(s.bg)
    val fill = Color(s.fill)
    val container: Brush = when (s.style) {
        IslandStyle.Classic, IslandStyle.Minimal -> SolidColor(bg)
        IslandStyle.Glass -> Brush.verticalGradient(
            listOf(
                lerp(bg, Color.White, 0.55f).copy(alpha = 0.32f),
                lerp(bg, Color.White, 0.35f).copy(alpha = 0.14f),
            )
        )
        IslandStyle.Aurex -> Brush.verticalGradient(listOf(lerp(bg, Color(0xFF3A4150), 0.55f), bg))
    }
    val fillBrush: Brush = when (s.style) {
        IslandStyle.Classic, IslandStyle.Minimal -> SolidColor(fill)
        IslandStyle.Glass -> Brush.verticalGradient(listOf(fill.copy(alpha = 0.85f), fill.copy(alpha = 0.55f)))
        IslandStyle.Aurex -> Brush.verticalGradient(listOf(fill, lerp(fill, Color(0xFF5C7CFF), 0.6f)))
    }
    val borderColor: Color? = when (s.style) {
        IslandStyle.Glass -> Color.White.copy(alpha = 0.30f)
        IslandStyle.Aurex -> Color(0xFF5C7CFF).copy(alpha = 0.50f)
        else -> null
    }

    val onLevelState by rememberUpdatedState(onLevel)
    val onInteractState by rememberUpdatedState(onInteract)
    val onTapState by rememberUpdatedState(onTap)
    val onDoubleState by rememberUpdatedState(onDoubleTap)
    val onLongState by rememberUpdatedState(onLongPress)

    Box(
        modifier
            .size(s.widthDp.dp, s.heightDp.dp)
            .alpha(s.alpha)
            .shadow(if (s.style == IslandStyle.Minimal) 0.dp else 8.dp, shape, clip = false)
            .background(container, shape)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onInteractState() },
                ) { change, _ ->
                    change.consume()
                    onLevelState((1f - change.position.y / size.height).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { o ->
                    onInteractState()
                    onLevelState((1f - o.y / size.height).coerceIn(0f, 1f))
                }
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad.dp)
                .clip(innerShape)
        ) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(shown.coerceIn(0f, 1f))
                    .background(fillBrush)
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .size((s.widthDp - pad * 2).dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onTapState() },
                            onDoubleTap = { onDoubleState() },
                            onLongPress = { onLongState() },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (muted || level <= 0.001f) Icons.AutoMirrored.Filled.VolumeOff
                    else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color(s.icon),
                    modifier = Modifier.size((s.widthDp * 0.42f).dp),
                )
            }
        }
    }
}

@Composable
fun EdgePopBar(s: AurexSettings, up: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(s.popWidthDp.dp, s.popHeightDp.dp)
            .alpha(s.alpha)
            .background(Color(if (up) s.popUpColor else s.popDownColor), RoundedCornerShape(s.popCornerDp.dp))
    )
}
