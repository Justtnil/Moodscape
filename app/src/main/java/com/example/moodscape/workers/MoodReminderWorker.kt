package com.example.moodscape.workers


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
        val dao = AppDatabase.getDatabase(context).moodEntryDao()
        val today = Calendar.getInstance()
        val startOfDay = today.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val hasTodaysEntry = dao.hasEntryForDay(startOfDay)

        if (!hasTodaysEntry) {
            showNotification("How are you feeling?", "Don't forget to log your mood today!")
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
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure you have this drawable
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}