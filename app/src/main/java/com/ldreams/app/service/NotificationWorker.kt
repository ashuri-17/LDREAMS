package com.ldreams.app.service

import android.Manifest
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
import com.ldreams.app.LDreamsApp
import com.ldreams.app.ui.screens.RealityCheckActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.random.Random

class NotificationWorker(
    @ApplicationContext context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("type") ?: "reality_check"
        val message = inputData.getString("message") ?: getDefaultMessage(type)

        if (type == "reality_check") {
            // Use fullScreenIntent to show the reality check popup over any app / lock screen.
            // This is the proper Android pattern for alarms and high-priority interruptions.
            val intent = Intent(applicationContext, RealityCheckActivity::class.java).apply {
                putExtra("message", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Check POST_NOTIFICATIONS on Android 13+
            val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true

            if (!canNotify) {
                // Fall back to starting the activity directly if possible
                try {
                    applicationContext.startActivity(intent)
                    return Result.success()
                } catch (e: Exception) {
                    return Result.failure()
                }
            }

            val notification = NotificationCompat.Builder(applicationContext, LDreamsApp.CHANNEL_REALITY_CHECK)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("LDREAMS - Reality Check")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // sound, vibration, lights
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()

            try {
                NotificationManagerCompat.from(applicationContext).notify(
                    Random.nextInt(Int.MAX_VALUE),
                    notification
                )
            } catch (e: SecurityException) {
                // Permission denied — try direct activity launch as fallback
                try {
                    applicationContext.startActivity(intent)
                } catch (_: Exception) {}
                return Result.failure()
            }

            return Result.success()
        }

        // --- Morning / Bedtime notifications ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
        }

        val channelId = when (type) {
            "morning" -> LDreamsApp.CHANNEL_MORNING_REMINDER
            "bedtime" -> LDreamsApp.CHANNEL_BEDTIME
            else -> LDreamsApp.CHANNEL_REALITY_CHECK
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("LDREAMS")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            ) {
                NotificationManagerCompat.from(applicationContext).notify(
                    Random.nextInt(Int.MAX_VALUE),
                    notification
                )
            }
        } catch (e: SecurityException) {
            return Result.failure()
        }

        return Result.success()
    }

    private fun getDefaultMessage(type: String): String {
        return when (type) {
            "morning" -> "Write your dream before you forget! What did you dream about?"
            "bedtime" -> "Time to prepare for lucid dreaming! Set your intention."
            else -> "Are you dreaming? Do a reality check!"
        }
    }
}
