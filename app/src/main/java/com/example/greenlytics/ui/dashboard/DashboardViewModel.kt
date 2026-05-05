package com.example.greenlytics.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenlytics.data.repository.EmissionRepo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: EmissionRepo
) : ViewModel() {

    private val _greetingText = MutableLiveData<String>()
    val greetingText: LiveData<String> = _greetingText

    private val _todayTotal = MutableLiveData(0.0)
    val todayTotal: LiveData<Double> = _todayTotal

    private val _transportTotal = MutableLiveData(0.0)
    val transportTotal: LiveData<Double> = _transportTotal

    private val _electricTotal = MutableLiveData(0.0)
    val electricTotal: LiveData<Double> = _electricTotal

    private val _foodTotal = MutableLiveData(0.0)
    val foodTotal: LiveData<Double> = _foodTotal

    private val _shoppingTotal = MutableLiveData(0.0)
    val shoppingTotal: LiveData<Double> = _shoppingTotal

    private val _wasteTotal = MutableLiveData(0.0)
    val wasteTotal: LiveData<Double> = _wasteTotal

    private val _chartData = MutableLiveData<List<Pair<String, Float>>>()
    val chartData: LiveData<List<Pair<String, Float>>> = _chartData

    // 🔥 LiveData untuk Streak Dinamis
    private val _streakCount = MutableLiveData<Int>()
    val streakCount: LiveData<Int> = _streakCount

    init {
        setupGreeting()
        loadDashboardData()
    }

    private fun setupGreeting() {
        val user = FirebaseAuth.getInstance().currentUser
        val firstName = user?.displayName?.split(" ")?.firstOrNull() ?: "Pengguna"

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 0..11 -> "Selamat Pagi"
            in 12..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
        _greetingText.value = "$timeGreeting,\n$firstName!"
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfToday = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, -6)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOf7DaysAgo = calendar.timeInMillis

            val weeklyEmissions = repository.getEmissionsListBetweenDates(startOf7DaysAgo, endOfToday)

            // --- DATA RINGKASAN HARI INI ---
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayString = dateFormat.format(System.currentTimeMillis())

            val todayData = weeklyEmissions.filter {
                dateFormat.format(java.util.Date(it.tanggalInput)) == todayString
            }

            _todayTotal.value = todayData.sumOf { it.emisiKarbon }
            _transportTotal.value = todayData.filter { it.kategori == "Transportasi" }.sumOf { it.emisiKarbon }
            _electricTotal.value = todayData.filter { it.kategori == "Listrik" }.sumOf { it.emisiKarbon }
            _wasteTotal.value = todayData.filter { it.kategori == "Sampah" }.sumOf { it.emisiKarbon }
            _foodTotal.value = todayData.filter { it.subKategori == "MAKANAN_MINUMAN" }.sumOf { it.emisiKarbon }
            _shoppingTotal.value = todayData.filter { it.kategori == "Belanja" && it.subKategori != "MAKANAN_MINUMAN" }.sumOf { it.emisiKarbon }

            // --- DATA GRAFIK (7 Hari Terakhir) ---
            val displayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
            val chartEntries = mutableListOf<Pair<String, Float>>()

            for (i in 0..6) {
                val loopCal = Calendar.getInstance()
                loopCal.add(Calendar.DAY_OF_YEAR, -(6 - i))

                val dateStrForFilter = dateFormat.format(loopCal.time)
                val dateStrForChart = displayFormat.format(loopCal.time)

                val dailyTotal = weeklyEmissions.filter {
                    dateFormat.format(java.util.Date(it.tanggalInput)) == dateStrForFilter
                }.sumOf { it.emisiKarbon }.toFloat()

                chartEntries.add(Pair(dateStrForChart, dailyTotal))
            }

            _chartData.value = chartEntries

            // 🔥 Panggil fungsi kalkulasi streak
            calculateStreak()
        }
    }

    // Fungsi sinkron dengan arsitektur Profil/Progress untuk menghitung hari berturut-turut
    private suspend fun calculateStreak() {
        val allEmissions = repository.getAllEmissions()
        var currentStreak = 0

        if (allEmissions.isNotEmpty()) {
            val uniqueDays = mutableSetOf<String>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (e in allEmissions) {
                uniqueDays.add(sdf.format(java.util.Date(e.tanggalInput)))
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
        _streakCount.value = currentStreak
    }
}