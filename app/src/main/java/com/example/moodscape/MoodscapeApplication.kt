package com.example.moodscape

import android.app.Application
import androidx.work.*
import com.example.moodscape.workers.MoodReminderWorker
import java.util.concurrent.TimeUnit

class MoodscapeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleMoodReminder()
    }

    private fun scheduleMoodReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Use a PeriodicWorkRequest to schedule the reminder daily
        val reminderRequest = PeriodicWorkRequestBuilder<MoodReminderWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            // Set the initial delay to roughly 8 AM tomorrow
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MoodReminder",
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
            reminderRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(java.util.Calendar.HOUR_OF_DAY, 8) // 8 AM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_MONTH, 1) // If 8 AM is already past, schedule for tomorrow
            }
        }
        return calendar.timeInMillis - currentTime
    }
}