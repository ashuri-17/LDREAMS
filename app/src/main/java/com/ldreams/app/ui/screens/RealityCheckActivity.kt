package com.ldreams.app.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldreams.app.data.repository.RealityCheckRepository
import com.ldreams.app.service.AlarmWorker
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.theme.LDreamsTheme
import com.ldreams.app.ui.theme.LucidGreen
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.SurfaceCard
import com.ldreams.app.ui.theme.TextPrimary
import com.ldreams.app.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RealityCheckActivity : ComponentActivity() {

    @Inject
    lateinit var realityCheckRepository: RealityCheckRepository

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

        val message = intent?.getStringExtra("message") ?: "Are you dreaming? Do a reality check!"

        setContent {
            LDreamsTheme {
                RealityCheckContent(
                    message = message,
                    onYesDreaming = {
                        lifecycleScope.launch {
                            realityCheckRepository.recordCheck(completed = true, wasDreaming = true)
                        }
                        // Launch alarm for lucid dream
                        val alarmIntent = Intent(this@RealityCheckActivity, AlarmRingingScreen::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(AlarmWorker.KEY_ALARM_ID, System.currentTimeMillis())
                            putExtra(AlarmWorker.KEY_LABEL, "Lucid Dream - Reality Check")
                        }
                        startActivity(alarmIntent)
                        finish()
                    },
                    onNotDreaming = {
                        lifecycleScope.launch {
                            realityCheckRepository.recordCheck(completed = true, wasDreaming = false)
                        }
                        finish()
                    },
                    onNotSure = {
                        // State handled inside composable
                    },
                    onDoneChecking = {
                        finish()
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------------------------- //
//  Composable UI
// ----------------------------------------------------------------------- //

@Composable
private fun RealityCheckContent(
    message: String,
    onYesDreaming: () -> Unit,
    onNotDreaming: () -> Unit,
    onNotSure: () -> Unit,
    onDoneChecking: () -> Unit
) {
    var showMethods by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Dreamy moon-and-stars background
        DreamBackground()

        if (!showMethods) {
            // Main card: question and action buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceCard.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- Title ---
                        Text(
                            text = "Reality Check",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Question ---
                        Text(
                            text = "Are you dreaming right now?",
                            fontSize = 20.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- Subtitle message ---
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- YES - I'm dreaming ---
                        Button(
                            onClick = onYesDreaming,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LucidGreen
                            )
                        ) {
                            Text(
                                text = "YES - I'm dreaming",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- NO - This is real ---
                        Button(
                            onClick = onNotDreaming,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan
                            )
                        ) {
                            Text(
                                text = "NO - This is real",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Not sure... let me check ---
                        OutlinedButton(
                            onClick = { showMethods = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonPurple
                            )
                        ) {
                            Text(
                                text = "Not sure... let me check",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Methods list view
            AnimatedVisibility(
                visible = showMethods,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 48.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceCard.copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Reality Check Methods",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val methods = listOf(
                                "1. Count your fingers" to "In dreams, you may have too many or too few fingers. Count them carefully on both hands.",
                                "2. Look at the clock twice" to "Look at a clock or text, look away, then look back. In dreams, the time or text will change.",
                                "3. Pinch your nose and breathe" to "Pinch your nose closed and try to breathe in. If you can breathe, you are dreaming.",
                                "4. Check your reflection" to "Look in a mirror. In dreams, reflections often appear distorted, blurry, or wrong."
                            )

                            methods.forEach { (title, description) ->
                                MethodItem(title = title, description = description)
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- Done checking ---
                            Button(
                                onClick = onDoneChecking,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonPurple
                                )
                            ) {
                                Text(
                                    text = "Done checking",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodItem(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}
