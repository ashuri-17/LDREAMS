package com.ldreams.app.service

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Singleton object to manage alarm sound playback across activities,
 * workers, and broadcast receivers. Holds a reference to the currently
 * playing Ringtone so it can be stopped from any context (e.g. notification
 * action BroadcastReceiver).
 */
object AlarmSoundManager {

    private var ringtone: Ringtone? = null
    private var isPlaying = false

    /**
     * Start playing the default alarm ringtone in a loop.
     */
    fun play(context: Context) {
        if (isPlaying) return
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmUri).also {
                it.isLooping = true
                it.play()
            }
            isPlaying = true
        } catch (e: Exception) {
            // Fallback to notification sound if alarm sound unavailable
            try {
                val notificationUri: Uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ringtone = RingtoneManager.getRingtone(context, notificationUri).also {
                    it.isLooping = true
                    it.play()
                }
                isPlaying = true
            } catch (e2: Exception) {
                isPlaying = false
            }
        }
    }

    /**
     * Start vibrator in a pattern for alarm haptic feedback.
     */
    fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 500, 500, 1000),
                        intArrayOf(0, 255, 0, 255, 0),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 500, 500), -1)
            }
        } catch (_: Exception) {
            // Vibrator not available
        }
    }

    /**
     * Stop the currently playing alarm sound.
     */
    fun stop() {
        ringtone?.stop()
        ringtone = null
        isPlaying = false
    }

    /**
     * Flag set when the notification "Dismiss" action fires while the
     * [AlarmRingingScreen] might be visible. The activity observes this
     * flag and finishes itself.
     */
    @Volatile
    var dismissedRemotely = false

    /** Mark the alarm as dismissed from a remote action (notification). */
    fun markDismissedFromRemote() {
        dismissedRemotely = true
    }

    /** Clear the remote-dismiss flag after the activity has finished. */
    fun resetRemoteDismiss() {
        dismissedRemotely = false
    }

    fun isAlarmPlaying(): Boolean = isPlaying
}
