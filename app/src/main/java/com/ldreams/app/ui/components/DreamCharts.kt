package com.ldreams.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.ui.theme.LucidGreen
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.SurfaceCard
import com.ldreams.app.ui.theme.TextMuted
import com.ldreams.app.ui.theme.TextPrimary
import com.ldreams.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Statistics charts composable: bar chart of dreams per day (last 7 days),
 * line chart of lucidity trend (last 30 days), and three progress rings
 * showing recall rate, lucidity rate, and reality check completion.
 */
@Composable
fun DreamCharts(
    dreams: List<DreamEntry>,
    totalDreams: Int,
    lucidDreamCount: Int,
    completedRealityChecks: Int,
    totalRealityChecks: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // --- Bar chart: Dreams per day (last 7 days) ---
        DreamsPerDayBarChart(dreams = dreams)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Line chart: Lucidity trend (last 30 days) ---
        LucidityTrendLineChart(dreams = dreams)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Progress rings ---
        ProgressRingsRow(
            dreams = dreams,
            totalDreams = totalDreams,
            lucidDreamCount = lucidDreamCount,
            completedRealityChecks = completedRealityChecks,
            totalRealityChecks = totalRealityChecks
        )
    }
}

// ---------------------------------------------------------------------------
// Bar chart
// ---------------------------------------------------------------------------

@Composable
private fun DreamsPerDayBarChart(dreams: List<DreamEntry>) {
    val dateKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayLabelFormat = remember { SimpleDateFormat("E", Locale.US) }

    // Compute counts for each of the last 7 days
    val barData = remember(dreams, dateKeyFormat, dayLabelFormat) {
        val result = mutableListOf<Pair<String, Int>>() // (label, count)
        val cal = Calendar.getInstance()
        for (i in 6 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateKeyFormat.format(cal.time)
            val label = dayLabelFormat.format(cal.time)
            val count = dreams.count { dateKeyFormat.format(it.timestamp) == key }
            result.add(Pair(label, count))
        }
        result
    }

    val maxCount = barData.maxOfOrNull { it.second } ?: 1
    val hasData = barData.any { it.second > 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dreams This Week",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (!hasData) {
                Text(
                    text = "No dreams recorded this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Value labels
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        barData.forEach { (_, count) ->
                            Text(
                                text = if (count > 0) count.toString() else "",
                                color = NeonPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bars
                    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        val chartHeight = size.height
                        val barCount = barData.size
                        val totalGapWidth = size.width * 0.12f
                        val gapPerSide = totalGapWidth / (barCount + 1)
                        val barWidth = (size.width - totalGapWidth) / barCount
                        val effectiveMax = maxCount.coerceAtLeast(1)

                        barData.forEachIndexed { index, (_, count) ->
                            val barHeight = (count.toFloat() / effectiveMax) * chartHeight
                            val x = gapPerSide + index * (barWidth + gapPerSide)
                            val y = chartHeight - barHeight

                            drawRoundRect(
                                color = NeonPurple,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight.coerceAtLeast(0f)),
                                cornerRadius = CornerRadius(barWidth / 3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // X-axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        barData.forEach { (label, _) ->
                            Text(
                                text = label,
                                color = TextMuted,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Line chart
// ---------------------------------------------------------------------------

@Composable
private fun LucidityTrendLineChart(dreams: List<DreamEntry>) {
    val dateKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayLabelFormat = remember { SimpleDateFormat("M/d", Locale.US) }

    // Compute average lucidity for each of the last 30 days
    val lineData = remember(dreams, dateKeyFormat, dayLabelFormat) {
        val result = mutableListOf<Pair<String, Float>>() // (label, avgLucidity)
        val cal = Calendar.getInstance()
        for (i in 29 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateKeyFormat.format(cal.time)
            val label = dayLabelFormat.format(cal.time)
            val dayDreams = dreams.filter { dateKeyFormat.format(it.timestamp) == key }
            val avg = if (dayDreams.isNotEmpty()) {
                dayDreams.map { it.lucidityLevel.toFloat() }.average().toFloat()
            } else {
                0f
            }
            result.add(Pair(label, avg))
        }
        result
    }

    val hasData = lineData.any { it.second > 0f }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Lucidity Trend (30 Days)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (!hasData) {
                Text(
                    text = "No lucidity data available yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val chartHeight = size.height
                    val chartBottom = size.height
                    val chartTop = 0f
                    val dataCount = lineData.size
                    val xStep = if (dataCount > 1) size.width / (dataCount - 1) else size.width

                    // Horizontal grid lines at 25, 50, 75
                    for (gridValue in listOf(25, 50, 75)) {
                        val gy = chartBottom - (gridValue / 100f) * chartHeight
                        drawLine(
                            color = Color(0x18FFFFFF),
                            start = Offset(0f, gy),
                            end = Offset(size.width, gy),
                            strokeWidth = 1f
                        )
                    }

                    // Build points
                    val points = mutableListOf<Offset>()
                    lineData.forEachIndexed { index, (_, value) ->
                        val x = index * xStep
                        val y = (chartBottom - (value / 100f) * chartHeight)
                            .coerceIn(chartTop, chartBottom)
                        points.add(Offset(x, y))
                    }

                    if (points.size >= 2) {
                        // Area fill under the line
                        val areaPath = Path().apply {
                            moveTo(points.first().x, chartBottom)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, chartBottom)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.2f),
                                    NeonCyan.copy(alpha = 0f)
                                ),
                                startY = chartTop,
                                endY = chartBottom
                            )
                        )

                        // Line path
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = NeonCyan,
                            style = Stroke(
                                width = 2.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Data point dots
                        points.forEach { point ->
                            drawCircle(color = NeonCyan, radius = 3f, center = point)
                            drawCircle(
                                color = Color(0xFF16213E),
                                radius = 1.5f,
                                center = point
                            )
                        }
                    } else if (points.size == 1) {
                        drawCircle(color = NeonCyan, radius = 4f, center = points[0])
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // X-axis: show every 5th label to avoid crowding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    lineData.filterIndexed { index, _ -> index % 5 == 0 || index == lineData.size - 1 }
                        .forEach { (label, _) ->
                            Text(
                                text = label,
                                color = TextMuted,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Progress rings
// ---------------------------------------------------------------------------

@Composable
private fun ProgressRingsRow(
    dreams: List<DreamEntry>,
    totalDreams: Int,
    lucidDreamCount: Int,
    completedRealityChecks: Int,
    totalRealityChecks: Int
) {
    // Dream recall rate: percentage of days in last 30 with at least one dream entry
    val recallRate = remember(dreams) {
        if (dreams.isEmpty()) 0
        else {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            val daysWithDreams = (0 until 30).count { i ->
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val key = fmt.format(cal.time)
                dreams.any { fmt.format(it.timestamp) == key }
            }
            (daysWithDreams * 100 / 30).coerceIn(0, 100)
        }
    }

    val lucidityRate = remember(totalDreams, lucidDreamCount) {
        if (totalDreams == 0) 0
        else (lucidDreamCount * 100 / totalDreams).coerceIn(0, 100)
    }

    val rcRate = remember(completedRealityChecks, totalRealityChecks) {
        if (totalRealityChecks == 0) 0
        else (completedRealityChecks * 100 / totalRealityChecks).coerceIn(0, 100)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressRing(
                    percentage = recallRate,
                    label = "Recall Rate",
                    color = LucidGreen,
                    modifier = Modifier.weight(1f)
                )
                ProgressRing(
                    percentage = lucidityRate,
                    label = "Lucidity Rate",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                ProgressRing(
                    percentage = rcRate,
                    label = "RC Completion",
                    color = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(
    percentage: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(68.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 7f
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background circle
                drawCircle(
                    color = color.copy(alpha = 0.12f),
                    radius = radius,
                    center = center
                )

                // Progress arc
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (percentage / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(
                        (size.width - radius * 2f) / 2f,
                        (size.height - radius * 2f) / 2f
                    ),
                    size = Size(radius * 2f, radius * 2f)
                )
            }
            Text(
                text = "${percentage}%",
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )
    }
}
