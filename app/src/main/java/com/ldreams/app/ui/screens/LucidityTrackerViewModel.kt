package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.data.repository.DreamRepository
import com.ldreams.app.data.repository.RealityCheckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LucidityStats(
    val lucidityScore: Int = 0,
    val totalLucidDreams: Int = 0,
    val totalDreams: Int = 0,
    val lucidRate: Int = 0,
    val avgVividness: Int = 0,
    val avgLucidity: Int = 0
)

@HiltViewModel
class LucidityTrackerViewModel @Inject constructor(
    private val dreamRepository: DreamRepository,
    private val realityCheckRepository: RealityCheckRepository
) : ViewModel() {

    private val _lucidityStats = MutableStateFlow(LucidityStats())
    val lucidityStats: StateFlow<LucidityStats> = _lucidityStats.asStateFlow()

    /** All dreams list for chart rendering. */
    private val _allDreams = MutableStateFlow<List<DreamEntry>>(emptyList())
    val allDreams: StateFlow<List<DreamEntry>> = _allDreams.asStateFlow()

    /** Completed reality check count for chart progress ring. */
    private val _rcCompleted = MutableStateFlow(0)
    val rcCompleted: StateFlow<Int> = _rcCompleted.asStateFlow()

    /** Total reality check count for chart progress ring. */
    private val _rcTotal = MutableStateFlow(0)
    val rcTotal: StateFlow<Int> = _rcTotal.asStateFlow()

    init {
        // Lucidity stats combine
        viewModelScope.launch {
            combine(
                dreamRepository.getLucidDreamCount(),
                dreamRepository.getDreamCount(),
                dreamRepository.getAverageVividness(),
                dreamRepository.getAverageLucidity()
            ) { lucidCount, totalCount, avgVivid, avgLucid ->
                val lucidRate = if (totalCount > 0) (lucidCount * 100 / totalCount) else 0
                val score = ((lucidRate * 2) + avgLucid.toInt() + avgVivid.toInt()) / 4

                LucidityStats(
                    lucidityScore = score.coerceIn(0, 100),
                    totalLucidDreams = lucidCount,
                    totalDreams = totalCount,
                    lucidRate = lucidRate,
                    avgVividness = avgVivid.toInt(),
                    avgLucidity = avgLucid.toInt()
                )
            }.collect { _lucidityStats.value = it }
        }

        // All dreams for charts
        viewModelScope.launch {
            dreamRepository.getAllDreams().collect { _allDreams.value = it }
        }

        // Reality check stats for charts
        viewModelScope.launch {
            realityCheckRepository.getAllChecks().collect { checks ->
                _rcTotal.value = checks.size
                _rcCompleted.value = checks.count { it.completed }
            }
        }
    }
}
