package com.example.moodscape.data

import java.util.*
import kotlin.math.roundToInt

class MoodAnalysisEngine {

    fun generateInsights(entries: List<MoodEntryWithCategory>): MoodInsights {
        return MoodInsights(
            trendMessage = analyzeTrend(entries),
            dayOfWeekMessage = analyzeDayOfWeekPatterns(entries),
            keywordMessage = analyzeKeywords(entries)
        )
    }

    private fun analyzeTrend(entries: List<MoodEntryWithCategory>): String {
        if (entries.size < 7) {
            return "Not enough data yet. Log more moods to see trends!"
        }

        // Get last 7 days and previous 7 days for comparison
        val sortedEntries = entries.sortedByDescending { it.timestamp }
        val last7Days = sortedEntries.take(7)
        val previous7Days = sortedEntries.drop(7).take(7)

        if (previous7Days.isEmpty()) {
            return "Keep logging to see your mood trends over time!"
        }

        val lastWeekAverage = last7Days.map { it.moodScore }.average()
        val previousWeekAverage = previous7Days.map { it.moodScore }.average()

        return when {
            lastWeekAverage > previousWeekAverage + 0.5 ->
                "Your mood has improved this week! 🎉"
            lastWeekAverage < previousWeekAverage - 0.5 ->
                "Your mood has declined slightly this week. Take care! 💙"
            else ->
                "Your mood has been relatively stable this week."
        }
    }

    private fun analyzeDayOfWeekPatterns(entries: List<MoodEntryWithCategory>): String {
        if (entries.size < 14) {
            return "Log more moods to discover your weekly patterns!"
        }

        val calendar = Calendar.getInstance()
        val moodByDay = mutableMapOf<Int, MutableList<Int>>()

        // Group moods by day of week (1=Sunday, 2=Monday, ..., 7=Saturday)
        entries.forEach { entry ->
            calendar.timeInMillis = entry.timestamp
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            moodByDay.getOrPut(dayOfWeek) { mutableListOf() }.add(entry.moodScore)
        }

        // Calculate average mood for each day
        val dayAverages = moodByDay.mapValues { (_, scores) ->
            scores.average()
        }

        if (dayAverages.isEmpty()) {
            return "Not enough data to analyze day patterns yet."
        }

        val happiestDay = dayAverages.maxByOrNull { it.value }?.key
        val saddestDay = dayAverages.minByOrNull { it.value }?.key

        val dayNames = arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        return when {
            happiestDay != null && saddestDay != null && happiestDay != saddestDay ->
                "You're happiest on ${dayNames[happiestDay]} and tend to feel lower on ${dayNames[saddestDay]}."
            happiestDay != null ->
                "You're consistently happiest on ${dayNames[happiestDay]}."
            else ->
                "Your mood varies throughout the week without a clear pattern."
        }
    }

    private fun analyzeKeywords(entries: List<MoodEntryWithCategory>): String {
        if (entries.isEmpty() || entries.all { it.note.isBlank() }) {
            return "Add notes to your mood entries to discover keyword insights!"
        }

        // Filter entries with notes
        val entriesWithNotes = entries.filter { it.note.isNotBlank() }

        // Simple keyword analysis
        val positiveWords = setOf("happy", "good", "great", "love", "joy", "excited", "wonderful", "amazing", "fantastic")
        val negativeWords = setOf("sad", "bad", "terrible", "hate", "angry", "frustrated", "worried", "stressed", "anxious")
        val workWords = setOf("work", "job", "office", "boss", "colleague", "meeting", "deadline", "project")
        val familyWords = setOf("family", "mom", "dad", "parent", "child", "kid", "spouse", "partner", "friend")

        val allNotes = entriesWithNotes.joinToString(" ") { it.note.lowercase() }
        val words = allNotes.split(Regex("\\W+")).filter { it.isNotBlank() }

        val positiveCount = words.count { it in positiveWords }
        val negativeCount = words.count { it in negativeWords }
        val workCount = words.count { it in workWords }
        val familyCount = words.count { it in familyWords }

        return when {
            positiveCount > negativeCount && positiveCount > 0 ->
                "You use more positive language in your notes. Keep focusing on what makes you happy! 😊"
            negativeCount > positiveCount && negativeCount > 0 ->
                "You've mentioned challenges in your notes. Remember, it's okay to have difficult days. 💪"
            workCount > 0 && familyCount > 0 ->
                "Your notes mention both work (${workCount} times) and family (${familyCount} times). Finding balance is key!"
            workCount > 0 ->
                "Work appears frequently in your notes (${workCount} times). Consider stress management techniques."
            familyCount > 0 ->
                "Family is important to you, mentioned ${familyCount} times in your notes."
            else ->
                "Your notes contain diverse topics. Keep journaling to discover more patterns!"
        }
    }
}

data class MoodInsights(
    val trendMessage: String,
    val dayOfWeekMessage: String,
    val keywordMessage: String
)