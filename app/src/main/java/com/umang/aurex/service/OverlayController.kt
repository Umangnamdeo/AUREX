package com.umang.aurex.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import com.umang.aurex.audio.AudioController
import com.umang.aurex.audio.AudioStream
import com.umang.aurex.audio.Haptics
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.GestureAction
import com.umang.aurex.data.Side
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Owns the three overlay windows (island, edge pops, mixer). All windows use
 * TYPE_ACCESSIBILITY_OVERLAY, which an enabled accessibility service may draw without the
 * separate "display over other apps" permission.
 */
class OverlayController(
    private val ctx: Context,
    owner: OverlayOwner,
    private val audio: AudioController,
    private val haptics: Haptics,
    private val currentSettings: () -> AurexSettings,
) {
    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val stream = AudioStream.Media
    private val density get() = ctx.resources.displayMetrics.density

    private val islandUi = IslandUi()
    private val popUpUi = PopUi(up = true)
    private val popDownUi = PopUi(up = false)
    private val mixerUi = MixerUi()

    private inner class Win(val view: View) {
        var added = false
        var job: Job? = null
        fun show(lp: WindowManager.LayoutParams) {
            if (added) wm.updateViewLayout(view, lp) else { wm.addView(view, lp); added = true }
        }
        fun remove() {
            if (added) { runCatching { wm.removeView(view) }; added = false }
        }
    }

    private val islandWin = Win(overlayView(ctx, owner) {
        IslandOverlay(
            islandUi,
            onLevel = { f -> audio.set(stream, f); sync(); scheduleIsland() },
            onInteract = { scheduleIsland() },
            onTap = { perform(currentSettings().tapAction) },
            onDouble = { perform(currentSettings().doubleTapAction) },
            onLong = { perform(currentSettings().longPressAction) },
        )
    })
    private val popUpWin = Win(overlayView(ctx, owner) { PopOverlay(popUpUi) })
    private val popDownWin = Win(overlayView(ctx, owner) { PopOverlay(popDownUi) })
    private val mixerWin = Win(overlayView(ctx, owner) {
        MixerOverlay(mixerUi, audio, Color(currentSettings().accent)) { hideMixer() }
    })

    init {
        // Keep the island in step with volume changes made elsewhere while it is on screen.
        audio.observe(stream).onEach { st ->
            if (islandUi.visible) { islandUi.level = st.level; islandUi.muted = st.muted }
        }.launchIn(scope)
    }

    private fun lp(w: Int, h: Int, gravity: Int, y: Int, touchable: Boolean, fullscreen: Boolean = false): WindowManager.LayoutParams {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (!touchable) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        if (fullscreen) flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        return WindowManager.LayoutParams(
            w, h, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, flags, PixelFormat.TRANSLUCENT,
        ).also { it.gravity = gravity; it.y = y }
    }

    private fun sync() {
        val st = audio.read(stream)
        islandUi.level = st.level
        islandUi.muted = st.muted
    }

    fun showIsland(s: AurexSettings, side: Side) {
        if (mixerUi.visible) return
        islandUi.settings = s
        islandUi.side = side
        sync()
        val g = (if (side == Side.Right) Gravity.END else Gravity.START) or Gravity.CENTER_VERTICAL
        islandWin.show(lp(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, g, (s.verticalOffsetDp * density).toInt(), true))
        islandUi.visible = true
        scheduleIsland()
    }

    private fun scheduleIsland() {
        islandWin.job?.cancel()
        islandWin.job = scope.launch {
            val s = islandUi.settings
            delay(s.durationMs.toLong())
            islandUi.visible = false
            delay(s.animMs + 150L)
            islandWin.remove()
        }
    }

    fun showPop(up: Boolean, s: AurexSettings, side: Side) {
        val ui = if (up) popUpUi else popDownUi
        val win = if (up) popUpWin else popDownWin
        ui.settings = s
        ui.side = side
        val g = (if (side == Side.Right) Gravity.END else Gravity.START) or Gravity.CENTER_VERTICAL
        val gap = ((s.popHeightDp / 2f + 14f) * density).toInt()
        val y = (s.verticalOffsetDp * density).toInt() + if (up) -gap else gap
        win.show(lp(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, g, y, false))
        ui.visible = true
        win.job?.cancel()
        win.job = scope.launch {
            delay(s.durationMs.toLong())
            ui.visible = false
            delay(s.animMs + 150L)
            win.remove()
        }
    }

    fun showMixer() {
        islandWin.job?.cancel()
        islandUi.visible = false
        scope.launch { delay(islandUi.settings.animMs + 100L); islandWin.remove() }
        mixerWin.show(lp(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER, 0, true, fullscreen = true))
        mixerUi.visible = true
    }

    fun hideMixer() {
        mixerUi.visible = false
        mixerWin.job?.cancel()
        mixerWin.job = scope.launch { delay(300); mixerWin.remove() }
    }

    private fun perform(action: GestureAction) {
        haptics.play(currentSettings().haptic)
        when (action) {
            GestureAction.None -> Unit
            GestureAction.Mute -> audio.toggleMute(stream)
            GestureAction.Max -> audio.set(stream, 1f)
            GestureAction.Min -> audio.set(stream, 0f)
            GestureAction.Mixer -> { showMixer(); return }
        }
        sync()
        scheduleIsland()
    }

    fun destroy() {
        scope.cancel()
        islandWin.remove(); popUpWin.remove(); popDownWin.remove(); mixerWin.remove()
    }
}
