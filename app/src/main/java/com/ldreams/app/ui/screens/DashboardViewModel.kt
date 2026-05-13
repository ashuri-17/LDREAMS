package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.repository.DreamRepository
import com.ldreams.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val dreamStreak: Int = 0,
    val totalDreams: Int = 0,
    val lucidCount: Int = 0,
    val lucidPercentage: Float = 0f,
    val level: Int = 1,
    val xp: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dreamRepository: DreamRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dreamRepository.getDreamCount(),
                dreamRepository.getLucidDreamCount(),
                dreamRepository.getTotalXp(),
                preferencesRepository.preferences
            ) { dreamCount, lucidCount, totalXp, prefs ->
                DashboardUiState(
                    dreamStreak = prefs.dreamStreak,
                    totalDreams = dreamCount,
                    lucidCount = lucidCount,
                    lucidPercentage = if (dreamCount > 0) lucidCount.toFloat() / dreamCount * 100 else 0f,
                    level = prefs.userLevel,
                    xp = prefs.userXp
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
