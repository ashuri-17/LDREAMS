package com.ldreams.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ldreams.app.ui.screens.AlarmRingingScreen

/**
 * WorkManager worker triggered by a scheduled alarm. It:
 * 1. Starts the full-screen [AlarmRingingScreen] activity
 * 2. Posts a high-priority notification with Snooze / Dismiss actions
 * 3. Returns [Result.success] immediately (the activity owns sound playback)
 */
class AlarmWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val alarmId = inputData.getLong(KEY_ALARM_ID, System.currentTimeMillis())
        val label = inputData.getString(KEY_LABEL) ?: "Alarm"

        // Ensure the alarm notification channel exists
        ensureNotificationChannel()

        // Launch the full-screen alarm activity
        val activityIntent = Intent(applicationContext, AlarmRingingScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(KEY_ALARM_ID, alarmId)
            putExtra(KEY_LABEL, label)
        }
        applicationContext.startActivity(activityIntent)

        // Post a high-priority notification with action buttons
        val notification = buildAlarmNotification(alarmId, label, activityIntent)
        val notificationId = (alarmId % 999_999L).toInt() + 3_000_000
        notify(notificationId, notification)

        return Result.success()
    }

    // ------------------------------------------------------------------ //
    //  Notification helpers
    // ------------------------------------------------------------------ //

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ALARM,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled alarm notifications"
                enableVibration(true)
                setSound(null, null) // Sound handled by RingtoneManager in the activity
                enableLights(true)
            }
            val manager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildAlarmNotification(
        alarmId: Long,
        label: String,
        activityIntent: Intent
    ): NotificationCompat.Builder {
        // Stable, positive Int request codes derived from the alarm ID
        val snoozeCode = (alarmId % 999_999L).toInt() + 1        // 1 … 999_999
        val dismissCode = snoozeCode + 1_000_000                  // 1_000_001 … 1_999_999
        val activityCode = dismissCode + 1_000_000                // 2_000_001 … 2_999_999

        val fullScreenIntent = PendingIntent.getActivity(
            applicationContext,
            activityCode,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(applicationContext, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE
            putExtra(KEY_ALARM_ID, alarmId)
            putExtra(KEY_LABEL, label)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            snoozeCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(applicationContext, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS
            putExtra(KEY_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            dismissCode,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("LDREAMS Alarm")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Snooze (10m)",
                snoozePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                dismissPendingIntent
            )
    }

    private fun notify(id: Int, builder: NotificationCompat.Builder) {
        // On API 33+ check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(applicationContext).notify(id, builder.build())
            }
        } else {
            NotificationManagerCompat.from(applicationContext).notify(id, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ALARM = "ldreams_alarms"

        const val KEY_ALARM_ID = "alarm_id"
        const val KEY_LABEL = "label"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"

        // Tag prefix for alarm WorkManager work
        const val TAG_ALARM = "ldreams_alarm"
    }
}
