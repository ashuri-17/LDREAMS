package com.ldreams.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * BroadcastReceiver that handles alarm notification action buttons
 * (Snooze and Dismiss) from the notification shade.
 */
class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("alarm_id", 0L)
        val label = intent.getStringExtra("label") ?: "Alarm"

        when (intent.action) {
            ACTION_SNOOZE -> {
                val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
                    .setInitialDelay(SNOOZE_MINUTES, TimeUnit.MINUTES)
                    .setInputData(
                        Data.Builder()
                            .putLong("alarm_id", alarmId)
                            .putString("label", label)
                            .build()
                    )
                    .addTag("alarm_${alarmId}")
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            ACTION_DISMISS -> {
                AlarmSoundManager.markDismissedFromRemote()
                AlarmSoundManager.stop()
            }
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.ldreams.app.action.SNOOZE"
        const val ACTION_DISMISS = "com.ldreams.app.action.DISMISS"
        const val SNOOZE_MINUTES = 10L
    }
}
