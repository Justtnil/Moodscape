package com.example.moodscape.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(moodEntry: MoodEntry)

    @Query("""
        SELECT m.*, c.name as categoryName, c.colorHex as categoryColorHex
        FROM mood_entries m
        LEFT JOIN mood_categories c ON m.categoryId = c.id
        ORDER BY m.timestamp DESC
    """)
    fun getAllEntriesWithCategory(): Flow<List<MoodEntryWithCategory>>

    @Query("SELECT EXISTS(SELECT 1 FROM mood_entries WHERE timestamp >= :startOfDayMillis)")
    suspend fun hasEntryForDay(startOfDayMillis: Long): Boolean

    @Query("""
        SELECT m.* FROM mood_entries m
        JOIN mood_entries_fts fts ON m.rowid = fts.rowid
        WHERE fts.note MATCH :query
    """)
    suspend fun searchNotes(query: String): List<MoodEntry>
}

@Dao
interface MoodCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: MoodCategory)

    @Query("SELECT * FROM mood_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<MoodCategory>>

    @Delete
    suspend fun deleteCategory(category: MoodCategory)
}