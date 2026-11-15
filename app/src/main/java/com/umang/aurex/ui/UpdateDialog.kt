package com.umang.aurex.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.umang.aurex.update.UpdateChecker
import com.umang.aurex.update.UpdateInfo
import com.umang.aurex.update.UpdateState

fun openUpdateLink(ctx: Context, info: UpdateInfo) {
    val url = info.apkUrl.ifBlank { info.releaseUrl }
    runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** Shown on app launch when a newer version is published in version.json. */
@Composable
fun UpdateDialog(vm: AurexViewModel) {
    val show by vm.showUpdatePopup.collectAsState()
    val state by vm.update.collectAsState()
    val ctx = LocalContext.current
    val info = (state as? UpdateState.Available)?.info
    if (!show || info == null) return
    AlertDialog(
        onDismissRequest = { vm.dismissUpdatePopup() },
        title = { Text("Update available") },
        text = {
            Text(
                "AUREX ${info.versionName} is available. You have ${UpdateChecker.installedName(ctx)}." +
                    if (info.changelog.isNotBlank()) "\n\n${info.changelog}" else ""
            )
        },
        confirmButton = {
            TextButton(onClick = { vm.dismissUpdatePopup(); openUpdateLink(ctx, info) }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = { vm.dismissUpdatePopup() }) { Text("Later") } },
    )
}
