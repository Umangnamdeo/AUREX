package com.umang.aurex.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.HapticLevel
import com.umang.aurex.data.IslandStyle
import com.umang.aurex.data.Side
import com.umang.aurex.ui.AurexViewModel
import com.umang.aurex.ui.components.ColorPickerRow
import com.umang.aurex.ui.components.EdgePopBar
import com.umang.aurex.ui.components.GlassCard
import com.umang.aurex.ui.components.IslandPill
import com.umang.aurex.ui.components.LabeledSlider
import com.umang.aurex.ui.components.SwitchRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val tabs = listOf("Appearance", "Position", "Dimensions", "Animation", "Edge pops", "Haptics")
private const val PREVIEW_SCALE = 0.46f

@Composable
fun CustomizeScreen(vm: AurexViewModel) {
    val s by vm.settings.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var previewLevel by remember { mutableFloatStateOf(0.6f) }
    val previewState = remember { MutableTransitionState(true) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.padding(top = 12.dp)) {
            Text("Customize", style = MaterialTheme.typography.headlineMedium)
            Text("Make it yours. Every change is live.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Interactive phone preview: drag or tap the pill, it is the real IslandPill composable.
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val phone = RoundedCornerShape(34.dp)
            Box(
                Modifier
                    .size(190.dp, 380.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF12151C), Color(0xFF05060A))), phone)
                    .border(2.dp, Color.White.copy(alpha = 0.18f), phone)
            ) {
                Text("Drag the pill", Modifier.align(Alignment.Center), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.25f))
                val right = s.side == Side.Right
                val scaled = s.copy(
                    widthDp = s.widthDp * PREVIEW_SCALE, heightDp = s.heightDp * PREVIEW_SCALE,
                    cornerDp = s.cornerDp * PREVIEW_SCALE,
                    popWidthDp = s.popWidthDp * PREVIEW_SCALE, popHeightDp = s.popHeightDp * PREVIEW_SCALE,
                    popCornerDp = s.popCornerDp * PREVIEW_SCALE,
                )
                val ms = if (s.reducedMotion) 0 else s.animMs
                val edge = if (right) Alignment.CenterEnd else Alignment.CenterStart
                if (s.popsEnabled) {
                    val gap = (s.popHeightDp / 2f + 14f) * PREVIEW_SCALE
                    EdgePopBar(scaled, up = true, modifier = Modifier.align(edge).padding(horizontal = 4.dp).offset(y = (s.verticalOffsetDp * PREVIEW_SCALE - gap).dp))
                    EdgePopBar(scaled, up = false, modifier = Modifier.align(edge).padding(horizontal = 4.dp).offset(y = (s.verticalOffsetDp * PREVIEW_SCALE + gap).dp))
                }
                if (s.islandEnabled) {
                    AnimatedVisibility(
                        visibleState = previewState,
                        modifier = Modifier.align(edge).offset(y = (s.verticalOffsetDp * PREVIEW_SCALE).dp),
                        enter = slideInHorizontally(tween(ms)) { if (right) it else -it } + fadeIn(tween(ms)),
                        exit = slideOutHorizontally(tween(ms)) { if (right) it else -it } + fadeOut(tween(ms)),
                    ) {
                        Box(Modifier.padding(4.dp)) {
                            IslandPill(scaled, previewLevel, previewLevel <= 0.001f, onLevel = { previewLevel = it })
                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { scope.launch { previewState.targetState = false; delay(s.animMs + 250L); previewState.targetState = true } },
            Modifier.fillMaxWidth(),
        ) { Text("Replay animation") }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(tabs) { i, t -> FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(t) }) }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (tab) {
                    0 -> Appearance(s, vm)
                    1 -> Position(s, vm)
                    2 -> Dimensions(s, vm)
                    3 -> Animation(s, vm)
                    4 -> Pops(s, vm)
                    else -> Haptic(s, vm)
                }
            }
        }

        OutlinedButton(onClick = { vm.resetAppearance() }, Modifier.fillMaxWidth()) { Text("Reset to defaults") }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Appearance(s: AurexSettings, vm: AurexViewModel) {
    SwitchRow("Edge Volume Island", "Show the island on volume button presses", s.islandEnabled) { v -> vm.update { it.copy(islandEnabled = v) } }
    Text("Style", style = MaterialTheme.typography.bodyMedium)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IslandStyle.entries.forEach { st ->
            FilterChip(selected = s.style == st, onClick = { vm.update { it.copy(style = st) } }, label = { Text(st.name) })
        }
    }
    ColorPickerRow("Background", s.bg) { c -> vm.update { it.copy(bg = c) } }
    ColorPickerRow("Volume fill", s.fill) { c -> vm.update { it.copy(fill = c) } }
    ColorPickerRow("Icon", s.icon) { c -> vm.update { it.copy(icon = c) } }
    ColorPickerRow("Accent (app and Aurex style)", s.accent) { c -> vm.update { it.copy(accent = c) } }
    LabeledSlider("Transparency (opacity)", s.alpha * 100f, 20f..100f, "%") { v -> vm.update { it.copy(alpha = v / 100f) } }
}

@Composable
private fun Position(s: AurexSettings, vm: AurexViewModel) {
    Text("Side (portrait)", style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Side.entries.forEach { sd -> FilterChip(selected = s.side == sd, onClick = { vm.update { it.copy(side = sd) } }, label = { Text(sd.name) }) }
    }
    Text("Side (landscape)", style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Side.entries.forEach { sd -> FilterChip(selected = s.landscapeSide == sd, onClick = { vm.update { it.copy(landscapeSide = sd) } }, label = { Text(sd.name) }) }
    }
    LabeledSlider("Vertical offset (negative is up)", s.verticalOffsetDp, -300f..300f, "dp") { v -> vm.update { it.copy(verticalOffsetDp = v) } }
}

@Composable
private fun Dimensions(s: AurexSettings, vm: AurexViewModel) {
    LabeledSlider("Width", s.widthDp, 32f..120f, "dp") { v -> vm.update { it.copy(widthDp = v) } }
    LabeledSlider("Height", s.heightDp, 120f..420f, "dp") { v -> vm.update { it.copy(heightDp = v) } }
    LabeledSlider("Corner radius", s.cornerDp, 4f..60f, "dp") { v -> vm.update { it.copy(cornerDp = v) } }
}

@Composable
private fun Animation(s: AurexSettings, vm: AurexViewModel) {
    LabeledSlider("Animation duration", s.animMs.toFloat(), 0f..800f, "ms") { v -> vm.update { it.copy(animMs = v.toInt()) } }
    LabeledSlider("Display time", s.durationMs / 1000f, 0.8f..6f, "s", decimals = 1) { v -> vm.update { it.copy(durationMs = (v * 1000).toInt()) } }
    SwitchRow("Reduce motion", "Removes spring and slide effects and pauses background animation", s.reducedMotion) { v -> vm.update { it.copy(reducedMotion = v) } }
}

@Composable
private fun Pops(s: AurexSettings, vm: AurexViewModel) {
    SwitchRow("Edge pops", "A small bar flashes on the edge for Volume Up and Volume Down", s.popsEnabled) { v -> vm.update { it.copy(popsEnabled = v) } }
    ColorPickerRow("Volume Up color", s.popUpColor) { c -> vm.update { it.copy(popUpColor = c) } }
    ColorPickerRow("Volume Down color", s.popDownColor) { c -> vm.update { it.copy(popDownColor = c) } }
    LabeledSlider("Width", s.popWidthDp, 2f..24f, "dp") { v -> vm.update { it.copy(popWidthDp = v) } }
    LabeledSlider("Height", s.popHeightDp, 24f..240f, "dp") { v -> vm.update { it.copy(popHeightDp = v) } }
    LabeledSlider("Corner radius", s.popCornerDp, 0f..24f, "dp") { v -> vm.update { it.copy(popCornerDp = v) } }
    Text("Pops share the island's side, offset, opacity, animation and display time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Haptic(s: AurexSettings, vm: AurexViewModel) {
    Text("Feedback on volume button presses and island gestures", style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HapticLevel.entries.forEach { h -> FilterChip(selected = s.haptic == h, onClick = { vm.update { it.copy(haptic = h) } }, label = { Text(h.name) }) }
    }
    Text("Devices without a vibration motor ignore this setting.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
