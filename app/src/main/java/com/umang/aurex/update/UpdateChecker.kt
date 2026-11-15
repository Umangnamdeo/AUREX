package com.umang.aurex.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Points at github.com/Umangnamdeo/AUREX (branch main). Change these if the repo or branch is renamed. */
object UpdateConfig {
    const val VERSION_JSON_URL = "https://raw.githubusercontent.com/Umangnamdeo/AUREX/main/version.json"
    const val RELEASES_URL = "https://github.com/Umangnamdeo/AUREX/releases/latest"
}

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseUrl: String,
    val changelog: String,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Error(val message: String) : UpdateState
}

object UpdateChecker {
    fun installedCode(ctx: Context): Long =
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).longVersionCode

    fun installedName(ctx: Context): String =
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?"

    /** Downloads and parses version.json. Throws on network or format errors. */
    suspend fun fetch(): UpdateInfo = withContext(Dispatchers.IO) {
        val conn = URL(UpdateConfig.VERSION_JSON_URL).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.useCaches = false
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            val j = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            UpdateInfo(
                versionCode = j.getInt("versionCode"),
                versionName = j.optString("versionName", j.getInt("versionCode").toString()),
                apkUrl = j.optString("apkUrl", ""),
                releaseUrl = j.optString("releaseUrl", UpdateConfig.RELEASES_URL),
                changelog = j.optString("changelog", ""),
            )
        } finally {
            conn.disconnect()
        }
    }
}
