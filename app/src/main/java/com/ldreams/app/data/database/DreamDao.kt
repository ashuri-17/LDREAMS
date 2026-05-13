package com.ldreams.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ldreams.app.data.models.DreamEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DreamDao {
    @Query("SELECT * FROM dreams ORDER BY timestamp DESC")
    fun getAllDreams(): Flow<List<DreamEntry>>

    @Query("SELECT * FROM dreams ORDER BY timestamp DESC")
    suspend fun getAllDreamsList(): List<DreamEntry>

    @Query("SELECT * FROM dreams WHERE id = :id")
    suspend fun getDreamById(id: Long): DreamEntry?

    @Query("SELECT * FROM dreams WHERE id = :id")
    fun getDreamByIdFlow(id: Long): Flow<DreamEntry?>

    @Query("SELECT * FROM dreams WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDreams(query: String): Flow<List<DreamEntry>>

    @Query("SELECT * FROM dreams WHERE tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    fun getDreamsByTag(tag: String): Flow<List<DreamEntry>>

    @Query("SELECT * FROM dreams WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getDreamsBetween(start: Date, end: Date): Flow<List<DreamEntry>>

    @Query("SELECT * FROM dreams WHERE isLucid = 1 ORDER BY timestamp DESC")
    fun getLucidDreams(): Flow<List<DreamEntry>>

    @Query("SELECT COUNT(*) FROM dreams")
    fun getDreamCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dreams WHERE isLucid = 1")
    fun getLucidDreamCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dreams WHERE timestamp >= :since")
    fun getDreamCountSince(since: Date): Flow<Int>

    @Query("SELECT AVG(lucidityLevel) FROM dreams")
    fun getAverageLucidity(): Flow<Float>

    @Query("SELECT AVG(vividnessLevel) FROM dreams")
    fun getAverageVividness(): Flow<Float>

    @Query("SELECT SUM(xpEarned) FROM dreams")
    fun getTotalXp(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(dream: DreamEntry): Long

    @Update
    suspend fun updateDream(dream: DreamEntry)

    @Delete
    suspend fun deleteDream(dream: DreamEntry)

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun deleteDreamById(id: Long)
}
