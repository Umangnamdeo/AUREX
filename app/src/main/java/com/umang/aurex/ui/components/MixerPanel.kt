package com.umang.aurex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.umang.aurex.audio.AudioController
import com.umang.aurex.audio.AudioStream
import com.umang.aurex.audio.StreamState
import kotlin.math.roundToInt

fun AudioStream.icon(): ImageVector = when (this) {
    AudioStream.Media -> Icons.Default.MusicNote
    AudioStream.Ring -> Icons.Default.RingVolume
    AudioStream.Notification -> Icons.Default.Notifications
    AudioStream.Alarm -> Icons.Default.Alarm
    AudioStream.System -> Icons.Default.PhoneAndroid
}

/** Five independent AudioManager streams with live sync. Shared by the app and the overlay. */
@Composable
fun MixerPanel(audio: AudioController, modifier: Modifier = Modifier, onChanged: () -> Unit = {}) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AudioStream.entries.forEach { stream ->
            val flow = remember(stream) { audio.observe(stream) }
            val st by flow.collectAsState(initial = remember(stream) { audio.read(stream) })
            MixerRow(stream, st,
                onLevel = { audio.set(stream, it); onChanged() },
                onMute = { audio.toggleMute(stream); onChanged() })
        }
    }
}

@Composable
private fun MixerRow(stream: AudioStream, st: StreamState, onLevel: (Float) -> Unit, onMute: () -> Unit) {
    var drag by remember { mutableStateOf<Float?>(null) }
    val shown = drag ?: st.level
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMute) {
            Icon(
                if (st.muted) Icons.AutoMirrored.Filled.VolumeOff else stream.icon(),
                contentDescription = "Mute ${stream.label}",
                tint = if (st.muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            )
        }
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stream.label, style = MaterialTheme.typography.bodyMedium)
                Text("${(shown * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Slider(
                value = shown,
                onValueChange = { drag = it; onLevel(it) },
                onValueChangeFinished = { drag = null },
            )
        }
    }
}
