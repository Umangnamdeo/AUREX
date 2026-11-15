package com.umang.aurex

import android.app.Application
import com.umang.aurex.data.SettingsRepository

class AurexApp : Application() {
    val repo by lazy { SettingsRepository(this) }
}
