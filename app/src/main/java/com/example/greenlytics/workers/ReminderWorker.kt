package com.example.greenlytics.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.greenlytics.R
import com.example.greenlytics.data.repository.EmissionRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: EmissionRepo
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("GreenLyticsPrefs", Context.MODE_PRIVATE)
        val isNotifOn = prefs.getBoolean("NOTIF_ON", true)

        // Jika notifikasi dimatikan, beri laporan sukses ke sistem dan langsung batalkan eksekusi
        if (!isNotifOn) {
            return Result.success()
        }

        return try {
            val allEmissions = repository.getAllEmissions()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val hasInputToday = allEmissions.any {
                val emissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.tanggalInput))
                emissionDate == today
            }

            if (!hasInputToday) {
                showReminderNotification()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Error: ${e.message}")
            Result.retry()
        }
    }

    private fun showReminderNotification() {
        val channelId = "greenlytics_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Input Emisi",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Halo GreenLytics! 🍃")
            .setContentText("Ayo catat jejak karbonmu hari ini sebelum lupa!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(2002, builder.build())
    }
}