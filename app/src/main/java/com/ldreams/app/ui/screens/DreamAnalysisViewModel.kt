package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.models.DreamAnalysis
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.data.repository.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DreamAnalysisViewModel @Inject constructor(
    private val dreamRepository: DreamRepository
) : ViewModel() {

    private val _analysis = MutableStateFlow<DreamAnalysis?>(null)
    val analysis: StateFlow<DreamAnalysis?> = _analysis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAnalysis()
    }

    fun loadAnalysis() {
        viewModelScope.launch {
            _isLoading.value = true
            _analysis.value = null

            // Simulate AI processing delay (3-5 seconds)
            val delayMillis = 3000L + Random.nextLong(2001L)
            delay(delayMillis)

            val dreams = dreamRepository.getAllDreamsList()
            if (dreams.size < 3) {
                _isLoading.value = false
                return@launch
            }

            val enriched = generateEnrichedAnalysis(dreams)
            _analysis.value = enriched
            _isLoading.value = false
        }
    }

    private suspend fun generateEnrichedAnalysis(dreams: List<DreamEntry>): DreamAnalysis {
        // --- Recurring Themes ---
        val allTags = dreams.flatMap { it.tags }
        val themeCounts = allTags.groupingBy { it }.eachCount()
        val recurringThemes = themeCounts.filter { it.value >= 2 }
            .entries
            .sortedByDescending { it.value }
            .map { it.key }

        // --- Common Emotions ---
        val emotionCounts = dreams.groupBy { it.mood }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (mood, count) ->
                Pair(mood.replaceFirstChar { it.uppercase() }, count)
            }

        // --- Dream Signs ---
        val signLibrary = listOf(
            "flying" to "Flying",
            "falling" to "Falling",
            "chase" to "Chased",
            "chasing" to "Chased",
            "teeth" to "Teeth falling out",
            "water" to "Water",
            "naked" to "Being naked",
            "monster" to "Monsters",
            "snake" to "Snakes",
            "spider" to "Spiders",
            "ocean" to "Ocean",
            "storm" to "Storms"
        )
        val dreamSigns = signLibrary
            .filter { (keyword, _) ->
                dreams.any { dream ->
                    dream.content.lowercase().contains(keyword) ||
                        dream.tags.any { tag ->
                            tag.lowercase().contains(keyword) ||
                                keyword.contains(tag.lowercase())
                        }
                }
            }
            .map { it.second }
            .distinct()

        // --- Weekly / Monthly Stats ---
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val thisWeek = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val thisMonth = calendar.time

        val weeklyCount = dreams.count { it.timestamp.after(thisWeek) }
        val monthlyCount = dreams.count { it.timestamp.after(thisMonth) }
        val avgVividness = if (dreams.isNotEmpty()) {
            dreams.map { it.vividnessLevel }.average().toFloat()
        } else 0f

        // --- Lucidity Trend ---
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val lucidityTrend = dreams
            .sortedBy { it.timestamp }
            .map { Pair(dateFormat.format(it.timestamp), it.lucidityLevel) }

        // --- PREMIUM ANALYSIS: Symbols ---
        val symbols = extractSymbols(dreams)

        // --- PREMIUM ANALYSIS: Emotion Intensities ---
        val emotionIntensities = calculateEmotionIntensities(dreams)

        // --- PREMIUM ANALYSIS: Clarity Score ---
        val clarityScore = calculateClarityScore(dreams)

        // --- PREMIUM ANALYSIS: Personalized Tips ---
        val tips = generateTips(dreams, emotionIntensities, recurringThemes, dreamSigns)

        return DreamAnalysis(
            recurringThemes = recurringThemes,
            commonEmotions = emotionCounts,
            dreamSigns = dreamSigns,
            lucidityTrend = lucidityTrend,
            averageVividness = avgVividness,
            totalDreamsAnalyzed = dreams.size,
            weeklyDreamCount = weeklyCount,
            monthlyDreamCount = monthlyCount,
            symbols = symbols,
            tips = tips,
            clarityScore = clarityScore,
            emotionIntensities = emotionIntensities
        )
    }

    /**
     * Extract dream symbols and their meanings from dream content.
     */
    private fun extractSymbols(dreams: List<DreamEntry>): List<Pair<String, String>> {
        val symbolLibrary = mapOf(
            "flying" to "Freedom and ambition -- you are rising above challenges",
            "falling" to "Loss of control or fear of failure in a situation",
            "water" to "Emotions and the subconscious mind at play",
            "chase" to "Anxiety or avoidance of a pressing problem",
            "teeth" to "Insecurity or feeling powerless in a situation",
            "naked" to "Vulnerability or fear of being exposed",
            "monster" to "Inner fears or unresolved conflicts surfacing",
            "house" to "The self -- different rooms represent different aspects of your psyche",
            "snake" to "Transformation, healing, or hidden fears",
            "spider" to "Feeling trapped or entangled in a situation",
            "ocean" to "Vast emotions and the depth of the unknown",
            "storm" to "Emotional turmoil or upheaval in your life",
            "baby" to "New beginnings, innocence, or untapped potential",
            "road" to "Life path, direction, and the journey ahead",
            "death" to "Transformation, closure, or a major life transition",
            "bridge" to "Transition or connection between two states of being",
            "fire" to "Passion, anger, or a purifying force",
            "forest" to "Exploration of the unknown self or hidden thoughts",
            "door" to "New opportunities, choices, or paths opening up",
            "mirror" to "Self-reflection, identity, or confronting your true self",
            "train" to "Life's journey, momentum, or feeling on the right track",
            "school" to "Learning, growth, or feeling tested by life",
            "phone" to "Desire for connection or difficulty communicating",
            "money" to "Self-worth, security, or anxiety about resources",
            "animal" to "Instincts, primal urges, or untamed aspects of self"
        )

        val allContent = dreams.joinToString(" ") { it.content.lowercase() }
        val tagContent = dreams.flatMap { it.tags }
            .joinToString(" ") { it.lowercase() }
        val combinedContent = "$allContent $tagContent"

        val found = mutableMapOf<String, Int>()
        for ((symbol, _) in symbolLibrary) {
            if (combinedContent.contains(symbol)) {
                found[symbol] = (found[symbol] ?: 0) + 1
            }
        }

        return found.entries
            .sortedByDescending { it.value }
            .take(7)
            .map { (symbol, _) ->
                Pair(
                    symbol.replaceFirstChar { it.uppercase() },
                    symbolLibrary[symbol] ?: "A meaningful symbol in your dream"
                )
            }
    }

    /**
     * Calculate emotion intensities (0-1) based on mood distribution across dreams.
     */
    private fun calculateEmotionIntensities(dreams: List<DreamEntry>): Map<String, Float> {
        val supportedEmotions = listOf(
            "happy", "sad", "anxious", "peaceful", "excited", "fearful"
        )

        if (dreams.isEmpty()) {
            return supportedEmotions.associateWith { 0f }
        }

        val totalDreams = dreams.size.toFloat()

        return supportedEmotions.associate { emotion ->
            val count = dreams.count {
                it.mood.lowercase() == emotion
            }
            val intensity = if (totalDreams > 0) {
                (count / totalDreams).coerceIn(0f, 1f)
            } else 0f

            // Boost very low intensities to create a more interesting wheel
            val adjusted = if (intensity > 0f && intensity < 0.05f) {
                0.05f
            } else intensity

            emotion to adjusted
        }
    }

    /**
     * Calculate a dream clarity score from 0-100 based on multiple factors.
     */
    private fun calculateClarityScore(dreams: List<DreamEntry>): Int {
        if (dreams.isEmpty()) return 0

        val avgLucidity = dreams.map { it.lucidityLevel }.average()
        val avgVividness = dreams.map { it.vividnessLevel }.average()
        val hasLucid = if (dreams.any { it.isLucid }) 1.0 else 0.0
        val moodVariety = dreams.map { it.mood }.distinct().size.toDouble()
        val dreamCount = dreams.size.toDouble()

        val score = (
            avgLucidity * 0.30 +
                avgVividness * 0.20 +
                hasLucid * 25.0 * 0.15 +
                (moodVariety / 6.0) * 100.0 * 0.15 +
                (dreamCount / 50.0).coerceAtMost(1.0) * 100.0 * 0.10 +
                // Bonus for emotional awareness (having emotions/tags)
                (if (dreams.any { it.emotions.isNotEmpty() }) 5.0 else 0.0) +
                (if (dreams.any { it.tags.isNotEmpty() }) 5.0 else 0.0)
            ).toInt().coerceIn(0, 100)

        return score
    }

    /**
     * Generate personalized lucidity tips based on dream patterns.
     */
    private fun generateTips(
        dreams: List<DreamEntry>,
        emotionIntensities: Map<String, Float>,
        themes: List<String>,
        signs: List<String>
    ): List<String> {
        val tips = mutableListOf<String>()
        val avgLucidity = dreams.map { it.lucidityLevel }.average()
        val avgVividness = dreams.map { it.vividnessLevel }.average()
        val hasLucid = dreams.any { it.isLucid }
        val nightmareCount = dreams.count { it.isNightmare }
        val allContent = dreams.joinToString(" ") { it.content.lowercase() }

        // Lucidity tips
        if (avgLucidity < 30) {
            tips.add("Practice reality checks throughout the day -- ask yourself 'Am I dreaming?' and look for inconsistencies.")
            tips.add("Keep a dream journal by your bedside and write immediately upon waking to improve dream recall.")
            tips.add("Try the MILD technique: before sleep, repeat 'I will remember I am dreaming' and visualize becoming lucid.")
        } else if (avgLucidity < 60) {
            tips.add("You have good dream awareness! Try adding WBTB (Wake Back To Bed) to boost lucidity further.")
            tips.add("Use your identified dream signs as triggers for reality checks during the day.")
        } else {
            tips.add("Excellent lucidity! Focus on dream control techniques -- stabilize your dreams by rubbing your hands together.")
        }

        if (!hasLucid) {
            tips.add("Aim for your first lucid dream by setting a strong intention before sleep each night.")
        }

        // Vividness tips
        if (avgVividness < 40) {
            tips.add("Improve dream vividness by engaging with your dreams throughout the day -- re-read entries before sleep.")
        }

        // Nightmare tips
        if (nightmareCount >= 2) {
            tips.add("Practice imagery rehearsal therapy: rewrite the ending of recurring nightmares while awake.")
        }

        // Theme-based tips
        if (themes.any { it.contains("flying", ignoreCase = true) }) {
            tips.add("Flying dreams suggest freedom. Try to recognize this theme and use it as a lucidity trigger.")
        }
        if (themes.any { it.contains("water", ignoreCase = true) } ||
            allContent.contains("water") || allContent.contains("ocean")
        ) {
            tips.add("Water represents emotions. Pay attention to your emotional state during the day to spot dream patterns.")
        }
        if (themes.any { it.contains("chase", ignoreCase = true) } ||
            allContent.contains("chase") || allContent.contains("running")
        ) {
            tips.add("Chase dreams often reflect anxiety. Try confronting the pursuer in your dream to transform the experience.")
        }

        // Emotion-based tips
        val anxious = emotionIntensities["anxious"] ?: 0f
        val fearful = emotionIntensities["fearful"] ?: 0f
        if (anxious > 0.3f || fearful > 0.3f) {
            tips.add("Anxiety and fear appear in your dreams. Practice mindfulness meditation before bed to calm your mind.")
        }

        // Sign-based tips
        if (signs.isNotEmpty()) {
            val signList = signs.take(3).joinToString(", ")
            tips.add("Your recurring dream signs ($signList) are powerful lucidity triggers. Watch for them!")
        }

        // General tips
        tips.add("Consistency is key -- aim to write in your dream journal every morning without fail.")

        return tips.distinct().take(7)
    }
}
