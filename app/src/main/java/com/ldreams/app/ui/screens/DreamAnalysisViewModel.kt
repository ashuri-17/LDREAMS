package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.DreamAnalysis
import com.ldreams.app.data.repository.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DreamAnalysisViewModel @Inject constructor(
    private val dreamRepository: DreamRepository
) : ViewModel() {

    private val _analysis = MutableStateFlow<DreamAnalysis?>(null)
    val analysis: StateFlow<DreamAnalysis?> = _analysis.asStateFlow()

    init {
        viewModelScope.launch {
            dreamRepository.getAllDreamsList().let { dreams ->
                if (dreams.size < 3) {
                    _analysis.value = null
                    return@launch
                }

                // Analyze dreams
                val allTags = dreams.flatMap { it.tags }
                val themeCounts = allTags.groupingBy { it }.eachCount()
                val recurringThemes = themeCounts.filter { it.value >= 2 }.map { it.key }

                val emotionCounts = dreams.groupBy { it.mood }.mapValues { it.value.size }
                    .entries.sortedByDescending { it.value }.take(5)
                    .map { (mood, count) -> Pair(mood.replaceFirstChar { it.uppercase() }, count) }

                val dreamSigns = listOf("Flying", "Falling", "Chased", "Teeth falling out", "Water")
                    .filter { sign -> dreams.any { it.tags.any { tag -> sign.lowercase().contains(tag) } || it.content.lowercase().contains(sign.lowercase()) } }

                val thisWeek = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time

                val thisMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time

                val weeklyCount = dreams.count { it.timestamp.after(thisWeek) }
                val monthlyCount = dreams.count { it.timestamp.after(thisMonth) }
                val avgVividness = if (dreams.isNotEmpty()) dreams.map { it.vividnessLevel }.average().toFloat() else 0f

                _analysis.value = DreamAnalysis(
                    recurringThemes = recurringThemes,
                    commonEmotions = emotionCounts,
                    dreamSigns = dreamSigns,
                    totalDreamsAnalyzed = dreams.size,
                    weeklyDreamCount = weeklyCount,
                    monthlyDreamCount = monthlyCount,
                    averageVividness = avgVividness
                )
            }
        }
    }
}
