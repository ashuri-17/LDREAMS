package com.ldreams.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ldreams.app.service.NotificationScheduler
import com.ldreams.app.service.UpdateChecker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class LDreamsApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleDefaultNotifications()
        checkForUpdatesSilently()
    }

    private fun checkForUpdatesSilently() {
        appScope.launch {
            val update = UpdateChecker.checkForUpdate()
            if (update != null && update.isAvailable) {
                val alreadyNotified = getSharedPreferences("ldreams_prefs", MODE_PRIVATE)
                    .getString("last_update_notified", "") == update.latestVersion
                if (!alreadyNotified) {
                    postUpdateNotification(update.latestVersion, update.downloadUrl)
                    getSharedPreferences("ldreams_prefs", MODE_PRIVATE).edit()
                        .putString("last_update_notified", update.latestVersion).apply()
                }
            }
        }
    }

    private fun postUpdateNotification(version: String, downloadUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("LDREAMS Update Available")
            .setContentText("Version $version is ready to download")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(9999, notification)
        } catch (_: SecurityException) {}
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationAudioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alarmAudioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val realityCheckChannel = NotificationChannel(
            CHANNEL_REALITY_CHECK,
            "Reality Checks",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Random reality check reminders throughout the day"
            setSound(notificationSoundUri, notificationAudioAttrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 200)
            enableLights(true)
        }

        val morningReminderChannel = NotificationChannel(
            CHANNEL_MORNING_REMINDER,
            "Morning Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Morning dream recall reminders"
            setSound(alarmSoundUri, alarmAudioAttrs)
            enableVibration(true)
        }

        val bedtimeChannel = NotificationChannel(
            CHANNEL_BEDTIME,
            "Bedtime Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Bedtime lucid dreaming reminders"
            setSound(notificationSoundUri, notificationAudioAttrs)
        }

        val updatesChannel = NotificationChannel(
            CHANNEL_UPDATES,
            "App Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "New version available notifications"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannels(
                listOf(realityCheckChannel, morningReminderChannel, bedtimeChannel, updatesChannel)
            )
        }
    }

    private fun scheduleDefaultNotifications() {
        NotificationScheduler.scheduleRealityChecks(this)
        NotificationScheduler.scheduleMorningReminder(this)
        NotificationScheduler.scheduleBedtimeReminder(this)
    }

    companion object {
        const val CHANNEL_REALITY_CHECK = "reality_checks"
        const val CHANNEL_MORNING_REMINDER = "morning_reminders"
        const val CHANNEL_BEDTIME = "bedtime_reminders"
        const val CHANNEL_UPDATES = "app_updates"
    }
}
