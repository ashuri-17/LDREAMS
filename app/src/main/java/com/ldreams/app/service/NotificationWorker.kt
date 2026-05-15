package com.ldreams.app.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ldreams.app.LDreamsApp
import com.ldreams.app.data.repository.RealityCheckRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

class NotificationWorker(
    @ApplicationContext context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // On Android 13+, POST_NOTIFICATIONS runtime permission is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
        }

        val type = inputData.getString("type") ?: "reality_check"
        val message = inputData.getString("message") ?: getDefaultMessage(type)

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
