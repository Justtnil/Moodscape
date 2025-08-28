package com.example.moodscape.data

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey
    val timestamp: Long,
    val emoji: String,
    val note: String,
    val categoryId: Long?,
    val moodScore: Int
)

@Entity(tableName = "mood_categories")
data class MoodCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String
)

@Fts4(contentEntity = MoodEntry::class)
@Entity(tableName = "mood_entries_fts")
data class MoodEntryFts(
    val note: String
)

data class MoodEntryWithCategory(
    val timestamp: Long,
    val emoji: String,
    val note: String,
    val categoryId: Long?,
    val moodScore: Int,
    val categoryName: String?,
    val categoryColorHex: String?
)