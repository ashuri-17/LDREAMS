package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.repository.RealityCheckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RealityCheckStats(
    val todayCompleted: Int = 0,
    val todayTotal: Int = 0,
    val allTimeCompleted: Int = 0
)

@HiltViewModel
class RealityCheckViewModel @Inject constructor(
    private val repository: RealityCheckRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(RealityCheckStats())
    val stats: StateFlow<RealityCheckStats> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTodayCompletedCount(),
                repository.getTodayTotalCount(),
                repository.getCompletedCount()
            ) { todayCompleted, todayTotal, allTime ->
                RealityCheckStats(
                    todayCompleted = todayCompleted,
                    todayTotal = todayTotal,
                    allTimeCompleted = allTime
                )
            }.collect { _stats.value = it }
        }
    }

    fun performCheck() {
        viewModelScope.launch {
            repository.recordCheck(completed = true)
        }
    }
}
