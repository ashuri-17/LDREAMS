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
        if (content.isBlank()) return Pair(0, 0)

        val text = content.lowercase().trim()
        val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val wordCount = words.size
        if (wordCount < 3) return Pair(0, 0)

        // =========================================================
        // LUCIDITY ANALYSIS
        // =========================================================

        // Phrases that strongly suggest lucidity awareness (multi-word patterns)
        val lucidPhrases = listOf(
            "realized i" to 30, "knew i" to 20, "aware that" to 25,
            "conscious of" to 28, "control my" to 25, "control the" to 20,
            "notice that" to 20, "noticed i" to 25, "am i dreaming" to 40,
            "woke up" to 15, "inside the dream" to 30, "this is a dream" to 45,
            "i knew" to 20, "i realized" to 30, "i noticed" to 22,
            "taking control" to 30, "took control" to 30, "gained control" to 35,
            "became aware" to 35, "became conscious" to 35,
            "i was dreaming" to 25, "i'm dreaming" to 30,
            "fly" to 20 // flying = classic lucidity sign
        )

        val lucidWords = listOf(
            "lucid" to 35, "realize" to 22, "realized" to 25, "realizing" to 23,
            "aware" to 22, "awareness" to 25, "conscious" to 26, "consciousness" to 28,
            "control" to 18, "controlling" to 20, "controlled" to 18,
            "focus" to 14, "focused" to 16, "focusing" to 15,
            "notice" to 14, "noticed" to 17, "noticing" to 16,
            "strange" to 12, "weird" to 12, "odd" to 10, "unusual" to 12,
            "different" to 10, "bizarre" to 15, "impossible" to 16,
            "wait" to 8, "question" to 10, "wonder" to 10,
            "dreaming" to 22, "dream" to 8,
            "inside" to 8, "awake" to 15, "waking" to 12,
            "reality" to 18, "real" to 12, "surely" to 8,
            "decision" to 10, "choose" to 12, "chose" to 12,
            "deliberate" to 18, "intentional" to 20, "intend" to 15,
            "observe" to 12, "observing" to 14, "examine" to 12,
            "skeptical" to 18, "doubt" to 14, "suspect" to 15,
            "reflect" to 12, "reflection" to 14,
            "clarity" to 18, "clear-minded" to 22,
            "spinning" to 12, "stabilize" to 20
        )

        // Negation words that reverse score when preceding a keyword
        val negations = setOf("not", "don't", "dont", "doesn't", "didn't", "can't", "cant", "couldn't", "wont", "won't", "no", "never")

        // Count phrase matches (higher weight)
        var lucidityScore = 0.0
        for ((phrase, points) in lucidPhrases) {
            val count = text.split(phrase).size - 1
            lucidityScore += points * count * 1.5
        }

        // Count individual word matches with negation handling
        for ((word, points) in lucidWords) {
            val regex = "(?<![a-z])$word(?![a-z])".toRegex()
            val matches = regex.findAll(text).toList()
            if (matches.isEmpty()) continue

            for (match in matches) {
                // Check if the word immediately before this match is a negation
                val before = text.substring(0, match.range.first).trim()
                val lastWord = before.split("\\s+".toRegex()).lastOrNull()
                if (lastWord in negations) {
                    lucidityScore -= points * 0.5
                } else {
                    lucidityScore += points
                }
            }
        }

        // Bonus: first-person pronouns (self-awareness marker in dreams)
        val firstPersonCount = listOf("i ", " i", " me ", " my ", " myself ", "mine ")
            .sumOf { text.split(it).size - 1 }
        val firstPersonDensity = if (wordCount > 0) firstPersonCount.toDouble() / wordCount else 0.0
        lucidityScore += (firstPersonDensity * 80).coerceAtMost(30.0)

        // Bonus: questions asked (curiosity = awareness)
        val questionCount = text.count { it == '?' }
        lucidityScore += questionCount * 5.0

        // Bonus: exclamation marks (intensity of realization)
        val exclamationCount = text.count { it == '!' }
        lucidityScore += exclamationCount * 3.0

        // Scale by content length (diminishing returns)
        val lengthFactor = (wordCount.coerceAtMost(200) / 200.0)
        lucidityScore *= (0.5 + 0.5 * lengthFactor)

        val lucidity = lucidityScore.coerceIn(0.0, 100.0).toInt()


        // =========================================================
        // VIVIDNESS ANALYSIS
        // =========================================================

        // Sensory category keywords with weights
        val visualWords = listOf(
            "saw" to 6, "see" to 5, "look" to 5, "looked" to 6, "looking" to 5,
            "watch" to 5, "watched" to 6, "view" to 5,
            "color" to 12, "colour" to 12, "colorful" to 15, "colourful" to 15,
            "bright" to 12, "brighter" to 14, "brightest" to 16, "brightly" to 13,
            "dark" to 8, "darker" to 10, "darkness" to 10,
            "light" to 8, "lighting" to 10, "sunlight" to 10, "moonlight" to 10,
            "vivid" to 20, "vibrant" to 18, "vibrancy" to 16,
            "clear" to 10, "clarity" to 12, "clearly" to 10,
            "detail" to 14, "detailed" to 16, "details" to 14,
            "beautiful" to 10, "gorgeous" to 12, "stunning" to 12, "magnificent" to 14,
            "sky" to 7, "sunset" to 10, "sunrise" to 10, "rainbow" to 12,
            "shadow" to 8, "shadows" to 8, "silhouette" to 10,
            "flash" to 8, "glow" to 10, "glowing" to 12, "shimmer" to 12,
            "reflection" to 10, "mirror" to 8, "gleam" to 10,
            "blur" to 6, "blurry" to 8, "fade" to 6, "fading" to 7,
            "red" to 6, "blue" to 6, "green" to 6, "yellow" to 6, "gold" to 8,
            "purple" to 7, "white" to 5, "black" to 5, "pink" to 6,
            "paint" to 6, "picture" to 6, "scene" to 7, "landscape" to 8,
            "star" to 7, "stars" to 7, "moon" to 7, "sun" to 7, "cloud" to 6,
            "rain" to 7, "lightning" to 8, "thunder" to 7, "fire" to 8,
            "ocean" to 8, "sea" to 7, "river" to 7, "lake" to 7, "water" to 6,
            "garden" to 7, "forest" to 8, "mountain" to 8,
            "room" to 5, "house" to 5, "building" to 5, "city" to 6,
            "face" to 7, "eyes" to 7, "hair" to 5, "skin" to 6, "smile" to 6
        )

        val auditoryWords = listOf(
            "sound" to 12, "sounds" to 12, "sounded" to 10,
            "heard" to 10, "hear" to 8, "hearing" to 10,
            "listen" to 8, "listened" to 10, "listening" to 9,
            "loud" to 10, "loudly" to 10, "quiet" to 8, "silence" to 10,
            "whisper" to 10, "whispered" to 12, "voice" to 9, "voices" to 10,
            "music" to 10, "melody" to 12, "tune" to 8,
            "noise" to 8, "noises" to 8, "ringing" to 8,
            "echo" to 10, "echoed" to 12, "roar" to 8, "scream" to 9,
            "talk" to 6, "talking" to 7, "speak" to 7, "spoke" to 8,
            "buzz" to 7, "hum" to 6, "click" to 6, "crash" to 8,
            "footstep" to 8, "knock" to 7, "bell" to 7
        )

        val tactileWords = listOf(
            "felt" to 10, "feeling" to 8, "touch" to 10, "touched" to 12,
            "touching" to 10, "hold" to 7, "held" to 8, "grab" to 7,
            "warm" to 9, "warmth" to 10, "hot" to 8, "burn" to 8,
            "cold" to 9, "cool" to 7, "freeze" to 8, "frozen" to 9,
            "soft" to 9, "softness" to 10, "smooth" to 10, "silky" to 12,
            "rough" to 8, "hard" to 6, "solid" to 7,
            "texture" to 14, "pressure" to 10, "weight" to 8,
            "pain" to 8, "hurt" to 7, "ache" to 7,
            "tickle" to 8, "breeze" to 10, "wind" to 8,
            "wet" to 8, "dry" to 6, "damp" to 8,
            "cuddle" to 10, "hug" to 8, "embrace" to 10,
            "vibration" to 10, "shake" to 7, "shaking" to 8,
            "surface" to 6, "ground" to 6, "floor" to 6
        )

        val olfactoryGustatoryWords = listOf(
            "smell" to 14, "smelled" to 16, "smelling" to 14,
            "scent" to 14, "fragrance" to 16, "aroma" to 16,
            "perfume" to 12, "stink" to 10, "stench" to 12,
            "taste" to 14, "tasted" to 16, "tasting" to 14,
            "flavor" to 14, "sweet" to 10, "sour" to 10, "bitter" to 10,
            "salty" to 10, "spicy" to 10, "delicious" to 12,
            "eat" to 7, "ate" to 8, "eating" to 8,
            "drink" to 7, "drank" to 8, "drinking" to 8,
            "food" to 6, "meal" to 6, "fruit" to 7
        )

        val emotionalWords = listOf(
            "fear" to 8, "scared" to 8, "terrified" to 10, "anxious" to 8,
            "happy" to 8, "joy" to 8, "excited" to 8, "thrilled" to 10,
            "sad" to 8, "sorrow" to 10, "grief" to 8,
            "anger" to 8, "angry" to 8, "rage" to 10,
            "surprise" to 8, "shocked" to 8, "amazed" to 10,
            "confused" to 6, "confusion" to 7,
            "peaceful" to 8, "calm" to 6, "serene" to 10,
            "love" to 8, "loved" to 8, "affection" to 8,
            "disgust" to 8, "disgusted" to 8
        )

        // Combine all vividness indicators with category tracking
        data class VividMatch(val points: Double, val category: String)
        val vividMatches = mutableListOf<VividMatch>()

        // Visual
        for ((word, points) in visualWords) {
            val regex = "(?<![a-z])$word(?![a-z])".toRegex()
            val count = regex.findAll(text).count()
            repeat(count) { vividMatches.add(VividMatch(points * 0.9, "visual")) }
        }
        // Auditory
        for ((word, points) in auditoryWords) {
            val regex = "(?<![a-z])$word(?![a-z])".toRegex()
            val count = regex.findAll(text).count()
            repeat(count) { vividMatches.add(VividMatch(points.toDouble(), "auditory")) }
        }
        // Tactile
        for ((word, points) in tactileWords) {
            val regex = "(?<![a-z])$word(?![a-z])".toRegex()
            val count = regex.findAll(text).count()
            repeat(count) { vividMatches.add(VividMatch(points.toDouble(), "tactile")) }
        }
        // Olfactory/Gustatory
        for ((word, points) in olfactoryGustatoryWords) {
            val regex = "(?<![a-z])$word(?![a-z])".toRegex()
            val count = regex.findAll(text).count()
            repeat(count) { vividMatches.add(VividMatch(points * 1.2, "olfactory_gustatory")) }
        }
        // Emotional
        for ((word, points) in emotionalWords) {
            val regex = "(?<![a-z])$word(?![a-z])".toRegex()
            val count = regex.findAll(text).count()
            repeat(count) { vividMatches.add(VividMatch(points * 0.7, "emotional")) }
        }

        // Base vividness from raw keyword scores
        var vividnessScore = vividMatches.sumOf { it.points }

        // Sensory diversity bonus: more categories = more vivid
        val categoriesUsed = vividMatches.map { it.category }.distinct().size
        val categoryBonus = (categoriesUsed - 1) * 8.0
        vividnessScore += categoryBonus

        // Sensory density: ratio of sensory words to total words
        val sensoryWordCount = vividMatches.size
        val sensoryDensity = sensoryWordCount.toDouble() / wordCount
        vividnessScore += (sensoryDensity * 100).coerceAtMost(25.0)

        // Vocabulary richness bonus (more unique words = more detailed description)
        val uniqueWords = words.distinct().size
        val vocabRichness = uniqueWords.toDouble() / wordCount
        vividnessScore += (vocabRichness * 60).coerceAtMost(15.0)

        // Length bonus (longer descriptions tend to be more detailed)
        val lengthBonus = (wordCount.toDouble() / 30).coerceAtMost(15.0)
        vividnessScore += lengthBonus

        // Scale by content length (diminishing returns)
        val vividLengthFactor = (wordCount.coerceAtMost(150) / 150.0)
        vividnessScore *= (0.4 + 0.6 * vividLengthFactor)

        val vividness = vividnessScore.coerceIn(0.0, 100.0).toInt()

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
