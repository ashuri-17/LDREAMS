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
            "realized" to 30, "knew" to 20, "aware" to 25, "conscious" to 30,
            "control" to 20, "focused" to 15, "notice" to 15, "strange" to 10,
            "different" to 10, "unusual" to 10, "impossible" to 15,
            "wait" to 10, "this can't" to 20, "am i" to 15, "dreaming" to 20
        )
        var lucidityScore = 0
        for ((word, points) in lucidIndicators) {
            if (text.contains(word)) lucidityScore += points
        }
        val lucidity = lucidityScore.coerceIn(0, 100)

        // Vividness indicators (words suggesting detail/sensory richness)
        val vividIndicators = listOf(
            "color" to 15, "bright" to 15, "vivid" to 20, "clear" to 15,
            "detail" to 15, "beautiful" to 10, "felt" to 10, "touched" to 10,
            "smell" to 15, "taste" to 15, "sound" to 10, "heard" to 10,
            "warm" to 10, "cold" to 10, "soft" to 10, "texture" to 15,
            "remember" to 10, "exactly" to 10, "vibrant" to 20, "intense" to 15
        )
        var vividnessScore = 50 // base vividness
        for ((word, points) in vividIndicators) {
            if (text.contains(word)) vividnessScore += points
        }
        val vividness = vividnessScore.coerceIn(0, 100)

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
