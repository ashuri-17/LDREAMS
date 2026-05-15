package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.data.repository.DreamRepository
import com.ldreams.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddDreamViewModel @Inject constructor(
    private val dreamRepository: DreamRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun saveDream(
        title: String,
        content: String,
        lucidityLevel: Int,
        vividnessLevel: Int,
        isLucid: Boolean,
        isNightmare: Boolean,
        mood: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val dream = DreamEntry(
                title = title,
                content = content,
                lucidityLevel = lucidityLevel,
                vividnessLevel = vividnessLevel,
                isLucid = isLucid,
                isNightmare = isNightmare,
                mood = mood,
                tags = tags
            )
            val xp = dreamRepository.saveDream(dream)
            preferencesRepository.addXp(xp)
            // Update streak
            updateStreak()
        }
    }

    fun analyzeDreamContent(content: String): Pair<Int, Int> {
        if (content.isBlank()) return Pair(0, 50)

        val text = content.lowercase()

        // Lucidity indicators (words suggesting awareness)
        val lucidIndicators = listOf(
            "realized" to 25, "knew" to 15, "aware" to 20, "conscious" to 25,
            "control" to 20, "focused" to 15, "notice" to 15, "strange" to 10,
            "different" to 10, "unusual" to 10, "impossible" to 15,
            "wait" to 10, "am i" to 15, "dreaming" to 20, "lucid" to 30,
            "woke up" to 10, "inside" to 10, "could" to 10
        )
        var lucidityScore = 0
        for ((word, points) in lucidIndicators) {
            val count = text.split(word).size - 1
            lucidityScore += points * count
        }
        val lucidity = (lucidityScore + 5).coerceIn(0, 100)

        // Vividness indicators (words suggesting detail/sensory richness)
        val vividIndicators = listOf(
            "saw" to 8, "look" to 8, "see" to 8, "color" to 12, "bright" to 12,
            "vivid" to 18, "clear" to 12, "detail" to 15, "beautiful" to 10,
            "felt" to 10, "touched" to 10, "smell" to 12, "taste" to 12,
            "sound" to 10, "heard" to 10, "warm" to 8, "cold" to 8,
            "soft" to 8, "texture" to 12, "remember" to 10, "vibrant" to 18,
            "intense" to 12, "big" to 5, "huge" to 5, "dark" to 8,
            "light" to 8, "sky" to 8, "water" to 8, "room" to 5,
            "house" to 5, "face" to 8, "eyes" to 8, "hair" to 5
        )
        var vividnessScore = 0
        for ((word, points) in vividIndicators) {
            val count = text.split(word).size - 1
            vividnessScore += points * count
        }
        val vividness = (vividnessScore + 5).coerceIn(0, 100)

        return Pair(lucidity, vividness)
    }

    private suspend fun updateStreak() {
        val dreams = dreamRepository.getAllDreamsList()
        if (dreams.isEmpty()) return

        // Get unique dream dates sorted as "YYYY-DOY" strings
        val dateSet = dreams.map { dream ->
            val cal = Calendar.getInstance()
            cal.time = dream.timestamp
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }.distinct().sorted()

        if (dateSet.isEmpty()) return

        val cal = Calendar.getInstance()
        val today = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"

        val lastDate = dateSet.last()
        // Streak only counts if the most recent dream is from today or yesterday
        if (lastDate != today && lastDate != yesterday) {
            preferencesRepository.updateStreak(0, 0)
            return
        }

        var streak = 0
        var expected = lastDate
        for (i in dateSet.indices.reversed()) {
            if (dateSet[i] == expected) {
                streak++
                val parts = expected.split("-")
                val prevCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.DAY_OF_YEAR, parts[1].toInt())
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                expected = "${prevCal.get(Calendar.YEAR)}-${prevCal.get(Calendar.DAY_OF_YEAR)}"
            } else {
                break
            }
        }

        preferencesRepository.updateStreak(streak, streak)
    }
}
