package com.umang.aurex.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.umang.aurex.ui.components.AtmosphereBackground
import com.umang.aurex.ui.screens.CustomizeScreen
import com.umang.aurex.ui.screens.HomeScreen
import com.umang.aurex.ui.screens.MixerScreen
import com.umang.aurex.ui.screens.SettingsScreen
import com.umang.aurex.ui.screens.ThemesScreen
import com.umang.aurex.ui.theme.AurexTheme

private const val MIXER_PAGE = 4

@Composable
fun AurexRoot(vm: AurexViewModel) {
    val s by vm.settings.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var mixer by rememberSaveable { mutableStateOf(false) }
    val page = if (mixer) MIXER_PAGE else tab

    AurexTheme(Color(s.accent)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AtmosphereBackground(Color(s.accent), s.reducedMotion)
            BackHandler(enabled = mixer || tab != 0) { if (mixer) mixer = false else tab = 0 }
            Column(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = page,
                    modifier = Modifier.weight(1f).statusBarsPadding(),
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (fadeIn(tween(320)) + slideInHorizontally(tween(320)) { it / 8 * dir }) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally(tween(320)) { -it / 8 * dir })
                    },
                    label = "pages",
                ) { p ->
                    when (p) {
                        0 -> HomeScreen(vm, onTab = { tab = it }, onMixer = { mixer = true })
                        1 -> CustomizeScreen(vm)
                        2 -> ThemesScreen(vm)
                        3 -> SettingsScreen(vm)
                        else -> MixerScreen(onBack = { mixer = false })
                    }
                }
                NavBar(selected = tab, onSelect = { tab = it; mixer = false })
            }
            UpdateDialog(vm)
        }
    }
}

@Composable
private fun NavBar(selected: Int, onSelect: (Int) -> Unit) {
    val items: List<Pair<String, ImageVector>> = listOf(
        "Home" to Icons.Default.Home, "Customize" to Icons.Default.Tune,
        "Themes" to Icons.Default.Palette, "Settings" to Icons.Default.Settings,
    )
    val shape = RoundedCornerShape(28.dp)
    Row(
        Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
            .background(Color(0xCC0E1015), shape)
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEachIndexed { i, (label, icon) ->
            val sel = i == selected
            Column(
                Modifier
                    .weight(1f)
                    .background(if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent, RoundedCornerShape(22.dp))
                    .clickable { onSelect(i) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(icon, label, tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
