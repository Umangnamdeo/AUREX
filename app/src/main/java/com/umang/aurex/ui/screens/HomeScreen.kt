package com.umang.aurex.ui.screens

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.umang.aurex.R
import com.umang.aurex.audio.AudioController
import com.umang.aurex.audio.AudioStream
import com.umang.aurex.audio.StreamState
import com.umang.aurex.data.ThemeSpec
import com.umang.aurex.service.AurexAccessibilityService
import com.umang.aurex.ui.AurexViewModel
import com.umang.aurex.ui.components.GlassCard
import com.umang.aurex.ui.components.SectionTitle
import com.umang.aurex.ui.components.VolumeOrb
import kotlinx.coroutines.delay

fun isAccessibilityEnabled(ctx: Context): Boolean {
    val cn = ComponentName(ctx, AurexAccessibilityService::class.java)
    val enabled = AndroidSettings.Secure.getString(ctx.contentResolver, AndroidSettings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    return enabled.split(':').any { it.equals(cn.flattenToString(), true) || it.equals(cn.flattenToShortString(), true) }
}

@Composable
fun HomeScreen(vm: AurexViewModel, onTab: (Int) -> Unit, onMixer: () -> Unit) {
    val ctx = LocalContext.current
    val s by vm.settings.collectAsState()
    val custom by vm.customThemes.collectAsState()
    val audio = remember { AudioController(ctx) }
    val mediaFlow = remember { audio.observe(AudioStream.Media) }
    val media by mediaFlow.collectAsState(initial = StreamState(0f, false))

    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    val a11y = remember(tick) { isAccessibilityEnabled(ctx) }
    val dnd = remember(tick) { (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted }

    // Music-playing check runs only while this screen is on screen (STARTED), every 1.5 s.
    var playing by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { playing = audio.isMusicActive(); delay(1500) }
        }
    }

    var showConsent by remember { mutableStateOf(false) }
    val themeName = (ThemeSpec.builtIns + custom).firstOrNull { it.id == s.themeId }?.name ?: "Custom"

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.aurex_emblem), "AUREX", Modifier.size(width = 150.dp, height = 96.dp), contentScale = ContentScale.Fit)
            Text("AUREX", fontSize = 30.sp, letterSpacing = 10.sp, color = Color(0xFFE4E7EE))
            Text("Customize. Control. Experience.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        }

        VolumeOrb(media.level, playing, Color(s.accent), s.reducedMotion, Modifier.fillMaxWidth().height(280.dp))

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Service", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                !a11y -> "Accessibility access needed"
                                s.serviceEnabled -> "Volume control active"
                                else -> "Paused"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (a11y && s.serviceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    androidx.compose.material3.Switch(checked = s.serviceEnabled, onCheckedChange = { v -> vm.update { it.copy(serviceEnabled = v) } })
                }
                if (!a11y) Button(onClick = { showConsent = true }, Modifier.fillMaxWidth()) { Text("Enable accessibility service") }
            }
        }

        GlassCard {
            Column {
                SectionTitle("LIVE VOLUME")
                Slider(value = media.level, onValueChange = { audio.set(AudioStream.Media, it) })
            }
        }

        GlassCard(Modifier.clickable { onTab(2) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    SectionTitle("CURRENT THEME")
                    Text(themeName, style = MaterialTheme.typography.titleMedium)
                }
                Box(Modifier.size(24.dp).background(Color(s.accent), androidx.compose.foundation.shape.CircleShape))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("Customize", Icons.Default.Tune, Modifier.weight(1f)) { onTab(1) }
            QuickAction("Themes", Icons.Default.Palette, Modifier.weight(1f)) { onTab(2) }
            QuickAction("Mixer", Icons.Default.Equalizer, Modifier.weight(1f)) { onMixer() }
            QuickAction("Settings", Icons.Default.Settings, Modifier.weight(1f)) { onTab(3) }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("PERMISSIONS")
                PermissionRow("Accessibility service", "Detects volume buttons and draws the overlay", a11y)
                PermissionRow("Do Not Disturb access", "Optional. Lets the mixer change ring volume in DND", dnd)
                if (!dnd) OutlinedButton(onClick = { ctx.startActivity(Intent(AndroidSettings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }) { Text("Grant DND access") }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Why accessibility?") },
            text = {
                Text(
                    "Android only lets an accessibility service see physical volume-button presses and draw over other apps. " +
                        "AUREX uses it for exactly that. It does not read your screen, keystrokes or any content, and nothing leaves your device.\n\n" +
                        "On the next screen, open AUREX and turn it on. On Android 13+, you may first need App info, menu, Allow restricted settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConsent = false
                    ctx.startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showConsent = false }) { Text("Not now") } },
        )
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .background2()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, label, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

private fun Modifier.background2(): Modifier = this.then(
    Modifier.background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
)

@Composable
private fun PermissionRow(title: String, subtitle: String, granted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (granted) "Granted" else "Off", color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}
