package com.umang.aurex.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.umang.aurex.AurexApp
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.ThemeSpec
import com.umang.aurex.update.UpdateChecker
import com.umang.aurex.update.UpdateState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class AurexViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as AurexApp).repo
    private val _settings = MutableStateFlow(AurexSettings())
    val settings: StateFlow<AurexSettings> = _settings.asStateFlow()
    val customThemes: StateFlow<List<ThemeSpec>> =
        repo.customThemes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var loaded = false
    private var saveJob: Job? = null

    private val _update = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val update: StateFlow<UpdateState> = _update.asStateFlow()
    private val _showUpdatePopup = MutableStateFlow(false)
    val showUpdatePopup: StateFlow<Boolean> = _showUpdatePopup.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = repo.settings.first()
            loaded = true
            // Runs once each time the app is opened (this ViewModel is created per app launch).
            if (_settings.value.autoUpdateCheck) checkForUpdates(manual = false)
        }
    }

    /** manual = true from the Settings button (result shown there); false = launch check (popup if newer). */
    fun checkForUpdates(manual: Boolean) {
        if (_update.value == UpdateState.Checking) return
        _update.value = UpdateState.Checking
        viewModelScope.launch {
            try {
                val info = UpdateChecker.fetch()
                val installed = UpdateChecker.installedCode(getApplication())
                if (info.versionCode > installed) {
                    _update.value = UpdateState.Available(info)
                    if (!manual) _showUpdatePopup.value = true
                } else {
                    _update.value = UpdateState.UpToDate
                }
            } catch (e: Exception) {
                _update.value = UpdateState.Error("Could not reach the update server")
            }
        }
    }

    fun dismissUpdatePopup() { _showUpdatePopup.value = false }

    /** UI updates instantly; the write to DataStore is debounced so dragging sliders stays smooth. */
    fun update(t: (AurexSettings) -> AurexSettings) {
        if (!loaded) return
        _settings.update(t)
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(120)
            repo.write(_settings.value)
        }
    }

    fun applyTheme(spec: ThemeSpec) = update {
        it.copy(themeId = spec.id, style = spec.style, bg = spec.bg, fill = spec.fill,
            icon = spec.icon, accent = spec.accent, alpha = spec.alpha)
    }

    fun resetAppearance() = update { AurexSettings(serviceEnabled = it.serviceEnabled, autoUpdateCheck = it.autoUpdateCheck) }

    private fun fromCurrent(id: String, name: String): ThemeSpec {
        val s = _settings.value
        return ThemeSpec(id, name, "Custom theme", s.style, s.bg, s.fill, s.icon, s.accent, s.alpha, builtIn = false)
    }

    fun createCustomTheme(name: String) {
        val spec = fromCurrent("custom_" + System.currentTimeMillis(), name.ifBlank { "Custom" })
        viewModelScope.launch { repo.saveTheme(spec) }
        update { it.copy(themeId = spec.id) }
    }

    fun updateCustomTheme(old: ThemeSpec) {
        viewModelScope.launch { repo.saveTheme(fromCurrent(old.id, old.name)) }
    }

    fun deleteTheme(id: String) {
        viewModelScope.launch { repo.deleteTheme(id) }
    }

    fun exportTheme(spec: ThemeSpec): String = spec.toJson().toString()

    /** Imports a theme from JSON text; returns false if the text is not a valid theme. */
    fun importTheme(text: String): Boolean = runCatching {
        val spec = ThemeSpec.fromJson(JSONObject(text))
            .copy(id = "custom_" + System.currentTimeMillis(), builtIn = false)
        viewModelScope.launch { repo.saveTheme(spec) }
        true
    }.getOrDefault(false)

    suspend fun exportBackup(): String = repo.exportAll()

    suspend fun importBackup(text: String): Boolean {
        val s = repo.importAll(text) ?: return false
        _settings.value = s
        return true
    }
}
