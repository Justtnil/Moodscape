package com.example.moodscape

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import com.example.moodscape.workers.MoodReminderWorker
import java.util.concurrent.TimeUnit

class MoodscapeApplication : Application() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("moodscape_prefs", Context.MODE_PRIVATE)
        scheduleMoodReminder()
    }

    fun scheduleMoodReminder() {
        // Check if we should schedule the worker based on permission status
        val permissionGranted = sharedPreferences.getBoolean("notification_permission_granted", false)
        val permissionAsked = sharedPreferences.getBoolean("notification_permission_asked", false)

        // For older Android versions, we can always schedule
        val shouldSchedule = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            // For Android 13+, only schedule if permission was granted or not yet asked
            permissionGranted || !permissionAsked
        }

        if (shouldSchedule) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Schedule worker to run approximately every 12 hours (twice daily)
            val reminderRequest = PeriodicWorkRequestBuilder<MoodReminderWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "MoodReminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
            )
        }
    }

    // Keep this function even if unused - good for future reference
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