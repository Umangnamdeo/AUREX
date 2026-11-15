package com.umang.aurex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.umang.aurex.ui.AurexViewModel
import com.umang.aurex.ui.components.GlassCard
import com.umang.aurex.ui.components.SectionTitle
import com.umang.aurex.ui.components.SwitchRow
import com.umang.aurex.ui.openUpdateLink
import com.umang.aurex.update.UpdateChecker
import com.umang.aurex.update.UpdateState

@Composable
fun UpdatesCard(vm: AurexViewModel, autoCheck: Boolean) {
    val ctx = LocalContext.current
    val state by vm.update.collectAsState()
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("UPDATES")
            Text("Installed version ${UpdateChecker.installedName(ctx)}", style = MaterialTheme.typography.bodyMedium)
            when (val st = state) {
                UpdateState.Idle -> Unit
                UpdateState.Checking -> Text("Checking for updates...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                UpdateState.UpToDate -> Text("You are up to date.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                is UpdateState.Error -> Text(st.message + ". Check your connection and try again.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                is UpdateState.Available -> {
                    Text("Update available: ${st.info.versionName}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    if (st.info.changelog.isNotBlank()) Text(st.info.changelog, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { openUpdateLink(ctx, st.info) }) { Text("Download update") }
                }
            }
            OutlinedButton(onClick = { vm.checkForUpdates(manual = true) }, enabled = state != UpdateState.Checking) { Text("Check for updates") }
            SwitchRow("Check when the app opens", "Shows a popup if a newer version is available", autoCheck) { v -> vm.update { it.copy(autoUpdateCheck = v) } }
        }
    }
}
