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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ldreams.app.ui.theme.GradientEnd
import com.ldreams.app.ui.theme.GradientMid
import com.ldreams.app.ui.theme.GradientStart
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DreamBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_anim"
    )

    val starPositions = remember {
        val count = 80
        val sz = androidx.compose.ui.geometry.Size(1080f, 1920f)
        List(count) {
            Offset(
                Random.nextFloat() * sz.width,
                Random.nextFloat() * sz.height * 0.8f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val gradient = Brush.verticalGradient(
            colors = listOf(GradientStart, GradientMid, GradientEnd)
        )
        drawRect(gradient)

        val centerX = size.width / 2
        val centerY = size.height / 3
        val phase = animationProgress * (Math.PI / 180f)

        // Glowing moon
        val moonRadius = size.width * 0.15f
        val glowRadius = moonRadius * 2.5f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x33B366FF),
                    Color(0x1AB366FF),
                    Color(0x00000000)
                ),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            color = Color(0xFFE8E8F0),
            radius = moonRadius,
            center = Offset(centerX, centerY)
        )

        // Moon crater details
        drawCircle(
            color = Color(0x33A0A0B8),
            radius = moonRadius * 0.2f,
            center = Offset(centerX - moonRadius * 0.3f, centerY - moonRadius * 0.2f)
        )
        drawCircle(
            color = Color(0x33A0A0B8),
            radius = moonRadius * 0.15f,
            center = Offset(centerX + moonRadius * 0.25f, centerY + moonRadius * 0.3f)
        )

        // Stars
        starPositions.forEachIndexed { index, pos ->
            val twinkle = sin(phase.toFloat() + index * 0.5f) * 0.5f + 0.5f
            val alpha = twinkle * 0.8f
            val starSize = (1f + twinkle * 2f)
            drawCircle(
                color = Color(1f, 1f, 1f, alpha),
                radius = starSize * 1.5f,
                center = pos
            )
        }

        // Floating particles
        val particleCount = 15
        for (i in 0 until particleCount) {
            val x = (sin(phase.toFloat() + i * 1.2f) * 0.3f + 0.5f) * size.width
            val y = (cos(phase.toFloat() * 0.7f + i * 0.8f) * 0.3f + 0.5f) * size.height
            val alpha = sin(phase.toFloat() + i) * 0.15f + 0.2f
            drawCircle(
                color = Color(0x99B366FF).copy(alpha = alpha),
                radius = 2f,
                center = Offset(x, y)
            )
        }

        // Subtle wave at bottom
        val wavePath = Path().apply {
            moveTo(0f, size.height * 0.9f)
            for (x in 0..size.width.toInt() step 20) {
                val y = size.height * 0.9f +
                        sin(phase.toFloat() + x * 0.01f) * 20f +
                        sin(phase.toFloat() * 0.5f + x * 0.005f) * 10f
                lineTo(x.toFloat(), y)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x1A8B5CF6),
                    Color(0x08000000)
                )
            )
        )
    }
}

@Composable
fun GlowingCard(
    modifier: Modifier,
    glowColor: Color = Color(0x33B366FF),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A2A4A),
                        Color(0xFF222244)
                    )
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}
