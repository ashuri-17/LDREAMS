package com.ldreams.app.data.models

data class LucidProgramDay(
    val day: Int,
    val title: String,
    val description: String,
    val morningTask: String,
    val daytimeTask: String,
    val eveningTask: String,
    val bedtimeTask: String,
    val technique: String,
    val affirmation: String,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
) {
    val tasks: List<String>
        get() = listOf(morningTask, daytimeTask, eveningTask, bedtimeTask)

    fun taskLabel(index: Int): String = when (index) {
        0 -> "Morning"
        1 -> "Daytime"
        2 -> "Evening"
        3 -> "Bedtime"
        else -> ""
    }
}

object LucidProgramRepository {
    private val days = listOf(
        LucidProgramDay(
            day = 1,
            title = "Dream Awareness",
            description = "Welcome to your lucid dreaming journey! This is where it all begins. " +
                    "Today you will establish the foundational practice of dream awareness. " +
                    "By simply paying attention to your dreams and setting clear intentions, " +
                    "you are already taking the first step toward lucidity. " +
                    "Remember, every expert lucid dreamer started exactly where you are right now. " +
                    "Trust the process and be patient with yourself.",
            morningTask = "Write down any dreams you remember from last night in your dream journal. Even fragments and feelings matter.",
            daytimeTask = "Set 3 phone reminders to pause and ask yourself: 'Am I dreaming right now?' Take a deep breath and genuinely check.",
            eveningTask = "Review your dream journal entries from the past week and notice any recurring themes, people, or places.",
            bedtimeTask = "Repeat with conviction 5 times: 'I will remember my dreams tonight.' Feel the intention in every word.",
            technique = "Intention Setting — The practice of programming your mind before sleep. By clearly stating your intention to remember dreams, you activate your brain's reticular activating system (RAS) to prioritize dream recall. This is the foundation of all lucid dreaming techniques.",
            affirmation = "I am fully aware of my dreams and open to the experience of lucidity."
        ),
        LucidProgramDay(
            day = 2,
            title = "Reality Testing",
            description = "Reality checks are the heartbeat of lucid dreaming. By training yourself to " +
                    "question reality throughout the day, you will naturally carry this habit into your dreams. " +
                    "Today you will master the three core reality check techniques. " +
                    "The key is consistency — the more you practice while awake, the more likely you are to " +
                    "try them while dreaming.",
            morningTask = "Perform a nose-pinch reality check: pinch your nose closed and try to breathe through it. If you can breathe, you are dreaming!",
            daytimeTask = "Every time you see a digital clock or phone screen, count your fingers on both hands. In dreams, numbers often shift.",
            eveningTask = "Practice all three reality checks: finger count, clock check (look away and back), and nose pinch. Do them with full focus.",
            bedtimeTask = "Perform all 3 reality checks in sequence before falling asleep. Say aloud: 'I will do reality checks in my dreams.'",
            technique = "Reality Testing — The scientific method for your dreams. By regularly testing whether you are awake, you create a powerful habit that carries into the dream state. The most reliable checks involve testing physical laws: breathing through a pinched nose, reading text twice, or pushing a finger through your palm.",
            affirmation = "I question reality throughout my day, and this awareness carries into my dreams."
        ),
        LucidProgramDay(
            day = 3,
            title = "Dream Signs",
            description = "Everyone has recurring themes, characters, or impossible situations in their dreams. " +
                    "These are your personal dream signs — and recognizing them is like finding a hidden map " +
                    "that leads straight to lucidity. Today you will become a detective of your own dream world, " +
                    "identifying the unique patterns that your mind creates night after night.",
            morningTask = "Review your dream journal and highlight anything unusual, impossible, or recurring with a bright marker.",
            daytimeTask = "Write down 5 things that could never happen in real life but could absolutely happen in a dream.",
            eveningTask = "Create a 'Dream Signs' list in your journal: categorize the most common themes, characters, locations, and feelings from your dreams.",
            bedtimeTask = "Pick one dream sign and visualize it vividly. Tell yourself: 'When I see this, I will know I am dreaming.'",
            technique = "Pattern Recognition — Your dreams are full of signatures that distinguish them from waking life. Common dream signs include: impossible physics (flying, breathing underwater), unusual settings (familiar places that are wrong), odd characters (people who look different or act strangely), and strong emotions (overwhelming fear or joy without cause).",
            affirmation = "I recognize the signs my dreams show me, and I use them to awaken within the dream."
        ),
        LucidProgramDay(
            day = 4,
            title = "MILD Technique",
            description = "The MILD technique (Mnemonic Induction of Lucid Dreams) was developed by Dr. Stephen LaBerge " +
                    "at Stanford University and is one of the most scientifically validated lucid dreaming methods. " +
                    "It leverages the power of prospective memory — remembering to do something in the future. " +
                    "Tonight you will program your mind to recognize when you are dreaming.",
            morningTask = "Read about the MILD technique: it uses future memory to trigger lucidity by repeating a mantra while visualizing becoming lucid.",
            daytimeTask = "During your reality checks, add a moment of visualization: picture yourself realizing you are dreaming and becoming lucid.",
            eveningTask = "Write a short script of exactly what you will do in your next lucid dream. Be specific!",
            bedtimeTask = "As you drift to sleep, repeat the mantra: 'Next time I am dreaming, I will remember I am dreaming.' Visualize yourself becoming lucid.",
            technique = "MILD (Mnemonic Induction of Lucid Dreams) — 1. As you fall asleep, bring your awareness to your intention. 2. Repeat your mantra with genuine belief. 3. Visualize yourself in a recent dream, spotting a dream sign, and becoming lucid. 4. Imagine what you would do as a lucid dreamer. 5. Repeat until the visualization merges into sleep.",
            affirmation = "When I am dreaming, I will recognize that I am dreaming and awaken within the dream."
        ),
        LucidProgramDay(
            day = 5,
            title = "WBTB Method",
            description = "Wake Back to Bed is widely considered the most powerful lucid dreaming technique available. " +
                    "By waking up after 5-6 hours of sleep (when REM sleep is longest), staying awake briefly, " +
                    "then returning to bed with focused intention, you dramatically increase your chances of " +
                    "entering REM sleep with full awareness. This method works best when combined with MILD or WILD.",
            morningTask = "Set an alarm for 5 hours after your planned bedtime. Place it across the room so you must get up to turn it off.",
            daytimeTask = "Plan your WBTB activity: prepare a book about lucid dreaming, or write in your journal about your intention for tonight.",
            eveningTask = "Review the WBTB method steps: 1. Wake after 5 hours, 2. Stay awake 20-45 minutes, 3. Practice MILD, 4. Return to sleep with lucid intention.",
            bedtimeTask = "When your alarm wakes you, get up and read about lucid dreaming for 20-30 minutes. Then return to bed, repeat your MILD mantra, and fall asleep with clear intention.",
            technique = "WBTB (Wake Back to Bed) — 1. Sleep for 5-6 hours. 2. Wake up and stay awake for 20-45 minutes (read, journal, or practice reality checks). 3. Return to bed while the mind is still alert. 4. Combine with MILD or WILD as you fall back asleep. This works because it targets the longest REM period of the night.",
            affirmation = "I will wake with purpose, return with clarity, and dream with lucid awareness."
        ),
        LucidProgramDay(
            day = 6,
            title = "WILD Technique",
            description = "The WILD technique (Wake-Initiated Lucid Dream) is the direct path into lucid dreaming. " +
                    "Instead of becoming lucid within a dream, you maintain your waking awareness as your body " +
                    "falls asleep, entering the dream directly from the hypnagogic state — the surreal " +
                    "transition between waking and dreaming. This takes practice, but it is incredibly rewarding.",
            morningTask = "Upon waking, lie still for 2-3 minutes and notice the transition from sleep to wakefulness. This is the reverse of WILD.",
            daytimeTask = "Practice a 5-minute mindfulness meditation: sit quietly and observe your thoughts without engaging them. This trains the mental stillness needed for WILD.",
            eveningTask = "Prepare your body: get comfortable in bed, loosen any tight clothing, and ensure the room is dark and quiet.",
            bedtimeTask = "Relax your entire body progressively from head to toe. Keep your mind gently alert. Watch for hypnagogic imagery — swirling colors, patterns, or images. Let them carry you into the dream.",
            technique = "WILD (Wake-Initiated Lucid Dream) — 1. Lie still in a comfortable position. 2. Relax your body completely (progressive relaxation). 3. Keep your mind awake while your body falls asleep. 4. Observe hypnagogic imagery without trying to control it. 5. Let the imagery grow until it forms a full dream scene. 6. Step into the dream while maintaining awareness.",
            affirmation = "My body sleeps peacefully, but my mind remains awake, aware, and ready to dream."
        ),
        LucidProgramDay(
            day = 7,
            title = "Lucid Living",
            description = "Congratulations on reaching the final day of your Lucid in 7 Days program! " +
                    "Today is about bringing everything together and creating a sustainable practice that " +
                    "will serve you for a lifetime. You will learn how to maintain lucidity once you achieve it, " +
                    "how to stabilize your dream environment, and how to integrate these techniques into " +
                    "your daily life. This is not an ending — it is a beautiful beginning.",
            morningTask = "Review your entire week of practice. Write down which techniques resonated most and what you want to continue practicing.",
            daytimeTask = "Create your personal 'Lucid Living Plan' — a sustainable routine combining your favorite reality checks, journaling habits, and techniques.",
            eveningTask = "Design your next dream goal: something specific you want to do in your next lucid dream (fly, explore, talk to a dream character, visit a place).",
            bedtimeTask = "Combine everything: perform your reality checks, repeat your favorite mantra, visualize a dream sign, and go to sleep with strong intention.",
            technique = "Lucid Living — A sustainable practice combining all techniques: daily reality checks, consistent dream journaling, regular MILD practice, weekly WBTB sessions, and setting dream goals. To maintain lucidity once in a dream: stabilize by rubbing your hands together, spinning around, or shouting 'Increase clarity now!' Engage your senses to prevent waking.",
            affirmation = "I am a lucid dreamer. My journey continues beyond this program, growing richer with every night."
        )
    )

    fun getAllDays(): List<LucidProgramDay> = days

    fun getDay(dayNumber: Int): LucidProgramDay? = days.find { it.day == dayNumber }
}
