package com.ldreams.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "dreams")
data class DreamEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Date = Date(),
    val lucidityLevel: Int = 0, // 0-100
    val vividnessLevel: Int = 0, // 0-100
    val isLucid: Boolean = false,
    val isNightmare: Boolean = false,
    val mood: String = "neutral", // happy, sad, anxious, peaceful, excited, fearful, neutral
    val tags: List<String> = emptyList(),
    val emotions: List<String> = emptyList(),
    val dreamSigns: List<String> = emptyList(),
    val xpEarned: Int = 0
)

@Entity(tableName = "reality_checks")
data class RealityCheck(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Date = Date(),
    val completed: Boolean = false,
    val checkType: String = "default", // default, finger_count, clock, nose_pinch
    val wasDreaming: Boolean = false
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Date? = null,
    val isUnlocked: Boolean = false
)

data class UserStats(
    val dreamStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalDreams: Int = 0,
    val lucidDreamCount: Int = 0,
    val lucidDreamPercentage: Float = 0f,
    val totalRealityChecks: Int = 0,
    val completedRealityChecks: Int = 0,
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val xpToNextLevel: Int = 100
)

data class DreamAnalysis(
    val recurringThemes: List<String> = emptyList(),
    val commonEmotions: List<Pair<String, Int>> = emptyList(),
    val dreamSigns: List<String> = emptyList(),
    val lucidityTrend: List<Pair<String, Int>> = emptyList(),
    val averageVividness: Float = 0f,
    val totalDreamsAnalyzed: Int = 0,
    val weeklyDreamCount: Int = 0,
    val monthlyDreamCount: Int = 0,
    val symbols: List<Pair<String, String>> = emptyList(),
    val tips: List<String> = emptyList(),
    val clarityScore: Int = 0,
    val emotionIntensities: Map<String, Float> = emptyMap()
)

enum class DreamMood(val displayName: String, val emoji: String) {
    HAPPY("Happy", "😊"),
    SAD("Sad", "😢"),
    ANXIOUS("Anxious", "😰"),
    PEACEFUL("Peaceful", "😌"),
    EXCITED("Excited", "🤩"),
    FEARFUL("Fearful", "😨"),
    NEUTRAL("Neutral", "😐");

    companion object {
        fun fromString(value: String): DreamMood =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NEUTRAL
    }
}
