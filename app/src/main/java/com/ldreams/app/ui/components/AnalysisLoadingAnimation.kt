package com.ldreams.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldreams.app.ui.theme.GradientEnd
import com.ldreams.app.ui.theme.GradientMid
import com.ldreams.app.ui.theme.GradientStart
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val loadingMessages = listOf(
    "Analyzing your dream patterns...",
    "Detecting symbols...",
    "Identifying emotions...",
    "Finding recurring themes...",
    "Sprinkling dream dust...",
    "Consulting dream guides..."
)

/**
 * A premium loading animation that displays a rotating moon/stars animation
 * with cycling analysis messages that fade in and out.
 */
@Composable
fun AnalysisLoadingAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_anim")

    // Rotation for the star field and moon
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing glow for the moon
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Star twinkle phase
    val starPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_phase"
    )

    // Cycling messages with fade in/out
    var currentMessageIndex by remember { mutableIntStateOf(0) }
    var messageAlpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            // Hold current message for 2 seconds
            delay(2000L)
            // Fade out
            val fadeOutSteps = 10
            for (i in 1..fadeOutSteps) {
                messageAlpha = 1f - i.toFloat() / fadeOutSteps
                delay(50L)
            }
            // Switch to next message
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
            // Fade in
            val fadeInSteps = 10
            for (i in 1..fadeInSteps) {
                messageAlpha = i.toFloat() / fadeInSteps
                delay(50L)
            }
        }
    }

    // Star positions (stable across recompositions)
    val starPositions = remember {
        val count = 30
        List(count) {
            val angle = Random.nextFloat() * 360f
            val dist = 30f + Random.nextFloat() * 80f
            Pair(angle, dist)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientMid,
                        GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated illustration area
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                    val orbitRadius = size.width * 0.38f

                    // Rotating star field
                    rotate(rotation, pivot = canvasCenter) {
                        // Draw orbiting stars
                        for (i in 0 until 12) {
                            val angle = (i * 30f)
                            val rad = Math.toRadians(angle.toDouble())
                            val x = canvasCenter.x + orbitRadius * cos(rad).toFloat()
                            val y = canvasCenter.y + orbitRadius * sin(rad).toFloat()
                            val starSize = 2f + sin(
                                starPhase * (Math.PI / 180f) + i
                            ).toFloat() * 1.5f

                            val alpha = 0.3f + sin(
                                starPhase * (Math.PI / 180f) + i * 0.7f
                            ).toFloat().coerceIn(0f, 1f) * 0.5f

                            drawCircle(
                                color = Color(1f, 1f, 1f, alpha),
                                radius = starSize.coerceAtLeast(1f),
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Inner rotating ring (counter-rotating)
                    rotate(-rotation * 0.7f, pivot = canvasCenter) {
                        val innerRadius = orbitRadius * 0.55f
                        for (i in 0 until 8) {
                            val angle = (i * 45f)
                            val rad = Math.toRadians(angle.toDouble())
                            val x = canvasCenter.x + innerRadius * cos(rad).toFloat()
                            val y = canvasCenter.y + innerRadius * sin(rad).toFloat()
                            val twinkle = sin(
                                starPhase * (Math.PI / 180f) + i * 1.3f
                            ).toFloat() * 0.3f + 0.5f

                            drawCircle(
                                color = NeonPurple.copy(alpha = twinkle * 0.4f),
                                radius = 2.5f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Fixed scattered stars
                    for ((angle, dist) in starPositions) {
                        val rad = Math.toRadians(angle.toDouble())
                        val x = canvasCenter.x + dist * cos(rad).toFloat()
                        val y = canvasCenter.y + dist * sin(rad).toFloat()
                        val twinkle = sin(
                            starPhase * (Math.PI / 180f) + angle
                        ).toFloat() * 0.3f + 0.5f

                        drawCircle(
                            color = Color(1f, 1f, 1f, twinkle * 0.6f),
                            radius = 1.2f,
                            center = Offset(x, y)
                        )
                    }

                    // Glowing moon center
                    val moonRadius = size.width * 0.16f

                    // Outer glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.08f * glowPulse),
                                NeonPurple.copy(alpha = 0.04f * glowPulse),
                                Color(0x00000000)
                            ),
                            radius = moonRadius * 4f
                        ),
                        radius = moonRadius * 4f,
                        center = canvasCenter
                    )

                    // Main glow ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonPurple.copy(alpha = 0.15f * glowPulse),
                                Color(0x00B366FF)
                            ),
                            radius = moonRadius * 2.5f
                        ),
                        radius = moonRadius * 2.5f,
                        center = canvasCenter
                    )

                    // Moon body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE8E8F0),
                                Color(0xFFC0C0D0),
                                Color(0xFFA0A0B8)
                            ),
                            radius = moonRadius
                        ),
                        radius = moonRadius,
                        center = canvasCenter
                    )

                    // Moon craters
                    drawCircle(
                        color = Color(0x33A0A0B8),
                        radius = moonRadius * 0.18f,
                        center = Offset(
                            canvasCenter.x - moonRadius * 0.3f,
                            canvasCenter.y - moonRadius * 0.25f
                        )
                    )
                    drawCircle(
                        color = Color(0x33A0A0B8),
                        radius = moonRadius * 0.12f,
                        center = Offset(
                            canvasCenter.x + moonRadius * 0.2f,
                            canvasCenter.y + moonRadius * 0.3f
                        )
                    )
                    drawCircle(
                        color = Color(0x22A0A0B8),
                        radius = moonRadius * 0.08f,
                        center = Offset(
                            canvasCenter.x + moonRadius * 0.35f,
                            canvasCenter.y - moonRadius * 0.15f
                        )
                    )

                    // Crescent overlay (to make it look like a crescent moon)
                    drawCircle(
                        color = GradientMid,
                        radius = moonRadius * 0.8f,
                        center = Offset(
                            canvasCenter.x + moonRadius * 0.4f,
                            canvasCenter.y - moonRadius * 0.2f
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator dots
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x22B366FF))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "AI Analysis",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonPurple.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cycling messages with alpha animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = loadingMessages[currentMessageIndex],
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondary.copy(alpha = messageAlpha)
                    ),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
