package com.ldreams.app.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ldreams.app.ui.theme.NeonPurple
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * A composable microphone button that records voice using Android's built-in
 * [RecognizerIntent.ACTION_RECOGNIZE_SPEECH] and returns the transcribed text.
 *
 * Flow:
 * 1. Tap the mic icon -- requests RECORD_AUDIO permission (accompanist).
 * 2. Once granted, launches the system speech-recognition dialog.
 * 3. While the system is listening a pulsing NeonPurple glow animates behind
 *    the button.
 * 4. When the result arrives [onRecordingComplete] is called with the
 *    transcribed text (or a fallback audio file path).
 * 5. A "Recording saved" snackbar is shown via [snackbarHostState].
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceInputButton(
    snackbarHostState: SnackbarHostState,
    onRecordingComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }

    // ---- Permission ----
    val permissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    // ---- Speech-recognition launcher ----
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecording = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val transcribed = matches?.firstOrNull()
            if (!transcribed.isNullOrBlank()) {
                onRecordingComplete(transcribed)
            }
        }
        scope.launch {
            snackbarHostState.showSnackbar("Recording saved")
        }
    }

    // ---- Pulsing glow animation ----
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    // ---- UI ----
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // Glow ring behind the button (visible only while recording)
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(pulseScale)
                    .background(
                        color = NeonPurple.copy(alpha = pulseAlpha * 0.3f),
                        shape = CircleShape,
                    ),
            )
        }

        IconButton(
            onClick = {
                when {
                    // 1. Permission not yet granted -> ask
                    !permissionState.status.isGranted -> {
                        permissionState.launchPermissionRequest()
                    }
                    // 2. Already recording -> ignore duplicate tap
                    isRecording -> { /* no-op */ }
                    // 3. Permission granted, not recording -> launch speech
                    else -> {
                        isRecording = true
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your dream...")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            isRecording = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Speech recognition is not available on this device",
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isRecording) {
                    NeonPurple.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                tint = if (isRecording) NeonPurple else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
