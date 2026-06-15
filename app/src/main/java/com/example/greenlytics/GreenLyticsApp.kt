package com.example.greenlytics

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.greenlytics.workers.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GreenLyticsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupDailyReminder()
    }
    private fun setupDailyReminder() {
        val prefs = getSharedPreferences("GreenLyticsPrefs", android.content.Context.MODE_PRIVATE)
        val isNotifOn = prefs.getBoolean("NOTIF_ON", true)

        if (!isNotifOn) {
            return
        }

        // Menggunakan jadwal berulang (Periodic) setiap 24 jam
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyReminderWork",
            ExistingPeriodicWorkPolicy.KEEP, // Pakai KEEP agar jadwalnya permanen
            dailyWorkRequest
        )
    }
}