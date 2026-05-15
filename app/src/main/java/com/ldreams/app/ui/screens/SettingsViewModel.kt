package com.ldreams.app.ui.screens

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.repository.UserPreferencesRepository
import com.ldreams.app.data.repository.dataStore
import com.ldreams.app.service.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ldreams.app.data.repository.UserPreferences

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences = preferencesRepository.preferences

    fun toggleRealityCheck(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateRealityCheckEnabled(enabled)
            if (enabled) {
                NotificationScheduler.scheduleRealityChecks(context)
            } else {
                // Cancel only reality check work, keep other notifications
                val workManager = androidx.work.WorkManager.getInstance(context)
                workManager.cancelUniqueWork("reality_check_work")
            }
        }
    }

    fun toggleMorningReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMorningReminder(enabled, 7, 30)
            if (enabled) {
                NotificationScheduler.scheduleMorningReminder(context)
            } else {
                val workManager = androidx.work.WorkManager.getInstance(context)
                workManager.cancelUniqueWork("morning_reminder_work")
            }
        }
    }

    fun toggleBedtimeReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateBedtimeReminder(enabled, 21, 0)
            if (enabled) {
                NotificationScheduler.scheduleBedtimeReminder(context)
            } else {
                val workManager = androidx.work.WorkManager.getInstance(context)
                workManager.cancelUniqueWork("bedtime_reminder_work")
            }
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[booleanPreferencesKey("sound_enabled")] = enabled }
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[booleanPreferencesKey("vibration_enabled")] = enabled }
        }
    }

}
