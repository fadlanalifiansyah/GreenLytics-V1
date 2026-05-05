package com.example.greenlytics.ui.tips

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.example.greenlytics.data.model.TipsModel
import com.example.greenlytics.data.repository.EmissionRepo
import com.example.greenlytics.utils.TipsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TipsViewModel @Inject constructor(
    private val repository: EmissionRepo,
    @ApplicationContext private val context: Context // Mengambil Context untuk SharedPreferences
) : ViewModel() {

    private val _tipsList = MutableLiveData<List<TipsModel>>()
    val tipsList: LiveData<List<TipsModel>> = _tipsList

    private val _potentialSaving = MutableLiveData<String>()
    val potentialSaving: LiveData<String> = _potentialSaving

    // Membuat buku catatan kecil di memori HP
    private val prefs: SharedPreferences = context.getSharedPreferences("TipsPrefs", Context.MODE_PRIVATE)

    // Fungsi pembantu untuk mendapatkan format tanggal hari ini (contoh: "2026-05-05")
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun loadContent() {
        viewModelScope.launch {
            val allEmissions = repository.getAllEmissions()

            // Filter data HARI INI saja
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis

            val emissionsToday = allEmissions.filter {
                it.tanggalInput in startOfDay..endOfDay
            }

            // 1. Ambil rekomendasi tips baru
            val newTips = TipsProvider.getRelevantTips(emissionsToday)
            val todayStr = getTodayDateString()

            // 2. LOGIKA HARIAN: Cek memori HP apakah tips ini sudah diklik HARI INI
            newTips.forEach { tip ->
                val savedDate = prefs.getString("tip_done_${tip.id}", "")
                // Jika tanggal di memori sama dengan tanggal hari ini, berarti sudah selesai
                tip.isCompleted = (savedDate == todayStr)
            }

            // 3. Kirim ke layar
            _tipsList.postValue(newTips)

            val totalToday = emissionsToday.sumOf { it.emisiKarbon }
            val saving = String.format("%.1f", totalToday * 0.15)
            _potentialSaving.postValue("$saving kg CO₂")
        }
    }

    // Fungsi untuk menyimpan tanggal saat tombol diklik
    fun markTipAsDone(tipId: Int) {
        prefs.edit().putString("tip_done_$tipId", getTodayDateString()).apply()
    }
}