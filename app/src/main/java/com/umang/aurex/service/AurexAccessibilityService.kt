package com.umang.aurex.service

import android.accessibilityservice.AccessibilityService
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.umang.aurex.AurexApp
import com.umang.aurex.audio.AudioController
import com.umang.aurex.audio.AudioStream
import com.umang.aurex.audio.Haptics
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.Side
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Receives physical volume-key events (the only thing it asks the system for), adjusts the
 * media volume itself, and shows the AUREX overlays. It ignores all accessibility events:
 * it never reads screen content.
 */
class AurexAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val owner = OverlayOwner()
    private var overlays: OverlayController? = null
    private var audio: AudioController? = null
    private var haptics: Haptics? = null
    @Volatile private var current = AurexSettings()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val a = AudioController(this)
        val h = Haptics(this)
        audio = a
        haptics = h
        owner.resume()
        overlays = OverlayController(this, owner, a, h) { current }
        (application as AurexApp).repo.settings.onEach { current = it }.launchIn(scope)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode
        if (code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN) return false
        val a = audio ?: return false
        val o = overlays ?: return false
        val s = current
        if (!s.serviceEnabled || a.inCall()) return false

        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val island = s.islandEnabled && (!landscape || s.landscapeIsland)
        val pops = s.popsEnabled && (!landscape || s.landscapePops)
        if (!island && !pops) return false

        if (event.action == KeyEvent.ACTION_DOWN) {
            val up = code == KeyEvent.KEYCODE_VOLUME_UP
            a.step(AudioStream.Media, up)
            haptics?.play(s.haptic)
            val side: Side = if (landscape) s.landscapeSide else s.side
            if (island) o.showIsland(s, side)
            if (pops) o.showPop(up, s, side)
        }
        return true // consume both down and up so the system panel does not also appear
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        overlays?.destroy()
        owner.destroy()
        scope.cancel()
        super.onDestroy()
    }
}
