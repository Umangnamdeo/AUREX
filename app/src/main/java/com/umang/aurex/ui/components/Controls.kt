package com.umang.aurex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.03f))), shape)
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
            .padding(16.dp)
    ) { content() }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.4.sp)
}

@Composable
fun LabeledSlider(
    label: String, value: Float, range: ClosedFloatingPointRange<Float>, unit: String,
    decimals: Int = 0, onChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            val shown = if (decimals == 0) value.roundToInt().toString() else "%.${decimals}f".format(value)
            Text("$shown$unit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onChange, valueRange = range)
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String? = null, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private val swatches = listOf(
    0xFF000000, 0xFFFFFFFF, 0xFF8E8E93, 0xFF1A1C20, 0xFFDDE1E8, 0xFF6C7BFF, 0xFF8FA6FF,
    0xFF5CF2C8, 0xFF2DA68A, 0xFFFF6B8B, 0xFFFFB86B, 0xFFD0BCFF,
).map { it.toInt() }

/** Palette swatches plus a #RRGGBB field for exact colors. */
@Composable
fun ColorPickerRow(label: String, color: Int, onChange: (Int) -> Unit) {
    var hex by remember(color) { mutableStateOf("#%06X".format(color and 0xFFFFFF)) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(20.dp).background(Color(color), CircleShape).border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(swatches) { c ->
                Box(
                    Modifier
                        .size(30.dp)
                        .background(Color(c), CircleShape)
                        .border(BorderStroke(if (c == color) 2.dp else 1.dp, if (c == color) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f)), CircleShape)
                        .clickable { onChange(c) }
                )
            }
        }
        OutlinedTextField(
            value = hex,
            onValueChange = { v ->
                hex = v
                val clean = v.removePrefix("#")
                if (clean.length == 6) clean.toLongOrNull(16)?.let { onChange((0xFF000000 or it).toInt()) }
            },
            singleLine = true,
            label = { Text("Hex") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
