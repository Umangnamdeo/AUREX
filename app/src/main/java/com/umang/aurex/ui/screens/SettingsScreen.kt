package com.umang.aurex.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.umang.aurex.R
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.GestureAction
import com.umang.aurex.data.HapticLevel
import com.umang.aurex.ui.AurexViewModel
import com.umang.aurex.ui.components.GlassCard
import com.umang.aurex.ui.components.SectionTitle
import com.umang.aurex.ui.components.SwitchRow
import com.umang.aurex.update.UpdateChecker
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AurexViewModel) {
    val ctx = LocalContext.current
    val s by vm.settings.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    val a11y = remember(tick) { isAccessibilityEnabled(ctx) }
    var showLicenses by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.padding(top = 12.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Text("Fine-tune your experience", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("ACCESSIBILITY AND PERMISSIONS")
                Text(if (a11y) "Accessibility service is on." else "Accessibility service is off. AUREX cannot see volume buttons until you enable it.", style = MaterialTheme.typography.bodyMedium)
                Text("Used only to detect volume-button presses and draw the overlay. No screen content is read.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }) { Text("Open accessibility settings") }
                OutlinedButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }) { Text("Do Not Disturb access (optional)") }
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("HAPTICS")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HapticLevel.entries.forEach { h -> FilterChip(selected = s.haptic == h, onClick = { vm.update { it.copy(haptic = h) } }, label = { Text(h.name) }) }
                }
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("LANDSCAPE")
                SwitchRow("Edge Volume Island in landscape", null, s.landscapeIsland) { v -> vm.update { it.copy(landscapeIsland = v) } }
                SwitchRow("Edge pops in landscape", null, s.landscapePops) { v -> vm.update { it.copy(landscapePops = v) } }
                Text("Landscape side is set in Customize, then Position.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("ISLAND GESTURES (ON THE SPEAKER ICON)")
                GestureRow("Single tap", s.tapAction) { a -> vm.update { it.copy(tapAction = a) } }
                GestureRow("Double tap", s.doubleTapAction) { a -> vm.update { it.copy(doubleTapAction = a) } }
                GestureRow("Long press", s.longPressAction) { a -> vm.update { it.copy(longPressAction = a) } }
                Text("Drag on the pill sets volume. Tap outside the mixer to dismiss it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("MOTION")
                SwitchRow("Reduce motion", "Static background, no springs or slides", s.reducedMotion) { v -> vm.update { it.copy(reducedMotion = v) } }
            }
        }

        UpdatesCard(vm, s.autoUpdateCheck)

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("BACKUP AND RESTORE")
                Text("Copies all settings and custom themes as text. Paste it back later or on another phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            clipboard.setText(AnnotatedString(vm.exportBackup()))
                            Toast.makeText(ctx, "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.weight(1f)) { Text("Back up") }
                    OutlinedButton(onClick = {
                        scope.launch {
                            val ok = vm.importBackup(clipboard.getText()?.text.orEmpty())
                            Toast.makeText(ctx, if (ok) "Backup restored" else "Clipboard does not contain an AUREX backup", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.weight(1f)) { Text("Restore") }
                }
            }
        }

        GlassCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Image(painterResource(R.drawable.aurex_emblem), "AUREX", Modifier.size(width = 130.dp, height = 84.dp), contentScale = ContentScale.Fit)
                Text("AUREX", fontSize = 24.sp, letterSpacing = 8.sp)
                Text("Customize. Control. Experience.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Version ${UpdateChecker.installedName(ctx)}  (com.umang.aurex)", style = MaterialTheme.typography.labelMedium)
                Text("Open source. Built with Kotlin and Jetpack Compose.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Created by Umangnamdeo", style = MaterialTheme.typography.labelMedium)
                Text("Licensed under Apache 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { showLicenses = true }) { Text("Open-source licenses") }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text("Open-source licenses") },
            text = {
                Text(
                    "AUREX is Copyright 2026 Umangnamdeo, licensed under the Apache License 2.0.\n\nIt uses these libraries, all under the Apache License 2.0:\n\n" +
                        "Kotlin and kotlinx.coroutines (JetBrains)\n" +
                        "AndroidX Core, Activity, Lifecycle, DataStore, SplashScreen (Google)\n" +
                        "Jetpack Compose and Material 3 (Google)\n\n" +
                        "License text: https://www.apache.org/licenses/LICENSE-2.0",
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text("Close") } },
        )
    }
}

@Composable
private fun GestureRow(label: String, value: GestureAction, onPick: (GestureAction) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Column {
            OutlinedButton(onClick = { open = true }) { Text(value.label) }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                GestureAction.entries.forEach { a ->
                    DropdownMenuItem(text = { Text(a.label) }, onClick = { onPick(a); open = false })
                }
            }
        }
    }
}
