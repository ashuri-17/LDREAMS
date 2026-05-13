package com.ldreams.app.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ldreams.app.service.AlarmActionReceiver
import com.ldreams.app.service.AlarmSoundManager
import com.ldreams.app.service.AlarmWorker
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.theme.LDreamsTheme
import com.ldreams.app.ui.theme.LucidGreen
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.TextSecondary
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Full-screen alarm activity displayed when an alarm fires.
 *
 * Shows an immersive DreamBackground with:
 * - Current time in large text
 * - "LDREAMS ALARM" header
 * - Wake Up, Snooze (10 min), and Awake Briefly (5 min) action buttons
 *
 * The activity claims sound playback via [AlarmSoundManager] so the alarm
 * ringtone persists across configuration changes and can be stopped from
 * notification actions even if this activity is destroyed.
 */
class AlarmRingingScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure the screen turns on and shows over the lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        enableEdgeToEdge()

        // Block back button -- user must explicitly dismiss the alarm
        onBackPressedDispatcher.addCallback(
            this@AlarmRingingScreen,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing — alarm cannot be dismissed via back button
                }
            }
        )

        // Start playing alarm sound
        AlarmSoundManager.play(this)
        AlarmSoundManager.vibrate(this)

        // Monitor for remote dismiss from notification action
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (isActive) {
                    if (AlarmSoundManager.dismissedRemotely) {
                        AlarmSoundManager.resetRemoteDismiss()
                        finish()
                        break
                    }
                    delay(500L)
                }
            }
        }

        // Read extras passed from AlarmWorker
        val alarmId = intent?.getLongExtra(AlarmWorker.KEY_ALARM_ID, 0L) ?: 0L
        val label = intent?.getStringExtra(AlarmWorker.KEY_LABEL) ?: "Alarm"

        setContent {
            LDreamsTheme {
                AlarmContent(
                    label = label,
                    onWakeUp = {
                        AlarmSoundManager.stop()
                        finish()
                    },
                    onSnooze = {
                        AlarmSoundManager.stop()
                        rescheduleAlarm(alarmId, label, AlarmActionReceiver.SNOOZE_MINUTES)
                        finish()
                    },
                    onAwakeBriefly = {
                        AlarmSoundManager.stop()
                        rescheduleAlarm(alarmId, label, 5L)
                        finish()
                    }
                )
            }
        }
    }

    /**
     * Reschedule this alarm for [minutes] from now via WorkManager.
     */
    private fun rescheduleAlarm(alarmId: Long, label: String, minutes: Long) {
        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putLong(AlarmWorker.KEY_ALARM_ID, alarmId)
                    .putString(AlarmWorker.KEY_LABEL, label)
                    .build()
            )
            .addTag("${AlarmWorker.TAG_ALARM}_${alarmId}")
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Sound is stopped explicitly by user actions (dismiss/snooze/briefly)
        // before finish() is called — no need to stop here.
    }
}

// ----------------------------------------------------------------------- //
//  Composable UI
// ----------------------------------------------------------------------- //

@Composable
private fun AlarmContent(
    label: String,
    onWakeUp: () -> Unit,
    onSnooze: () -> Unit,
    onAwakeBriefly: () -> Unit
) {
    // Smooth fade-in when the alarm triggers
    val fadeAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fadeAlpha.animateTo(1f, animationSpec = tween(durationMillis = 1500))
    }

    // Live clock — updates every 30 seconds
    var currentTime by remember { mutableStateOf(formattedTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            currentTime = formattedTime()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fadeAlpha.value)
    ) {
        // Dreamy moon-and-stars background
        DreamBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Header ---
            Text(
                text = "LDREAMS  ALARM",
                style = MaterialTheme.typography.labelLarge,
                color = NeonPurple,
                letterSpacing = 10.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Current time (large, glowing) ---
            Text(
                text = currentTime,
                fontSize = 80.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Alarm label ---
            Text(
                text = label,
                fontSize = 18.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Wake Up ---
            Button(
                onClick = onWakeUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple
                )
            ) {
                Text(
                    text = "WAKE UP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Snooze (10 min) ---
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
            ) {
                Text(
                    text = "SNOOZE (10 MIN)",
                    fontSize = 15.sp,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Awake Briefly (5 min, WBTB technique) ---
            TextButton(
                onClick = onAwakeBriefly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AWAKE BRIEFLY (5 MIN) — WBTB",
                    fontSize = 13.sp,
                    color = LucidGreen.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formattedTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
