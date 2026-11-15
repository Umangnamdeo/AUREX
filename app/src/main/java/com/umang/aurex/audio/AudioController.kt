package com.umang.aurex.audio

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

enum class AudioStream(val label: String, val type: Int) {
    Media("Media", AudioManager.STREAM_MUSIC),
    Ring("Ring", AudioManager.STREAM_RING),
    Notification("Notifications", AudioManager.STREAM_NOTIFICATION),
    Alarm("Alarm", AudioManager.STREAM_ALARM),
    System("System", AudioManager.STREAM_SYSTEM),
}

data class StreamState(val level: Float, val muted: Boolean)

class AudioController(context: Context) {
    private val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val resolver = context.contentResolver

    fun read(s: AudioStream): StreamState {
        val max = am.getStreamMaxVolume(s.type)
        val min = am.getStreamMinVolume(s.type)
        val v = am.getStreamVolume(s.type)
        val level = if (max > min) (v - min).toFloat() / (max - min) else 0f
        return StreamState(level.coerceIn(0f, 1f), am.isStreamMute(s.type))
    }

    fun set(s: AudioStream, fraction: Float) {
        val max = am.getStreamMaxVolume(s.type)
        val min = am.getStreamMinVolume(s.type)
        val v = (min + fraction.coerceIn(0f, 1f) * (max - min)).roundToInt().coerceIn(min, max)
        try { am.setStreamVolume(s.type, v, 0) } catch (_: SecurityException) { /* blocked by Do Not Disturb policy */ }
    }

    fun step(s: AudioStream, up: Boolean) {
        try {
            am.adjustStreamVolume(s.type, if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER, 0)
        } catch (_: SecurityException) { }
    }

    fun toggleMute(s: AudioStream) {
        try { am.adjustStreamVolume(s.type, AudioManager.ADJUST_TOGGLE_MUTE, 0) } catch (_: SecurityException) { }
    }

    fun isMusicActive(): Boolean = am.isMusicActive

    /** True while a call is active or ringing: AUREX then leaves the buttons to the system. */
    fun inCall(): Boolean = am.mode != AudioManager.MODE_NORMAL

    /** Emits the current state, then again whenever any system volume setting changes. */
    fun observe(s: AudioStream): Flow<StreamState> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { trySend(read(s)) }
        }
        resolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        trySend(read(s))
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.distinctUntilChanged()
}
