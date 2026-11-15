package com.umang.aurex.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.umang.aurex.data.HapticLevel

class Haptics(ctx: Context) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun play(level: HapticLevel) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val (ms, amp) = when (level) {
            HapticLevel.Off -> return
            HapticLevel.Light -> 12L to 50
            HapticLevel.Medium -> 22L to 120
            HapticLevel.Strong -> 38L to 255
        }
        val amplitude = if (v.hasAmplitudeControl()) amp else VibrationEffect.DEFAULT_AMPLITUDE
        v.vibrate(VibrationEffect.createOneShot(ms, amplitude))
    }
}
