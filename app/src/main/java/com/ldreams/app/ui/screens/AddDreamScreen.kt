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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ldreams.app.data.models.DreamMood
import com.ldreams.app.ui.components.VoiceInputButton
import com.ldreams.app.ui.theme.DreamGold
import com.ldreams.app.ui.theme.LucidGreen
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.NightmareRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDreamScreen(
    navController: NavController,
    viewModel: AddDreamViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var lucidityLevel by remember { mutableFloatStateOf(0f) }
    var vividnessLevel by remember { mutableFloatStateOf(50f) }
    var isLucid by remember { mutableStateOf(false) }
    var isNightmare by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf(DreamMood.NEUTRAL) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val availableTags = listOf("vivid", "nightmare", "flying", "chase", "falling", "water", "animals", "people", "places", "teeth", "exam", "death")

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Dream", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Dream Title") },
                placeholder = { Text("Give your dream a title...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Dream Description") },
                placeholder = { Text("Describe your dream in detail... What happened? How did you feel?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Voice recording button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceInputButton(
                    snackbarHostState = snackbarHostState,
                    onRecordingComplete = { transcribedText ->
                        content = if (content.isBlank()) transcribedText else "$content\n$transcribedText"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lucidity slider
            Text(
                text = "Lucidity Level: ${lucidityLevel.toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                color = NeonPurple
            )
            Slider(
                value = lucidityLevel,
                onValueChange = { lucidityLevel = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonPurple,
                    activeTrackColor = NeonPurple
                )
            )

            // Vividness slider
            Text(
                text = "Vividness Level: ${vividnessLevel.toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                color = NeonCyan
            )
            Slider(
                value = vividnessLevel,
                onValueChange = { vividnessLevel = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isLucid,
                        onCheckedChange = { isLucid = it },
                        colors = CheckboxDefaults.colors(checkmarkColor = LucidGreen)
                    )
                    Text("Lucid", color = LucidGreen, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isNightmare,
                        onCheckedChange = { isNightmare = it },
                        colors = CheckboxDefaults.colors(checkmarkColor = NightmareRed)
                    )
                    Text("Nightmare", color = NightmareRed, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood selector
            Text(
                text = "Mood",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DreamMood.entries.forEach { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = mood },
                        label = { Text("${mood.emoji} ${mood.displayName}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Tag chips in a flow-like layout
            val tagRows = availableTags.chunked(4)
            tagRows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { tag ->
                        FilterChip(
                            selected = tag in selectedTags,
                            onClick = {
                                selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        viewModel.saveDream(
                            title = title.ifBlank { "Untitled Dream" },
                            content = content,
                            lucidityLevel = lucidityLevel.toInt(),
                            vividnessLevel = vividnessLevel.toInt(),
                            isLucid = isLucid,
                            isNightmare = isNightmare,
                            mood = selectedMood.name.lowercase(),
                            tags = selectedTags.toList()
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Dream", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    focusedLabelColor = NeonPurple,
    cursorColor = NeonPurple
)

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = { label() },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonPurple.copy(alpha = 0.2f),
            selectedLabelColor = NeonPurple
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
