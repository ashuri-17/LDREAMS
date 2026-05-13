package com.ldreams.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ldreams.app.LDreamsApp
import com.ldreams.app.R
import java.util.Calendar
import kotlin.random.Random

class RealityCheckScheduler {

    companion object {
        private const val REALITY_CHECK_REQUEST_CODE = 1001
        private const val MORNING_REMINDER_REQUEST_CODE = 1002
        private const val BEDTIME_REMINDER_REQUEST_CODE = 1003

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

        fun scheduleRealityChecks(context: Context, frequency: Int = 4, startHour: Int = 8, endHour: Int = 22) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val activeHours = endHour - startHour
            if (activeHours <= 0) return

            // Cancel existing alarms
            cancelAlarm(context, REALITY_CHECK_REQUEST_CODE)

            val intervalMs = (activeHours * 3600000L) / frequency

            for (i in 0 until frequency) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, startHour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, Random.nextInt(60))
                    add(Calendar.MILLISECOND, (intervalMs * i + Random.nextInt((intervalMs / 3).toInt())).toInt())
                }

                if (cal.timeInMillis <= System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, RealityCheckReceiver::class.java).apply {
                    putExtra("message", realityCheckMessages[Random.nextInt(realityCheckMessages.size)])
                    putExtra("request_code", REALITY_CHECK_REQUEST_CODE + i)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REALITY_CHECK_REQUEST_CODE + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            }
        }

        fun scheduleMorningReminder(context: Context, hour: Int = 7, minute: Int = 30) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            cancelAlarm(context, MORNING_REMINDER_REQUEST_CODE)

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, RealityCheckReceiver::class.java).apply {
                putExtra("message", "Write your dream before you forget! What did you dream about?")
                putExtra("type", "morning")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                MORNING_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        fun scheduleBedtimeReminder(context: Context, hour: Int = 21, minute: Int = 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            cancelAlarm(context, BEDTIME_REMINDER_REQUEST_CODE)

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val messages = listOf(
                "Time to prepare for lucid dreaming!",
                "Set your intention for lucid dreaming tonight.",
                "Practice your reality checks before sleep.",
                "Time for a MILD technique session."
            )

            val intent = Intent(context, RealityCheckReceiver::class.java).apply {
                putExtra("message", messages[Random.nextInt(messages.size)])
                putExtra("type", "bedtime")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                BEDTIME_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        fun cancelAlarm(context: Context, requestCode: Int) {
            val intent = Intent(context, RealityCheckReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.cancel()
        }
    }
}

class RealityCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Are you dreaming?"
        val type = intent.getStringExtra("type") ?: "reality_check"

        val channelId = when (type) {
            "morning" -> LDreamsApp.CHANNEL_MORNING_REMINDER
            "bedtime" -> LDreamsApp.CHANNEL_BEDTIME
            else -> LDreamsApp.CHANNEL_REALITY_CHECK
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("LDREAMS")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            RealityCheckScheduler.scheduleRealityChecks(context)
            RealityCheckScheduler.scheduleMorningReminder(context)
            RealityCheckScheduler.scheduleBedtimeReminder(context)
        }
    }
}
