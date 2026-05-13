package com.ldreams.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
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

        val realityCheckChannel = NotificationChannel(
            CHANNEL_REALITY_CHECK,
            "Reality Checks",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Random reality check reminders throughout the day"
            enableVibration(true)
            enableLights(true)
        }

        val morningReminderChannel = NotificationChannel(
            CHANNEL_MORNING_REMINDER,
            "Morning Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Morning dream recall reminders"
            enableVibration(true)
        }

        val bedtimeChannel = NotificationChannel(
            CHANNEL_BEDTIME,
            "Bedtime Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Bedtime lucid dreaming reminders"
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
