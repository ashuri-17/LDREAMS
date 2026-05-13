package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ldreams.app.data.repository.UserPreferences

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences = preferencesRepository.preferences

    fun toggleRealityCheck(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateRealityCheckEnabled(enabled) }
    }

    fun toggleMorningReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMorningReminder(enabled, 7, 30)
        }
    }

    fun toggleBedtimeReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateBedtimeReminder(enabled, 21, 0)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            // sound toggling logic
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            // vibration toggling logic
        }
    }

    fun togglePrivacyLock(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updatePrivacyLock(enabled) }
    }
}
