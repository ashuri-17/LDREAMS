package com.ldreams.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.components.DreamCalendar
import com.ldreams.app.ui.components.DreamCard
import com.ldreams.app.ui.theme.NeonPurple
import com.ldreams.app.ui.theme.SurfaceCard
import com.ldreams.app.ui.theme.TextMuted
import com.ldreams.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamJournalScreen(
    navController: NavController,
    viewModel: DreamJournalViewModel = hiltViewModel()
) {
    val dreams by viewModel.allDreams.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState(initial = null)

    // Calendar mode state
    var isCalendarMode by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }

    val displayList = searchResults ?: dreams

    // Date key format for comparing dates (yyyy-MM-dd)
    val dateKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Filter dreams by the selected date
    val filteredDreams = remember(dreams, selectedDate, dateKeyFormat) {
        if (selectedDate == null) emptyList()
        else {
            val key = dateKeyFormat.format(selectedDate!!)
            dreams.filter { dateKeyFormat.format(it.timestamp) == key }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dream Journal",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_dream") },
                containerColor = NeonPurple
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Dream"
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            DreamBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // ---- Search bar + List/Calendar toggle ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchDreams(it)
                        },
                        placeholder = { Text("Search dreams...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // List / Calendar toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard.copy(alpha = 0.6f))
                    ) {
                        TextButton(
                            onClick = {
                                isCalendarMode = false
                                selectedDate = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (!isCalendarMode)
                                    NeonPurple.copy(alpha = 0.25f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "List",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (!isCalendarMode) NeonPurple else TextSecondary
                            )
                        }
                        TextButton(
                            onClick = { isCalendarMode = true },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (isCalendarMode)
                                    NeonPurple.copy(alpha = 0.25f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Calendar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isCalendarMode) NeonPurple else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---- Animated content switching ----
                Crossfade(
                    targetState = isCalendarMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { calendarMode ->
                    if (calendarMode) {
                        // ========== CALENDAR VIEW ==========
                        Column(modifier = Modifier.fillMaxSize()) {
                            DreamCalendar(
                                dreams = dreams,
                                selectedDate = selectedDate,
                                onDaySelected = { date ->
                                    selectedDate = if (selectedDate != null && date != null &&
                                        dateKeyFormat.format(date) == dateKeyFormat.format(selectedDate)
                                    ) {
                                        null // deselect if same day tapped again
                                    } else {
                                        date
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Filtered dreams for selected day
                            if (selectedDate != null) {
                                val dateFmt = remember {
                                    SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                                }
                                Text(
                                    text = "${filteredDreams.size} dream${if (filteredDreams.size != 1) "s" else ""} on ${dateFmt.format(selectedDate)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (filteredDreams.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No dreams on this day",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(bottom = 8.dp)
                                    ) {
                                        items(filteredDreams, key = { it.id }) { dream ->
                                            DreamCard(
                                                dream = dream,
                                                onClick = {
                                                    navController.navigate("dream_detail/${dream.id}")
                                                },
                                                )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = TextMuted.copy(alpha = 0.4f),
                                            modifier = Modifier.height(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Tap a day to view dreams",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ========== LIST VIEW (original behavior) ==========
                        if (displayList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.height(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "No dreams found" else "No dreams yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap + to write your first dream",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(displayList, key = { it.id }) { dream ->
                                    DreamCard(
                                        dream = dream,
                                        onClick = { navController.navigate("dream_detail/${dream.id}") },
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
