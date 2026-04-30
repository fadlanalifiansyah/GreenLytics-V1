package com.example.greenlytics.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenlytics.data.repository.EmissionRepo
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: EmissionRepo
) : ViewModel() {

    // LiveData untuk Chart
    private val _chartData = MutableLiveData<Pair<List<BarEntry>, List<Entry>>>()
    val chartData: LiveData<Pair<List<BarEntry>, List<Entry>>> = _chartData

    // LiveData untuk Streak
    private val _streakCount = MutableLiveData<Int>()
    val streakCount: LiveData<Int> = _streakCount

    fun loadProgressData() {
        viewModelScope.launch {
            calculateWeeklyChart()
            calculateStreak()
        }
    }

    private suspend fun calculateWeeklyChart() {
        val barEntries = ArrayList<BarEntry>()
        val lineEntries = ArrayList<Entry>()

        val calendar = Calendar.getInstance()
        // Set ke jam 23:59 hari ini
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)

        // Ambil data 7 hari ke belakang
        for (i in 6 downTo 0) {
            val endTime = calendar.timeInMillis

            // Set ke jam 00:00 di hari yang sama
            val tempCal = calendar.clone() as Calendar
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            val startTime = tempCal.timeInMillis

            // Ambil total emisi hari itu dari Room
            val dailyTotal = repository.getEmissionsBetweenDates(startTime, endTime)

            // Masukkan ke list (index 0 adalah hari tertua, index 6 adalah hari ini)
            val xPos = (6 - i).toFloat()
            barEntries.add(BarEntry(xPos, dailyTotal.toFloat()))

            // Tren (Line) bisa kita buat sama dengan Bar atau rata-rata bergerak
            lineEntries.add(Entry(xPos, dailyTotal.toFloat()))

            // Mundur 1 hari untuk iterasi berikutnya
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        _chartData.value = Pair(barEntries, lineEntries)
    }

    private suspend fun calculateStreak() {
        // 1. Ambil semua data emisi dari database
        val allEmissions = repository.getAllEmissions() // Datanya sudah urut dari terbaru ke terlama berkat DAO

        if (allEmissions.isEmpty()) {
            _streakCount.value = 0
            return
        }

        // 2. Ekstrak tanggalnya saja (buang jam, menit, detik) agar aktivitas di hari yang sama tidak dihitung ganda
        val uniqueDays = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (emission in allEmissions) {
            val dateStr = sdf.format(Date(emission.tanggalInput))
            uniqueDays.add(dateStr)
        }

        // Jadikan list yang berurutan (tanggal terbaru di urutan pertama)
        val sortedDays = uniqueDays.toList().sortedDescending()

        // 3. Logika Menghitung Streak (Hari Berturut-turut)
        var currentStreak = 0
        val calendar = Calendar.getInstance()

        val todayStr = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)

        // Jika aktivitas terakhir bukan hari ini ATAU kemarin, berarti streak sudah putus (bolong)
        if (sortedDays.first() != todayStr && sortedDays.first() != yesterdayStr) {
            _streakCount.value = 0
            return
        }

        // Mulai cek mundur dari hari aktivitas terakhir
        val checkCalendar = Calendar.getInstance()
        if (sortedDays.first() == yesterdayStr) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (dayStr in sortedDays) {
            val targetStr = sdf.format(checkCalendar.time)

            if (dayStr == targetStr) {
                currentStreak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // Ada hari yang bolong/terlewat, hentikan penghitungan
                break
            }
        }

        // 4. Update UI
        _streakCount.value = currentStreak
    }
}