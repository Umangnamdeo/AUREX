package com.umang.aurex.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.umang.aurex.audio.AudioController
import com.umang.aurex.ui.components.GlassCard
import com.umang.aurex.ui.components.MixerPanel

@Composable
fun MixerScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val audio = remember { AudioController(ctx) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Column {
                Text("Audio Mixer", style = MaterialTheme.typography.headlineSmall)
                Text("Control every sound", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))
        GlassCard { MixerPanel(audio) }
        Spacer(Modifier.height(12.dp))
        Text(
            "Ring and notification volume can be blocked while Do Not Disturb is on. Grant Do Not Disturb access from Home to change them anyway.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
