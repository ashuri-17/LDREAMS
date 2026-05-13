package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.LucidProgramDay
import com.ldreams.app.data.models.LucidProgramRepository
import com.ldreams.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LucidProgramUiState(
    val days: List<LucidProgramDay> = emptyList(),
    val currentDay: Int = 1,
    val completedDays: Set<Int> = emptySet(),
    val completedTasks: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LucidProgramViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LucidProgramUiState())
    val uiState: StateFlow<LucidProgramUiState> = _uiState.asStateFlow()

    private val _rewardEvent = MutableStateFlow<Int?>(null)
    val rewardEvent: StateFlow<Int?> = _rewardEvent.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.programCurrentDay,
                userPreferencesRepository.programCompletedDays,
                userPreferencesRepository.programCompletedTasks
            ) { currentDay, completedDays, completedTasks ->
                val allDays = LucidProgramRepository.getAllDays().map { day ->
                    day.copy(
                        isUnlocked = day.day <= currentDay || day.day.toString() in completedDays,
                        isCompleted = day.day.toString() in completedDays
                    )
                }
                LucidProgramUiState(
                    days = allDays,
                    currentDay = currentDay,
                    completedDays = completedDays.mapNotNull { it.toIntOrNull() }.toSet(),
                    completedTasks = completedTasks,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun completeTask(day: Int, taskIndex: Int) {
        viewModelScope.launch {
            val taskKey = "${day}_$taskIndex"
            val currentTasks = _uiState.value.completedTasks
            val newTasks = if (taskKey in currentTasks) {
                currentTasks - taskKey
            } else {
                currentTasks + taskKey
            }
            userPreferencesRepository.updateProgramCompletedTasks(newTasks)
        }
    }

    fun isTaskCompleted(day: Int, taskIndex: Int): Boolean {
        return "${day}_$taskIndex" in _uiState.value.completedTasks
    }

    fun areAllTasksCompleted(day: Int): Boolean {
        return (0..3).all { "${day}_$it" in _uiState.value.completedTasks }
    }

    fun completeDay(day: Int) {
        viewModelScope.launch {
            val newCompletedDays = _uiState.value.completedDays + day
            userPreferencesRepository.updateProgramCompletedDays(
                newCompletedDays.map { it.toString() }.toSet()
            )

            // Add XP reward
            userPreferencesRepository.addXp(50)
            _rewardEvent.value = 50

            // Unlock next day
            if (day < 7 && _uiState.value.currentDay <= day) {
                userPreferencesRepository.updateProgramCurrentDay(day + 1)
            }
        }
    }

    fun unlockNextDay() {
        viewModelScope.launch {
            val nextDay = _uiState.value.currentDay + 1
            if (nextDay <= 7) {
                userPreferencesRepository.updateProgramCurrentDay(nextDay)
            }
        }
    }

    fun dismissReward() {
        _rewardEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
    }
}
