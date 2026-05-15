package com.ldreams.app.service

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object NotificationScheduler {

    private const val REALITY_CHECK_WORK = "reality_check_work"
    private const val MORNING_REMINDER_WORK = "morning_reminder_work"
    private const val BEDTIME_REMINDER_WORK = "bedtime_reminder_work"

    private val realityCheckMessages = listOf(
        "Are you dreaming right now?",
        "Do a reality check!",
        "Count your fingers.",
        "Look at the clock twice.",
        "Pinch your nose and try to breathe.",
        "Question reality.",
        "Are you in a dream?",
        "Reality check time!",
        "Is this real or a dream?",
        "Check if you're dreaming."
    )

    /**
     * Schedule randomized reality check notifications within active hours.
     * Uses multiple one-time workers scheduled at staggered times each day.
     * WorkManager automatically persists across reboots.
     */
    fun scheduleRealityChecks(
        context: Context,
        frequency: Int = 10,
        startHour: Int = 8,
        endHour: Int = 22
    ) {
        val workManager = WorkManager.getInstance(context)
        val activeHours = endHour - startHour
        if (activeHours <= 0 || frequency <= 0) return

        // Cancel existing reality check work
        workManager.cancelUniqueWork(REALITY_CHECK_WORK)

        val intervalMs = (activeHours * 3600000L) / frequency

        for (i in 0 until frequency) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, Random.nextInt(60))
                add(Calendar.MILLISECOND, (intervalMs * i + Random.nextInt((intervalMs / 3).toInt())).toInt())
            }

            val delayMs = cal.timeInMillis - System.currentTimeMillis()
            if (delayMs <= 0) continue

            val message = realityCheckMessages[Random.nextInt(realityCheckMessages.size)]

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(
                    "type" to "reality_check",
                    "message" to message
                ))
                .addTag("reality_check_$i")
                .build()

            workManager.enqueue(workRequest)
        }
    }

    /**
     * Schedule daily morning reminder to write dreams.
     * Automatically persists across reboots via WorkManager.
     */
    fun scheduleMorningReminder(context: Context, hour: Int = 7, minute: Int = 30) {
        val workManager = WorkManager.getInstance(context)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val initialDelayMs = if (cal.timeInMillis <= System.currentTimeMillis()) {
            // Schedule for tomorrow
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.timeInMillis - System.currentTimeMillis()
        } else {
            cal.timeInMillis - System.currentTimeMillis()
        }

        val messages = listOf(
            "Write your dream before you forget! What did you dream about?",
            "Your dreams are waiting! Record them now.",
            "Good morning! Take 5 minutes to journal your dreams.",
            "Don't let your dreams fade away. Write them down!"
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "type" to "morning",
                "message" to messages[Random.nextInt(messages.size)]
            ))
            .addTag("morning_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            MORNING_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Schedule daily bedtime reminder for lucid dreaming prep.
     */
    fun scheduleBedtimeReminder(context: Context, hour: Int = 21, minute: Int = 0) {
        val workManager = WorkManager.getInstance(context)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val initialDelayMs = if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.timeInMillis - System.currentTimeMillis()
        } else {
            cal.timeInMillis - System.currentTimeMillis()
        }

        val messages = listOf(
            "Time to prepare for lucid dreaming tonight!",
            "Set your intention for lucid dreaming before sleep.",
            "Practice your reality checks before sleeping.",
            "Try the MILD technique tonight. Repeat: I will remember I'm dreaming."
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "type" to "bedtime",
                "message" to messages[Random.nextInt(messages.size)]
            ))
            .addTag("bedtime_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            BEDTIME_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancel all scheduled notifications.
     */
    fun cancelAll(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(REALITY_CHECK_WORK)
        workManager.cancelUniqueWork(MORNING_REMINDER_WORK)
        workManager.cancelUniqueWork(BEDTIME_REMINDER_WORK)
    }

    /**
     * Schedule a one-time alarm at the given time.
     * When the alarm fires, [AlarmWorker] starts [AlarmRingingScreen] and
     * posts a high-priority notification with Snooze / Dismiss actions.
     *
     * @param context Application context.
     * @param hour    Hour of the alarm (0-23).
     * @param minute  Minute of the alarm (0-59).
     * @param label   Display label for the alarm (e.g. "Wake up!").
     */
    fun scheduleAlarm(context: Context, hour: Int, minute: Int, label: String) {
        val workManager = WorkManager.getInstance(context)
        val alarmId = System.currentTimeMillis()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow
        val delayMs = if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.timeInMillis - System.currentTimeMillis()
        } else {
            cal.timeInMillis - System.currentTimeMillis()
        }

        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                AlarmWorker.KEY_ALARM_ID to alarmId,
                AlarmWorker.KEY_LABEL to label,
                AlarmWorker.KEY_HOUR to hour,
                AlarmWorker.KEY_MINUTE to minute
            ))
            .addTag("${AlarmWorker.TAG_ALARM}_${hour}_${minute}")
            .build()

        workManager.enqueueUniqueWork(
            "alarm_${hour}_${minute}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
