package com.example.moodscape.data

data class MoodOption(val emoji: String, val name: String, val score: Int)

val defaultMoodOptions = listOf(
    MoodOption("😊", "Happy", 5),
    MoodOption("😄", "Excited", 5),
    MoodOption("😐", "Neutral", 3),
    MoodOption("😠", "Angry", 1),
    MoodOption("😢", "Sad", 1),
    MoodOption("😴", "Tired", 2),
)