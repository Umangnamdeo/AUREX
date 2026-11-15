package com.umang.aurex.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.store by preferencesDataStore(name = "aurex")

class SettingsRepository(private val context: Context) {
    private val settingsKey = stringPreferencesKey("settings")
    private val themesKey = stringPreferencesKey("themes")

    val settings: Flow<AurexSettings> = context.store.data.map { p ->
        p[settingsKey]?.let { AurexSettings.fromJson(it) } ?: AurexSettings()
    }

    val customThemes: Flow<List<ThemeSpec>> = context.store.data.map { p -> parseThemes(p[themesKey]) }

    suspend fun write(s: AurexSettings) {
        context.store.edit { it[settingsKey] = s.toJson().toString() }
    }

    suspend fun saveTheme(spec: ThemeSpec) {
        val list = parseThemes(context.store.data.first()[themesKey]).filter { it.id != spec.id } + spec
        context.store.edit { it[themesKey] = encodeThemes(list) }
    }

    suspend fun deleteTheme(id: String) {
        val list = parseThemes(context.store.data.first()[themesKey]).filter { it.id != id }
        context.store.edit { it[themesKey] = encodeThemes(list) }
    }

    suspend fun exportAll(): String {
        val p = context.store.data.first()
        return JSONObject().apply {
            put("app", "AUREX"); put("version", 1)
            put("settings", (p[settingsKey]?.let { AurexSettings.fromJson(it) } ?: AurexSettings()).toJson())
            put("themes", JSONArray(encodeThemes(parseThemes(p[themesKey]))))
        }.toString()
    }

    /** Returns the restored settings, or null if the text is not a valid AUREX backup. */
    suspend fun importAll(text: String): AurexSettings? = runCatching {
        val j = JSONObject(text)
        require(j.optString("app") == "AUREX")
        val s = AurexSettings.fromJson(j.getJSONObject("settings"))
        val themes = parseThemes(j.optJSONArray("themes")?.toString())
        context.store.edit {
            it[settingsKey] = s.toJson().toString()
            it[themesKey] = encodeThemes(themes)
        }
        s
    }.getOrNull()

    private fun parseThemes(raw: String?): List<ThemeSpec> = runCatching {
        val a = JSONArray(raw ?: "[]")
        (0 until a.length()).map { ThemeSpec.fromJson(a.getJSONObject(it)).copy(builtIn = false) }
    }.getOrDefault(emptyList())

    private fun encodeThemes(list: List<ThemeSpec>): String =
        JSONArray().also { arr -> list.forEach { arr.put(it.toJson()) } }.toString()
}
