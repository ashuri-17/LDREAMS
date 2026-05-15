package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.data.repository.DreamRepository
import com.ldreams.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DreamDetailViewModel @Inject constructor(
    private val dreamRepository: DreamRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _dream = MutableStateFlow<DreamEntry?>(null)
    val dream: StateFlow<DreamEntry?> = _dream.asStateFlow()

    fun loadDream(id: Long) {
        viewModelScope.launch {
            dreamRepository.getDreamByIdFlow(id).collect { d ->
                _dream.value = d
            }
        }
    }

    fun deleteDream(id: Long) {
        viewModelScope.launch {
            val dream = dreamRepository.getDreamById(id)
            val xpEarned = dream?.xpEarned ?: 0
            dreamRepository.deleteDreamById(id)
            if (xpEarned > 0) {
                preferencesRepository.addXp(-xpEarned)
            }
        }
    }
}
