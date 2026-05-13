package com.ldreams.app.data.repository

import com.ldreams.app.data.database.RealityCheckDao
import com.ldreams.app.data.models.RealityCheck
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealityCheckRepository @Inject constructor(
    private val realityCheckDao: RealityCheckDao
) {
    fun getAllChecks(): Flow<List<RealityCheck>> = realityCheckDao.getAllChecks()

    fun getCompletedCount(): Flow<Int> = realityCheckDao.getCompletedCount()

    fun getTodayCompletedCount(): Flow<Int> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return realityCheckDao.getCompletedCountSince(cal.time)
    }

    fun getTodayTotalCount(): Flow<Int> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return realityCheckDao.getTotalCountSince(cal.time)
    }

    suspend fun getTodayCompletedCountSuspend(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return realityCheckDao.getCompletedCountSinceSuspend(cal.time)
    }

    suspend fun getTodayTotalCountSuspend(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return realityCheckDao.getTotalCountSinceSuspend(cal.time)
    }

    fun getDreamingConfirmedCount(): Flow<Int> = realityCheckDao.getDreamingConfirmedCount()

    suspend fun recordCheck(completed: Boolean, wasDreaming: Boolean = false) {
        realityCheckDao.insertCheck(
            RealityCheck(
                completed = completed,
                wasDreaming = wasDreaming
            )
        )
    }
}
