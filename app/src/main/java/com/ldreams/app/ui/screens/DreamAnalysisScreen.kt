package com.ldreams.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ldreams.app.data.models.DreamAnalysis
import com.ldreams.app.ui.components.AnalysisLoadingAnimation
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.components.EmotionWheel
import com.ldreams.app.ui.theme.CardBorder
import com.ldreams.app.ui.theme.DreamGold
import com.ldreams.app.ui.theme.GlassWhite
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.SurfaceCard
import com.ldreams.app.ui.theme.TextMuted
import com.ldreams.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamAnalysisScreen(
    navController: NavController,
    viewModel: DreamAnalysisViewModel = hiltViewModel()
) {
    val analysis by viewModel.analysis.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dream Analysis",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            DreamBackground()

            when {
                isLoading -> {
                    AnalysisLoadingAnimation(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }

                analysis == null -> {
                    EmptyAnalysisState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }

                else -> {
                    AnalysisResults(
                        analysis = analysis!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAnalysisState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonPurple.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            radius = 1.5f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonPurple.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Not enough data yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Write at least 3 dream entries to unlock\npersonalized AI dream analysis and insights.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun AnalysisResults(
    analysis: DreamAnalysis,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section 1: Clarity Score
        ClarityScoreCard(score = analysis.clarityScore)

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Overview Stats
        OverviewStatsCard(analysis = analysis)

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Dream Symbols
        if (analysis.symbols.isNotEmpty()) {
            DreamSymbolsCard(symbols = analysis.symbols)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 4: Emotion Wheel
        EmotionWheelCard(emotionIntensities = analysis.emotionIntensities)

        Spacer(modifier = Modifier.height(16.dp))

        // Section 5: Recurring Patterns
        if (analysis.recurringThemes.isNotEmpty() || analysis.dreamSigns.isNotEmpty()) {
            RecurringPatternsCard(analysis = analysis)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 6: Personalized Tips
        if (analysis.tips.isNotEmpty()) {
            PersonalizedTipsCard(tips = analysis.tips)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ---- Clarity Score Section ----

@Composable
private fun ClarityScoreCard(score: Int) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "clarity_score"
    )

    PremiumCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Dream Clarity Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Circular progress indicator
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                    val canvasRadius = minOf(size.width, size.height) / 2f * 0.85f

                    // Background ring
                    drawCircle(
                        color = GlassWhite,
                        radius = canvasRadius,
                        center = canvasCenter,
                        style = Stroke(width = 12f)
                    )

                    // Progress arc
                    val sweepAngle = (animatedScore / 100f) * 360f
                    val progressColor = when {
                        animatedScore >= 70 -> NeonCyan
                        animatedScore >= 40 -> DreamGold
                        else -> NeonPurple
                    }

                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                progressColor.copy(alpha = 0.6f),
                                progressColor,
                                progressColor.copy(alpha = 0.9f)
                            ),
                            center = canvasCenter
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(
                            canvasCenter.x - canvasRadius,
                            canvasCenter.y - canvasRadius
                        ),
                        size = Size(canvasRadius * 2, canvasRadius * 2),
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )

                    // Glow on the arc end
                    val endAngle = Math.toRadians((sweepAngle - 90f).toDouble())
                    val dotPos = Offset(
                        canvasCenter.x + canvasRadius * kotlin.math.cos(endAngle).toFloat(),
                        canvasCenter.y + canvasRadius * kotlin.math.sin(endAngle).toFloat()
                    )
                    drawCircle(
                        color = progressColor.copy(alpha = 0.3f),
                        radius = 10f,
                        center = dotPos
                    )
                    drawCircle(
                        color = progressColor,
                        radius = 5f,
                        center = dotPos
                    )

                    // Score text using native canvas
                    val scoreText = "${animatedScore.toInt()}"
                    val scorePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 48.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                    val scoreFm = scorePaint.fontMetrics
                    val scoreTextY = canvasCenter.y - (scoreFm.ascent + scoreFm.descent) / 2f - 6f
                    drawContext.canvas.nativeCanvas.drawText(
                        scoreText,
                        canvasCenter.x,
                        scoreTextY,
                        scorePaint
                    )

                    // "/ 100" label
                    val outOfPaint = android.graphics.Paint().apply {
                        color = TextSecondary.toArgb()
                        textSize = 13.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val outOfFm = outOfPaint.fontMetrics
                    val outOfTextY = canvasCenter.y - (outOfFm.ascent + outOfFm.descent) / 2f + 18f
                    drawContext.canvas.nativeCanvas.drawText(
                        "/ 100",
                        canvasCenter.x,
                        outOfTextY,
                        outOfPaint
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interpretation text
            val interpretation = when {
                score >= 80 -> "Exceptional clarity! Your dream recall and awareness are outstanding."
                score >= 60 -> "Good clarity. Your dream practice is showing solid results."
                score >= 40 -> "Developing clarity. Keep up with journaling and reality checks."
                else -> "Early stages. Consistency with dream journaling will improve your scores."
            }
            Text(
                text = interpretation,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ---- Overview Stats ----

@Composable
private fun OverviewStatsCard(analysis: DreamAnalysis) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.BarChart,
                title = "Overview",
                color = NeonPurple
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = "${analysis.totalDreamsAnalyzed}",
                    icon = Icons.Default.BarChart
                )
                StatItem(
                    label = "This Week",
                    value = "${analysis.weeklyDreamCount}",
                    icon = Icons.Default.Repeat
                )
                StatItem(
                    label = "This Month",
                    value = "${analysis.monthlyDreamCount}",
                    icon = Icons.Default.AutoAwesome
                )
            }
            if (analysis.averageVividness > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Avg Vividness",
                        value = "${analysis.averageVividness.toInt()}%",
                        icon = Icons.Default.Star
                    )
                    if (analysis.lucidityTrend.isNotEmpty()) {
                        val avgLucidity = analysis.lucidityTrend
                            .map { it.second }
                            .average()
                            .toInt()
                        StatItem(
                            label = "Avg Lucidity",
                            value = "$avgLucidity%",
                            icon = Icons.Default.Psychology
                        )
                    }
                }
            }
        }
    }
}

// ---- Dream Symbols ----

@Composable
private fun DreamSymbolsCard(symbols: List<Pair<String, String>>) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Search,
                title = "Dream Symbols",
                color = DreamGold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Key symbols detected in your dreams and their interpretations:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            symbols.forEachIndexed { index, (symbol, meaning) ->
                SymbolItem(symbol = symbol, meaning = meaning)
                if (index < symbols.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun SymbolItem(symbol: String, meaning: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = GlassWhite,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DreamGold.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 1.2f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol.first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DreamGold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = meaning,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

// ---- Emotion Wheel ----

@Composable
private fun EmotionWheelCard(emotionIntensities: Map<String, Float>) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.SelfImprovement,
                title = "Emotional Landscape",
                color = NeonPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The distribution of emotions across your dream experiences:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            EmotionWheel(
                emotions = emotionIntensities,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

// ---- Recurring Patterns ----

@Composable
private fun RecurringPatternsCard(analysis: DreamAnalysis) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Repeat,
                title = "Recurring Patterns",
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (analysis.recurringThemes.isNotEmpty()) {
                Text(
                    text = "Recurring Themes",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                analysis.recurringThemes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = NeonCyan,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = theme,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (analysis.dreamSigns.isNotEmpty()) {
                if (analysis.recurringThemes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
                Text(
                    text = "Dream Signs",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DreamGold.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "These recurring elements can trigger lucidity:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                analysis.dreamSigns.forEach { sign ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = DreamGold,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sign,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Common emotions summary
            if (analysis.commonEmotions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Emotion Frequencies",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonPurple.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                analysis.commonEmotions.forEach { (emotion, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = NeonPurple,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$emotion ($count times)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ---- Personalized Tips ----

@Composable
private fun PersonalizedTipsCard(tips: List<String>) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Lightbulb,
                title = "Personalized Tips",
                color = DreamGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI-generated recommendations based on your dream patterns:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            tips.forEachIndexed { index, tip ->
                TipItem(index = index + 1, tip = tip)
                if (index < tips.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TipItem(index: Int, tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = GlassWhite,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DreamGold.copy(alpha = 0.3f),
                            DreamGold.copy(alpha = 0.1f)
                        ),
                        radius = 1.2f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = DreamGold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ---- Shared Composables ----

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 1.5f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = GlassWhite,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.75f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = CardBorder.copy(alpha = 0.5f)
        )
    ) {
        content()
    }
}
