package com.ldreams.app.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.theme.DreamGold
import com.ldreams.app.ui.theme.LucidGreen
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidesScreen(navController: NavController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Lucid Dreaming Guides", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            DreamBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Technique guides
                Text(
                    text = "Induction Techniques",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "WILD Technique",
                    subtitle = "Wake-Initiated Lucid Dream",
                    description = "Lie still and keep your mind awake while your body falls asleep. Focus on hypnagogic imagery as you transition directly from wakefulness into a dream state.",
                    icon = Icons.Default.Nightlight,
                    color = NeonPurple
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "MILD Technique",
                    subtitle = "Mnemonic Induction of Lucid Dreams",
                    description = "Set a strong intention to remember you're dreaming. Repeat 'Next time I'm dreaming, I will remember I'm dreaming' as you fall asleep. Visualize yourself becoming lucid.",
                    icon = Icons.Default.Psychology,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "WBTB Method",
                    subtitle = "Wake Back to Bed",
                    description = "Wake up after 5-6 hours of sleep, stay awake for 20-60 minutes, then go back to sleep. This increases your chances of entering REM sleep with heightened awareness.",
                    icon = Icons.Default.Bedtime,
                    color = DreamGold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mindfulness exercises
                Text(
                    text = "Mindfulness & Preparation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "Meditation",
                    subtitle = "Pre-sleep mindfulness",
                    description = "10-minute guided meditation to calm the mind before sleep. Focus on your breath and body awareness to increase overall mindfulness.",
                    icon = Icons.Default.SelfImprovement,
                    color = LucidGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "Breathing Exercise",
                    subtitle = "4-7-8 technique",
                    description = "Inhale for 4 seconds, hold for 7 seconds, exhale for 8 seconds. This activates the parasympathetic nervous system and prepares you for deep sleep.",
                    icon = Icons.Default.Air,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "Bedtime Affirmations",
                    subtitle = "Set your intention",
                    description = "Repeat positive affirmations: 'I will remember my dreams tonight.' 'I will become lucid in my dreams.' 'I have full control over my dream experience.'",
                    icon = Icons.Default.Star,
                    color = DreamGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideCard(
                    title = "Sleep Soundscapes",
                    subtitle = "Ambient dream audio",
                    description = "Listen to relaxing ambient sounds designed to promote lucid dreaming. Binaural beats and theta wave frequencies can enhance dream recall and lucidity.",
                    icon = Icons.Default.MusicNote,
                    color = NeonPurple
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun GuideCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}
