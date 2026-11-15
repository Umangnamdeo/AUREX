package com.umang.aurex.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(val x: Float, val y: Float, val speed: Float, val size: Float, val phase: Float)

@Composable
private fun rememberLoopTime(durationMs: Int): Float {
    val t = rememberInfiniteTransition(label = "loop").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMs, easing = LinearEasing), RepeatMode.Restart),
        label = "t",
    )
    return t.value
}

/** Midnight base, drifting radial light, faint metallic arcs and floating particles. */
@Composable
fun AtmosphereBackground(accent: Color, reduced: Boolean, modifier: Modifier = Modifier.fillMaxSize()) {
    val t = if (reduced) 0f else rememberLoopTime(30_000)
    val particles = remember {
        val r = Random(7)
        List(34) { Particle(r.nextFloat(), r.nextFloat(), 0.2f + r.nextFloat() * 0.8f, 0.8f + r.nextFloat() * 2f, r.nextFloat()) }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF040507), Color(0xFF0A0C11), Color(0xFF050608))))
        val a = (t * 2 * PI).toFloat()
        drawCircle(
            Brush.radialGradient(
                listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(w * (0.30f + 0.18f * sin(a)), h * (0.22f + 0.05f * cos(a))), radius = w * 0.95f,
            ),
            radius = w * 0.95f,
            center = Offset(w * (0.30f + 0.18f * sin(a)), h * (0.22f + 0.05f * cos(a))),
        )
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF9AA4B8).copy(alpha = 0.09f), Color.Transparent),
                center = Offset(w * (0.78f - 0.14f * cos(a)), h * 0.72f), radius = w * 0.8f,
            ),
            radius = w * 0.8f,
            center = Offset(w * (0.78f - 0.14f * cos(a)), h * 0.72f),
        )
        rotate(degrees = t * 360f * 0.15f, pivot = Offset(w * 0.5f, h * 0.38f)) {
            drawOval(
                Color.White.copy(alpha = 0.035f),
                topLeft = Offset(-w * 0.2f, h * 0.38f - w * 0.45f), size = Size(w * 1.4f, w * 0.9f),
                style = Stroke(width = 1.2f),
            )
            drawOval(
                accent.copy(alpha = 0.05f),
                topLeft = Offset(-w * 0.05f, h * 0.38f - w * 0.3f), size = Size(w * 1.1f, w * 0.6f),
                style = Stroke(width = 1.2f),
            )
        }
        particles.forEach { p ->
            val yy = (((p.y - t * p.speed * 3f) % 1f) + 1f) % 1f
            val xx = p.x + 0.012f * sin(((t * 2f + p.phase) * 2 * PI).toFloat())
            val tw = 0.5f + 0.5f * sin(((t * 6f + p.phase) * 2 * PI).toFloat())
            drawCircle(Color.White.copy(alpha = 0.10f + 0.28f * tw), p.size * density, Offset(xx * w, yy * h))
        }
    }
}
