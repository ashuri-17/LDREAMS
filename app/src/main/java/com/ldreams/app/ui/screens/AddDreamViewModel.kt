package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.data.repository.DreamRepository
import com.ldreams.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDreamViewModel @Inject constructor(
    private val dreamRepository: DreamRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun saveDream(
        title: String,
        content: String,
        lucidityLevel: Int,
        vividnessLevel: Int,
        isLucid: Boolean,
        isNightmare: Boolean,
        mood: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val dream = DreamEntry(
                title = title,
                content = content,
                lucidityLevel = lucidityLevel,
                vividnessLevel = vividnessLevel,
                isLucid = isLucid,
                isNightmare = isNightmare,
                mood = mood,
                tags = tags
            )
            val xp = dreamRepository.saveDream(dream)
            preferencesRepository.addXp(xp)
            // Update streak
            updateStreak()
        }
    }

    private suspend fun updateStreak() {
        val prefs = preferencesRepository.preferences
        // Simple streak update logic
        val currentStreak = 1 // In production, check consecutive days
        preferencesRepository.updateStreak(currentStreak, maxOf(currentStreak, 0))
    }
}
