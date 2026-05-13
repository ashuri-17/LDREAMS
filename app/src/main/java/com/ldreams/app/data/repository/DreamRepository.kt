package com.ldreams.app.data.repository

import com.ldreams.app.data.database.DreamDao
import com.ldreams.app.data.models.DreamEntry
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DreamRepository @Inject constructor(
    private val dreamDao: DreamDao
) {
    fun getAllDreams(): Flow<List<DreamEntry>> = dreamDao.getAllDreams()

    suspend fun getAllDreamsList(): List<DreamEntry> = dreamDao.getAllDreamsList()

    suspend fun getDreamById(id: Long): DreamEntry? = dreamDao.getDreamById(id)

    fun getDreamByIdFlow(id: Long): Flow<DreamEntry?> = dreamDao.getDreamByIdFlow(id)

    fun searchDreams(query: String): Flow<List<DreamEntry>> = dreamDao.searchDreams(query)

    fun getDreamsByTag(tag: String): Flow<List<DreamEntry>> = dreamDao.getDreamsByTag(tag)

    fun getDreamCount(): Flow<Int> = dreamDao.getDreamCount()

    fun getLucidDreamCount(): Flow<Int> = dreamDao.getLucidDreamCount()

    fun getAverageLucidity(): Flow<Float> = dreamDao.getAverageLucidity()

    fun getAverageVividness(): Flow<Float> = dreamDao.getAverageVividness()

    fun getTotalXp(): Flow<Int?> = dreamDao.getTotalXp()

    fun getThisWeekCount(): Flow<Int> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return dreamDao.getDreamCountSince(cal.time)
    }

    fun getLucidDreams(): Flow<List<DreamEntry>> = dreamDao.getLucidDreams()

    suspend fun saveDream(dream: DreamEntry): Long {
        val xp = calculateXp(dream)
        return dreamDao.insertDream(dream.copy(xpEarned = xp))
    }

    suspend fun updateDream(dream: DreamEntry) = dreamDao.updateDream(dream)

    suspend fun deleteDream(dream: DreamEntry) = dreamDao.deleteDream(dream)

    suspend fun deleteDreamById(id: Long) = dreamDao.deleteDreamById(id)

    private fun calculateXp(dream: DreamEntry): Int {
        var xp = 10 // base XP
        xp += (dream.content.length / 50) * 2 // length bonus
        xp += dream.lucidityLevel / 10 // lucidity bonus
        xp += dream.vividnessLevel / 10 // vividness bonus
        if (dream.isLucid) xp += 20 // lucid dream bonus
        if (dream.tags.isNotEmpty()) xp += 5
        if (dream.mood != "neutral") xp += 3
        return xp.coerceAtMost(100)
    }
}
