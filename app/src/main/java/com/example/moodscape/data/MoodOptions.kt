package com.example.moodscape.data

import androidx.compose.runtime.mutableStateListOf

data class MoodOption(val emoji: String, val name: String, val score: Int)

val defaultMoodOptions = mutableStateListOf(
    MoodOption("😊", "Happy", 5),
    MoodOption("😄", "Excited", 5),
    MoodOption("😐", "Neutral", 3),
    MoodOption("😠", "Angry", 1),
    MoodOption("😢", "Sad", 1),
    MoodOption("😴", "Tired", 2),
)