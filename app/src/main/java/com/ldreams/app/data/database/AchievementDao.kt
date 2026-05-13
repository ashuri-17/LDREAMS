package com.ldreams.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ldreams.app.data.models.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockAchievement(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedCount(): Flow<Int>
}
