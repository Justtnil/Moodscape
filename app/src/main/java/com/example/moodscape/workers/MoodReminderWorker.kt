package com.example.moodscape.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moodscape.R
import com.example.moodscape.data.AppDatabase
import java.util.Calendar

class MoodReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Check if notifications are allowed
        val sharedPreferences = context.getSharedPreferences("moodscape_prefs", Context.MODE_PRIVATE)
        val permissionGranted = sharedPreferences.getBoolean("notification_permission_granted", false)
        val permissionAsked = sharedPreferences.getBoolean("notification_permission_asked", false)

        // For older Android versions, we can always show notifications
        val canShowNotification = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            // For Android 13+, only show if permission was granted
            permissionGranted
        }

        // Only proceed if we can show notifications
        if (canShowNotification) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            // Determine which reminder this is based on time of day
            val (title, message) = when (currentHour) {
                in 8..14 -> { // Morning reminder (8 AM - 2 PM)
                    "Good Morning! 🌞" to "How are you feeling today? Take a moment to log your morning mood!"
                }
                in 15..23 -> { // Evening reminder (3 PM - 11:59 PM)
                    "Good Evening! 🌙" to "How was your day? Don't forget to log your evening mood!"
                }
                else -> { // Night reminder (12 AM - 7 AM) - less common but possible
                    "How are you feeling?" to "Don't forget to log your mood!"
                }
            }

            val dao = AppDatabase.getDatabase(context).moodEntryDao()
            val today = Calendar.getInstance()
            val startOfDay = today.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val hasTodaysEntry = dao.hasEntryForDay(startOfDay)

            // Show reminder if no mood entry for today
            if (!hasTodaysEntry) {
                showNotification(title, message)
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mood_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mood Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification) // Unique ID each time
    }
}