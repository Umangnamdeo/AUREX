package com.umang.aurex.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Home-screen centerpiece. A shaded metallic sphere with two orbit rings projected in real 3D
 * (back half of each ring is drawn behind the sphere, front half in front). It grows with the
 * volume, emits sound-wave rings while music is playing, takes the theme accent, and can be
 * rotated by dragging. Plain Canvas drawing, so it costs little on mid-range GPUs.
 */
@Composable
fun VolumeOrb(level: Float, playing: Boolean, accent: Color, reduced: Boolean, modifier: Modifier = Modifier) {
    val animLevel by animateFloatAsState(level, spring(0.6f, Spring.StiffnessLow), label = "orbLevel")
    val waveStrength by animateFloatAsState(if (playing) 1f else 0f, tween(600), label = "waves")
    val time = if (reduced) 0f else {
        rememberInfiniteTransition(label = "orb").animateFloat(
            0f, 1f, infiniteRepeatable(tween(16_000, easing = LinearEasing), RepeatMode.Restart), label = "orbT",
        ).value
    }
    var yaw by remember { mutableFloatStateOf(0.4f) }
    var tilt by remember { mutableFloatStateOf(0.9f) }

    Canvas(
        modifier.pointerInput(Unit) {
            detectDragGestures { _, d ->
                yaw += d.x * 0.01f
                tilt = (tilt + d.y * 0.005f).coerceIn(0.25f, 1.35f)
            }
        }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = min(size.width, size.height) * 0.19f * (0.78f + 0.5f * animLevel)
        val spin = time * 2f * PI.toFloat()

        // sound waves
        if (waveStrength > 0.01f) {
            for (k in 0..2) {
                val ph = ((time * 5f + k / 3f) % 1f)
                drawCircle(
                    accent.copy(alpha = (1f - ph) * 0.28f * waveStrength),
                    radius = r * (1.15f + ph * 1.2f), center = Offset(cx, cy), style = Stroke(2f),
                )
            }
        }

        drawRing(cx, cy, r * 1.75f, tilt, yaw + spin, accent, back = true, width = 3f)
        drawRing(cx, cy, r * 2.15f, tilt * 0.55f + 0.3f, -yaw * 0.6f - spin * 0.5f, Color(0xFFC3CAD6), back = true, width = 2f)

        // sphere
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFF5F7FB), Color(0xFF8D96A8), Color(0xFF1B1E26), Color(0xFF07080B)),
                center = Offset(cx - r * 0.35f, cy - r * 0.4f), radius = r * 1.35f,
            ),
            radius = r, center = Offset(cx, cy),
        )
        drawCircle(accent.copy(alpha = 0.40f), radius = r, center = Offset(cx, cy), style = Stroke(2f))
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(cx - r * 0.38f, cy - r * 0.42f), radius = r * 0.28f,
            ),
            radius = r * 0.28f, center = Offset(cx - r * 0.38f, cy - r * 0.42f),
        )

        drawRing(cx, cy, r * 1.75f, tilt, yaw + spin, accent, back = false, width = 3f)
        drawRing(cx, cy, r * 2.15f, tilt * 0.55f + 0.3f, -yaw * 0.6f - spin * 0.5f, Color(0xFFC3CAD6), back = false, width = 2f)
    }
}

private fun DrawScope.drawRing(
    cx: Float, cy: Float, radius: Float, tilt: Float, yaw: Float, color: Color, back: Boolean, width: Float,
) {
    val steps = 90
    var prev: Offset? = null
    var prevZ = 0f
    for (i in 0..steps) {
        val a = i / steps.toFloat() * 2f * PI.toFloat()
        val px = radius * cos(a)
        val pz = radius * sin(a)
        // tilt around X, then rotate around Y
        val y1 = -pz * sin(tilt)
        val z1 = pz * cos(tilt)
        val x2 = px * cos(yaw) + z1 * sin(yaw)
        val z2 = -px * sin(yaw) + z1 * cos(yaw)
        val persp = 1f / (1f - z2 / (radius * 4f))
        val pt = Offset(cx + x2 * persp, cy + y1 * persp)
        val p0 = prev
        if (p0 != null) {
            val z = (z2 + prevZ) / 2f
            val front = z >= 0f
            if (front != back) {
                val depth = ((z / radius) + 1f) / 2f
                drawLine(color.copy(alpha = 0.18f + 0.82f * depth), p0, pt, strokeWidth = width * (0.7f + 0.8f * depth))
            }
        }
        prev = pt
        prevZ = z2
    }
}
