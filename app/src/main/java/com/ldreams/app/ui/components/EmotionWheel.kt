package com.ldreams.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldreams.app.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val emotionColorMap = mapOf(
    "happy" to Color(0xFFF59E0B),
    "sad" to Color(0xFF3B82F6),
    "anxious" to Color(0xFF8B5CF6),
    "peaceful" to Color(0xFF06F7F7),
    "excited" to Color(0xFFEC4899),
    "fearful" to Color(0xFFEF4444)
)

/**
 * A circular emotion wheel that visualizes detected emotions using colored arc segments.
 *
 * @param emotions Map of emotion name to intensity value (0-1 scale).
 *        Supported emotion names: happy, sad, anxious, peaceful, excited, fearful
 * @param modifier Modifier for the composable
 */
@Composable
fun EmotionWheel(
    emotions: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val validEmotions = remember(emotions) {
        emotions.filterKeys { it.lowercase() in emotionColorMap }
            .mapKeys { it.key.lowercase() }
    }

    val hasEmotions = validEmotions.isNotEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = minOf(size.width, size.height) / 2f * 0.72f
                val arcRadius = outerRadius * 0.85f
                val labelRadius = outerRadius * 1.15f
                val centerRadius = outerRadius * 0.30f

                // Initialize text paint for labels
                val labelPaint = android.graphics.Paint().apply {
                    color = Color.White.copy(alpha = 0.9f).toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                if (hasEmotions) {
                    val totalIntensity = validEmotions.values.sum().coerceAtLeast(0.01f)

                    // Draw subtle background ring
                    drawCircle(
                        color = Color(0x22FFFFFF),
                        radius = outerRadius,
                        center = canvasCenter,
                        style = Stroke(width = 1.5f)
                    )

                    var startAngle = -90f

                    for ((name, intensity) in validEmotions) {
                        val sweepAngle = (intensity / totalIntensity) * 360f
                        if (sweepAngle <= 0f) continue

                        val color = emotionColorMap[name] ?: Color.Gray
                        val effectiveSweep = sweepAngle.coerceAtLeast(1f)

                        // Draw arc segment with glow
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.3f),
                                    color,
                                    color.copy(alpha = 0.3f)
                                ),
                                center = canvasCenter
                            ),
                            startAngle = startAngle - 1f,
                            sweepAngle = effectiveSweep + 2f,
                            useCenter = true,
                            topLeft = Offset(
                                canvasCenter.x - arcRadius,
                                canvasCenter.y - arcRadius
                            ),
                            size = Size(arcRadius * 2, arcRadius * 2)
                        )

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = effectiveSweep,
                            useCenter = true,
                            topLeft = Offset(
                                canvasCenter.x - arcRadius,
                                canvasCenter.y - arcRadius
                            ),
                            size = Size(arcRadius * 2, arcRadius * 2)
                        )

                        // Outer border line
                        drawArc(
                            color = color.copy(alpha = 0.6f),
                            startAngle = startAngle,
                            sweepAngle = effectiveSweep,
                            useCenter = false,
                            topLeft = Offset(
                                canvasCenter.x - arcRadius,
                                canvasCenter.y - arcRadius
                            ),
                            size = Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(width = 2f)
                        )

                        // Draw label outside arc
                        val midAngleRad = Math.toRadians(
                            (startAngle + effectiveSweep / 2f).toDouble()
                        )
                        val labelPos = Offset(
                            canvasCenter.x + labelRadius * cos(midAngleRad).toFloat(),
                            canvasCenter.y + labelRadius * sin(midAngleRad).toFloat()
                        )

                        val labelText = name.replaceFirstChar { it.uppercase() }
                        labelPaint.color = Color.White.copy(alpha = 0.9f).toArgb()
                        val fm = labelPaint.fontMetrics
                        val textY = labelPos.y - (fm.ascent + fm.descent) / 2f
                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            labelPos.x,
                            textY,
                            labelPaint
                        )

                        // Draw connecting line
                        val lineStartAngle =
                            Math.toRadians((startAngle + effectiveSweep / 2f).toDouble())
                        val lineStart = Offset(
                            canvasCenter.x + arcRadius * cos(lineStartAngle).toFloat(),
                            canvasCenter.y + arcRadius * sin(lineStartAngle).toFloat()
                        )
                        val lineEnd = Offset(
                            canvasCenter.x + outerRadius * cos(lineStartAngle).toFloat(),
                            canvasCenter.y + outerRadius * sin(lineStartAngle).toFloat()
                        )
                        drawLine(
                            color = color.copy(alpha = 0.3f),
                            start = lineStart,
                            end = lineEnd,
                            strokeWidth = 1f
                        )

                        startAngle += effectiveSweep
                    }
                }

                // Outer glow ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x22B366FF),
                            Color(0x0FB366FF),
                            Color(0x00000000)
                        ),
                        radius = centerRadius * 3.5f
                    ),
                    radius = centerRadius * 3.5f,
                    center = canvasCenter
                )

                // Center circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A2A4A),
                            Color(0xFF1E1E3A),
                            Color(0xFF1A1A3A)
                        ),
                        radius = centerRadius
                    ),
                    radius = centerRadius,
                    center = canvasCenter
                )

                // Inner subtle glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44B366FF),
                            Color(0x00000000)
                        ),
                        radius = centerRadius * 0.8f
                    ),
                    radius = centerRadius * 0.8f,
                    center = canvasCenter
                )

                // Center text: "Emotions"
                val centerPaint = android.graphics.Paint().apply {
                    color = Color.White.copy(alpha = 0.85f).toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                val centerFm = centerPaint.fontMetrics
                val centerTextY = canvasCenter.y - (centerFm.ascent + centerFm.descent) / 2f
                drawContext.canvas.nativeCanvas.drawText(
                    "Emotions",
                    canvasCenter.x,
                    centerTextY,
                    centerPaint
                )
            }
        }

        // Legend
        if (hasEmotions) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                validEmotions.keys.take(3).forEach { name ->
                    EmotionLegendItem(
                        name = name.replaceFirstChar { it.uppercase() },
                        color = emotionColorMap[name] ?: Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                validEmotions.keys.drop(3).take(3).forEach { name ->
                    EmotionLegendItem(
                        name = name.replaceFirstChar { it.uppercase() },
                        color = emotionColorMap[name] ?: Color.Gray
                    )
                }
            }
        }

        if (!hasEmotions) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No emotion data available",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmotionLegendItem(
    name: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
