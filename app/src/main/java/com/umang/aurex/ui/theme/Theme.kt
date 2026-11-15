package com.umang.aurex.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.umang.aurex.data.ThemeSpec

@Composable
fun AurexTheme(accent: Color, content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = accent,
        onPrimary = Color.Black,
        secondary = Color(0xFFB8BEC9),
        background = Color.Black,
        surface = Color(0xFF0C0E12),
        surfaceVariant = Color(0xFF181B21),
        onSurface = Color(0xFFE9ECF2),
        onSurfaceVariant = Color(0xFF9AA1AE),
        outline = Color(0xFF3A3F49),
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

/** Material You pulls its colors from the wallpaper (Android 12+); other themes are returned unchanged. */
fun resolveTheme(ctx: Context, spec: ThemeSpec): ThemeSpec =
    if (spec.id == "material_you" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val cs = dynamicDarkColorScheme(ctx)
        spec.copy(
            bg = cs.surfaceContainerHigh.toArgb(),
            fill = cs.primary.toArgb(),
            icon = cs.onSurfaceVariant.toArgb(),
            accent = cs.primary.toArgb(),
        )
    } else spec
