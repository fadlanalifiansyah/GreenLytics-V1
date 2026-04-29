package com.example.greenlytics.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenlytics.data.repository.AuthRepository
import com.example.greenlytics.data.repository.EmissionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val repository: EmissionRepo
) : ViewModel() {

    // LiveData untuk Auth (Firebase)
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    // LiveData untuk Statistik Emisi
    private val _monthlyCarbon = MutableLiveData<Double>()
    val monthlyCarbon: LiveData<Double> = _monthlyCarbon

    private val _lowestEmission = MutableLiveData<Double>()
    val lowestEmission: LiveData<Double> = _lowestEmission

    private val _totalActivities = MutableLiveData<Int>()
    val totalActivities: LiveData<Int> = _totalActivities

    // LiveData untuk Streak & Badge
    private val _streakCount = MutableLiveData<Int>()
    val streakCount: LiveData<Int> = _streakCount

    private val _badgeCount = MutableLiveData<Int>()
    val badgeCount: LiveData<Int> = _badgeCount

    fun loadProfilData() {
        // 1. Load Data Firebase Auth
        val user = AuthRepository.getInstance().getCurrentUser()
        if (user != null) {
            val name = user.displayName
            _userName.value = if (!name.isNullOrBlank()) name else "Pengguna GreenLytics"
            _userEmail.value = user.email ?: "email@greenlytics.com"
        } else {
            _userName.value = "Tamu GreenLytics"
            _userEmail.value = "Belum Login"
        }

        // 2. Load Data Emisi dari Room
        viewModelScope.launch {
            _totalActivities.value = repository.getTotalActivitiesCount()
            _lowestEmission.value = repository.getLowestEmission()

            // Menghitung waktu awal bulan ini sampai detik ini
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            // Ambil emisi hanya untuk bulan ini
            _monthlyCarbon.value = repository.getEmissionsBetweenDates(startTime, endTime)

            // Hitung Streak & Badge
            calculateStreakAndBadges()
        }
    }

    private suspend fun calculateStreakAndBadges() {
        val allEmissions = repository.getAllEmissions()
        var currentStreak = 0
        var unlockedBadges = 0

        if (allEmissions.isNotEmpty()) {
            val uniqueDays = mutableSetOf<String>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (e in allEmissions) {
                uniqueDays.add(sdf.format(Date(e.tanggalInput)))
            }

            val sortedDays = uniqueDays.toList().sortedDescending()
            val cal = Calendar.getInstance()
            val todayStr = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)

            if (sortedDays.first() == todayStr || sortedDays.first() == yesterdayStr) {
                val checkCal = Calendar.getInstance()
                if (sortedDays.first() == yesterdayStr) {
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                }

                for (dayStr in sortedDays) {
                    if (dayStr == sdf.format(checkCal.time)) {
                        currentStreak++
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    } else break
                }
            }
        }

        // Tentukan jumlah badge berdasarkan streak
        if (currentStreak >= 30) unlockedBadges = 4
        else if (currentStreak >= 14) unlockedBadges = 3
        else if (currentStreak >= 7) unlockedBadges = 2
        else if (currentStreak >= 3) unlockedBadges = 1

        _streakCount.value = currentStreak
        _badgeCount.value = unlockedBadges
    }
}