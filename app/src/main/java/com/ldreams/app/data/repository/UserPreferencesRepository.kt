package com.ldreams.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "ldreams_prefs")

data class UserPreferences(
    val realityCheckEnabled: Boolean = true,
    val realityCheckFrequency: Int = 4,
    val realityCheckStartHour: Int = 8,
    val realityCheckEndHour: Int = 22,
    val morningReminderEnabled: Boolean = true,
    val morningReminderHour: Int = 7,
    val morningReminderMinute: Int = 30,
    val bedtimeReminderEnabled: Boolean = true,
    val bedtimeReminderHour: Int = 21,
    val bedtimeReminderMinute: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val privacyLockEnabled: Boolean = false,
    val darkThemeEnabled: Boolean = true,
    val dreamStreak: Int = 0,
    val longestDreamStreak: Int = 0,
    val userLevel: Int = 1,
    val userXp: Int = 0
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val REALITY_CHECK_ENABLED = booleanPreferencesKey("reality_check_enabled")
        val REALITY_CHECK_FREQUENCY = intPreferencesKey("reality_check_frequency")
        val REALITY_CHECK_START_HOUR = intPreferencesKey("reality_check_start_hour")
        val REALITY_CHECK_END_HOUR = intPreferencesKey("reality_check_end_hour")
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_REMINDER_HOUR = intPreferencesKey("morning_reminder_hour")
        val MORNING_REMINDER_MINUTE = intPreferencesKey("morning_reminder_minute")
        val BEDTIME_REMINDER_ENABLED = booleanPreferencesKey("bedtime_reminder_enabled")
        val BEDTIME_REMINDER_HOUR = intPreferencesKey("bedtime_reminder_hour")
        val BEDTIME_REMINDER_MINUTE = intPreferencesKey("bedtime_reminder_minute")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val PRIVACY_LOCK_ENABLED = booleanPreferencesKey("privacy_lock_enabled")
        val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
        val DREAM_STREAK = intPreferencesKey("dream_streak")
        val LONGEST_DREAM_STREAK = intPreferencesKey("longest_dream_streak")
        val USER_LEVEL = intPreferencesKey("user_level")
        val USER_XP = intPreferencesKey("user_xp")
        val PROGRAM_CURRENT_DAY = intPreferencesKey("program_current_day")
        val PROGRAM_COMPLETED_DAYS = stringSetPreferencesKey("program_completed_days")
        val PROGRAM_COMPLETED_TASKS = stringSetPreferencesKey("program_completed_tasks")
    }

    val programCurrentDay: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.PROGRAM_CURRENT_DAY] ?: 1
    }

    val programCompletedDays: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PROGRAM_COMPLETED_DAYS] ?: emptySet()
    }

    val programCompletedTasks: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PROGRAM_COMPLETED_TASKS] ?: emptySet()
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            realityCheckEnabled = prefs[Keys.REALITY_CHECK_ENABLED] ?: true,
            realityCheckFrequency = prefs[Keys.REALITY_CHECK_FREQUENCY] ?: 10,
            realityCheckStartHour = prefs[Keys.REALITY_CHECK_START_HOUR] ?: 8,
            realityCheckEndHour = prefs[Keys.REALITY_CHECK_END_HOUR] ?: 22,
            morningReminderEnabled = prefs[Keys.MORNING_REMINDER_ENABLED] ?: true,
            morningReminderHour = prefs[Keys.MORNING_REMINDER_HOUR] ?: 7,
            morningReminderMinute = prefs[Keys.MORNING_REMINDER_MINUTE] ?: 30,
            bedtimeReminderEnabled = prefs[Keys.BEDTIME_REMINDER_ENABLED] ?: true,
            bedtimeReminderHour = prefs[Keys.BEDTIME_REMINDER_HOUR] ?: 21,
            bedtimeReminderMinute = prefs[Keys.BEDTIME_REMINDER_MINUTE] ?: 0,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            privacyLockEnabled = prefs[Keys.PRIVACY_LOCK_ENABLED] ?: false,
            darkThemeEnabled = prefs[Keys.DARK_THEME_ENABLED] ?: true,
            dreamStreak = prefs[Keys.DREAM_STREAK] ?: 0,
            longestDreamStreak = prefs[Keys.LONGEST_DREAM_STREAK] ?: 0,
            userLevel = prefs[Keys.USER_LEVEL] ?: 1,
            userXp = prefs[Keys.USER_XP] ?: 0
        )
    }

    suspend fun updateRealityCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REALITY_CHECK_ENABLED] = enabled }
    }

    suspend fun updateRealityCheckFrequency(freq: Int) {
        context.dataStore.edit { it[Keys.REALITY_CHECK_FREQUENCY] = freq }
    }

    suspend fun updateRealityCheckHours(start: Int, end: Int) {
        context.dataStore.edit {
            it[Keys.REALITY_CHECK_START_HOUR] = start
            it[Keys.REALITY_CHECK_END_HOUR] = end
        }
    }

    suspend fun updateMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.MORNING_REMINDER_ENABLED] = enabled
            it[Keys.MORNING_REMINDER_HOUR] = hour
            it[Keys.MORNING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateBedtimeReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.BEDTIME_REMINDER_ENABLED] = enabled
            it[Keys.BEDTIME_REMINDER_HOUR] = hour
            it[Keys.BEDTIME_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateStreak(streak: Int, longestStreak: Int) {
        context.dataStore.edit {
            it[Keys.DREAM_STREAK] = streak
            it[Keys.LONGEST_DREAM_STREAK] = longestStreak
        }
    }

    suspend fun updateLevel(level: Int, xp: Int) {
        context.dataStore.edit {
            it[Keys.USER_LEVEL] = level
            it[Keys.USER_XP] = xp
        }
    }

    suspend fun updatePrivacyLock(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PRIVACY_LOCK_ENABLED] = enabled }
    }

    suspend fun updateProgramCurrentDay(day: Int) {
        context.dataStore.edit { it[Keys.PROGRAM_CURRENT_DAY] = day }
    }

    suspend fun updateProgramCompletedDays(days: Set<String>) {
        context.dataStore.edit { it[Keys.PROGRAM_COMPLETED_DAYS] = days }
    }

    suspend fun updateProgramCompletedTasks(tasks: Set<String>) {
        context.dataStore.edit { it[Keys.PROGRAM_COMPLETED_TASKS] = tasks }
    }

    suspend fun addXp(xp: Int) {
        context.dataStore.edit { prefs ->
            val currentXp = (prefs[Keys.USER_XP] ?: 0) + xp
            val currentLevel = prefs[Keys.USER_LEVEL] ?: 1
            val xpNeeded = currentLevel * 100
            if (currentXp >= xpNeeded) {
                prefs[Keys.USER_LEVEL] = currentLevel + 1
                prefs[Keys.USER_XP] = currentXp - xpNeeded
            } else {
                prefs[Keys.USER_XP] = currentXp
            }
        }
    }
}
