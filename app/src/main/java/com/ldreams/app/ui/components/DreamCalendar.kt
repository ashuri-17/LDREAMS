package com.ldreams.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.ui.theme.LucidGreen
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
 * Monthly calendar grid composable.
 *
 * @param dreams Full list of dream entries; days with dreams get highlighted indicators.
 * @param selectedDate Currently selected date (or null if none selected).
 * @param onDaySelected Called with the tapped day's Date, or null when month changes.
 * @param modifier Modifier for the outer wrapper.
 */
@Composable
fun DreamCalendar(
    dreams: List<DreamEntry>,
    selectedDate: Date?,
    onDaySelected: (Date?) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { Calendar.getInstance() }
    val dateKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val monthLabelFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }

    // Track the current month being displayed (stored as the first millisecond of the month)
    var currentMonthStart by remember { mutableStateOf(monthStartDate(Date())) }

    // Build a map: "yyyy-MM-dd" -> list of dreams on that day
    val dayDreamsMap = remember(dreams, dateKeyFormat) {
        val map = mutableMapOf<String, MutableList<DreamEntry>>()
        for (dream in dreams) {
            val key = dateKeyFormat.format(dream.timestamp)
            map.getOrPut(key) { mutableListOf() }.add(dream)
        }
        map
    }

    // Pre-compute the grid: weeks of (dayNumber, isCurrentMonth) pairs
    val gridData = remember(currentMonthStart) {
        val cal = Calendar.getInstance().apply { time = currentMonthStart }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        val prevCal = Calendar.getInstance().apply {
            time = currentMonthStart
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val weeks = mutableListOf<List<Pair<Int, Boolean>>>()
        var week = mutableListOf<Pair<Int, Boolean>>()

        // Trailing days from previous month
        for (i in 0 until firstDayOfWeek) {
            week.add(Pair(daysInPrevMonth - firstDayOfWeek + i + 1, false))
        }

        // Current month days
        for (day in 1..daysInMonth) {
            week.add(Pair(day, true))
            if (week.size == 7) {
                weeks.add(week)
                week = mutableListOf()
            }
        }

        // Leading days from next month
        if (week.isNotEmpty()) {
            var nextDay = 1
            while (week.size < 7) {
                week.add(Pair(nextDay++, false))
            }
            weeks.add(week)
        }

        weeks
    }

    // Helper to get a Date for a given day number in the current month
    fun dateForDay(day: Int): Date {
        val cal = Calendar.getInstance().apply { time = currentMonthStart }
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.time
    }

    fun isToday(day: Int): Boolean {
        val cal = Calendar.getInstance().apply { time = dateForDay(day) }
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    fun isSelectedDay(day: Int): Boolean {
        if (selectedDate == null) return false
        val cal = Calendar.getInstance().apply { time = dateForDay(day) }
        val selCal = Calendar.getInstance().apply { time = selectedDate }
        return cal.get(Calendar.YEAR) == selCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR)
    }

    fun dreamCountForDay(day: Int): Int {
        val key = dateKeyFormat.format(dateForDay(day))
        return dayDreamsMap[key]?.size ?: 0
    }

    fun hasLucidOnDay(day: Int): Boolean {
        val key = dateKeyFormat.format(dateForDay(day))
        return dayDreamsMap[key]?.any { it.isLucid } == true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // --- Month navigation ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance().apply { time = currentMonthStart }
                    cal.add(Calendar.MONTH, -1)
                    currentMonthStart = monthStartDate(cal.time)
                    onDaySelected(null)
                }) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = TextSecondary
                    )
                }

                Text(
                    text = monthLabelFormat.format(currentMonthStart),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = {
                    val cal = Calendar.getInstance().apply { time = currentMonthStart }
                    cal.add(Calendar.MONTH, 1)
                    currentMonthStart = monthStartDate(cal.time)
                    onDaySelected(null)
                }) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Day-of-week headers ---
            Row(modifier = Modifier.fillMaxWidth()) {
                val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                dayHeaders.forEach { day ->
                    Text(
                        text = day.take(1),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Calendar grid ---
            for ((weekIndex, week) in gridData.withIndex()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for ((dayNumber, isCurrentMonth) in week) {
                        val hasDreams = isCurrentMonth && dreamCountForDay(dayNumber) > 0
                        val hasLucid = isCurrentMonth && hasLucidOnDay(dayNumber)
                        val todayFlag = isCurrentMonth && isToday(dayNumber)
                        val selected = isCurrentMonth && isSelectedDay(dayNumber)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .then(
                                    if (todayFlag)
                                        Modifier.border(
                                            width = 1.dp,
                                            color = NeonPurple.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                    else Modifier
                                )
                                .clip(CircleShape)
                                .then(
                                    if (selected)
                                        Modifier.background(NeonPurple.copy(alpha = 0.25f))
                                    else Modifier
                                )
                                .clickable(enabled = isCurrentMonth) {
                                    onDaySelected(dateForDay(dayNumber))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Day number
                            Text(
                                text = dayNumber.toString(),
                                color = when {
                                    !isCurrentMonth -> TextMuted.copy(alpha = 0.3f)
                                    selected -> NeonPurple
                                    else -> TextPrimary
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (todayFlag) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )

                            // Dream indicator (small circle below the number)
                            if (hasDreams) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 2.dp)
                                        .size(if (hasLucid) 6.dp else 4.dp)
                                        .clip(CircleShape)
                                        .background(if (hasLucid) LucidGreen else NeonPurple)
                                )
                            }
                        }
                    }
                }
            }

            // --- Dream count for selected day ---
            if (selectedDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val key = dateKeyFormat.format(selectedDate)
                val count = dayDreamsMap[key]?.size ?: 0
                val displayFmt = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US) }
                Text(
                    text = if (count > 0) "$count dream${if (count != 1) "s" else ""} on ${displayFmt.format(selectedDate)}"
                    else "No dreams on ${displayFmt.format(selectedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Returns a Date representing midnight of the first day of the month containing [date].
 */
private fun monthStartDate(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}
