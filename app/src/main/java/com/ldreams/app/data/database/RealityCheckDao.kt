package com.ldreams.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ldreams.app.data.models.RealityCheck
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RealityCheckDao {
    @Query("SELECT * FROM reality_checks ORDER BY timestamp DESC")
    fun getAllChecks(): Flow<List<RealityCheck>>

    @Query("SELECT COUNT(*) FROM reality_checks WHERE completed = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reality_checks WHERE completed = 1 AND timestamp >= :since")
    fun getCompletedCountSince(since: Date): Flow<Int>

    @Query("SELECT COUNT(*) FROM reality_checks WHERE timestamp >= :since")
    fun getTotalCountSince(since: Date): Flow<Int>

    @Query("SELECT COUNT(*) FROM reality_checks WHERE completed = 1 AND timestamp >= :since")
    suspend fun getCompletedCountSinceSuspend(since: Date): Int

    @Query("SELECT COUNT(*) FROM reality_checks WHERE timestamp >= :since")
    suspend fun getTotalCountSinceSuspend(since: Date): Int

    @Query("SELECT COUNT(*) FROM reality_checks WHERE wasDreaming = 1")
    fun getDreamingConfirmedCount(): Flow<Int>

    @Insert
    suspend fun insertCheck(check: RealityCheck): Long
}
