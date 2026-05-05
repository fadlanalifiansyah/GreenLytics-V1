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
import java.util.Locale

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: EmissionRepo
) : ViewModel() {

    // LiveData untuk Chart (Sekarang mengirim 3 data: Bar, Line, dan Label Hari)
    private val _chartData = MutableLiveData<Triple<List<BarEntry>, List<Entry>, Array<String>>>()
    val chartData: LiveData<Triple<List<BarEntry>, List<Entry>, Array<String>>> = _chartData

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
        val labels = Array(7) { "" }

        // LOGIKA MENCARI HARI SENIN MINGGU INI
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY

        // Set ke Senin jam 00:00
        calendar.add(Calendar.DAY_OF_MONTH, -diffToMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfThisWeek = calendar.timeInMillis

        // Set ke Minggu jam 23:59
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startOfThisWeek
        endCal.add(Calendar.DAY_OF_MONTH, 6)
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        val endOfThisWeek = endCal.timeInMillis

        // Tarik data emisi minggu ini
        val weeklyEmissions = repository.getEmissionsListBetweenDates(startOfThisWeek, endOfThisWeek)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))

        // Loop dari Senin (0) sampai Minggu (6)
        for (i in 0..6) {
            val loopCal = Calendar.getInstance()
            loopCal.timeInMillis = startOfThisWeek
            loopCal.add(Calendar.DAY_OF_MONTH, i)

            val dateStrForFilter = dateFormat.format(loopCal.time)

            // Simpan nama harinya
            labels[i] = displayFormat.format(loopCal.time)

            // Hitung emisi untuk hari tersebut
            val dailyTotal = weeklyEmissions.filter {
                dateFormat.format(java.util.Date(it.tanggalInput)) == dateStrForFilter
            }.sumOf { it.emisiKarbon }.toFloat()

            barEntries.add(BarEntry(i.toFloat(), dailyTotal))
            lineEntries.add(Entry(i.toFloat(), dailyTotal))
        }

        // Kirim ketiga data sekaligus ke UI
        _chartData.value = Triple(barEntries, lineEntries, labels)
    }

    private suspend fun calculateStreak() {
        val allEmissions = repository.getAllEmissions()

        if (allEmissions.isEmpty()) {
            _streakCount.value = 0
            return
        }

        val uniqueDays = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (emission in allEmissions) {
            val dateStr = sdf.format(java.util.Date(emission.tanggalInput))
            uniqueDays.add(dateStr)
        }

        val sortedDays = uniqueDays.toList().sortedDescending()
        var currentStreak = 0
        val calendar = Calendar.getInstance()

        val todayStr = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)

        if (sortedDays.first() != todayStr && sortedDays.first() != yesterdayStr) {
            _streakCount.value = 0
            return
        }

        val checkCalendar = Calendar.getInstance()
        if (sortedDays.first() == yesterdayStr) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (dayStr in sortedDays) {
            val targetStr = sdf.format(checkCalendar.time)
            if (dayStr == targetStr) {
                currentStreak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }

        _streakCount.value = currentStreak
    }
}