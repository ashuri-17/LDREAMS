package com.ldreams.app

import android.app.Application
import android.media.AudioAttributes
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.os.Build
import com.ldreams.app.service.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LDreamsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleDefaultNotifications()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannels(
                listOf(realityCheckChannel, morningReminderChannel, bedtimeChannel)
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
    }
}
