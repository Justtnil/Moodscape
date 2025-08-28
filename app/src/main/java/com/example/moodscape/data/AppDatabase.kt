package com.example.moodscape.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MoodEntry::class, MoodCategory::class, MoodEntryFts::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun moodCategoryDao(): MoodCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // In a production app, you would create a robust migration plan.
        // For development, falling back is often easier.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodscape_database"
                )
                    .fallbackToDestructiveMigration() // Easiest for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}