package com.example.greenlytics.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.greenlytics.R
import com.example.greenlytics.databinding.FragmentProgressBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private val viewModel: ProgressViewModel by viewModels()

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Konfigurasi awal grafik saat data masih dimuat
        binding.combinedChart.setNoDataText("Sedang memuat grafik...")

        // Panggil data dari ViewModel
        viewModel.loadProgressData()

        // Observasi Data Grafik
        viewModel.chartData.observe(viewLifecycleOwner) { (barEntries, lineEntries) ->
            val labels = getLast7DaysLabels()
            setupMyChart(barEntries, lineEntries, labels)
        }

        // Observasi Data Streak
        viewModel.streakCount.observe(viewLifecycleOwner) { streak ->
            binding.tvStreakTitle.text = "$streak Hari"

            // Hitung persentase progress bar (Target 14 hari)
            val progressPercentage = (streak.toFloat() / 14f * 100).toInt()
            binding.pbStreak.progress = progressPercentage.coerceAtMost(100)
            binding.tvStreakTarget.text = "$streak/14 hari"

            // Panggil fungsi update warna badge di sini
            updateBadgesUI(streak)
        }
    }

    // Fungsi logika warna badge (Menyala jika streak tercapai, Abu-abu jika belum)
    private fun updateBadgesUI(streak: Int) {
        // Warna abu-abu (Terkunci)
        val lockedBg = Color.parseColor("#EEEEEE")
        val lockedIcon = Color.parseColor("#9E9E9E")

        // 1. Badge 3 Hari (Pemula Hijau) - Biru
        if (streak >= 3) {
            binding.bgBadge1.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
            binding.ivBadge1.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))
        } else {
            binding.bgBadge1.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge1.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }

        // 2. Badge 7 Hari (Pejuang Iklim) - Kuning/Warning
        if (streak >= 7) {
            // Mengambil warna kuning dari res/color agar konsisten
            val warningColor = resources.getColor(R.color.warning, null)
            binding.bgBadge2.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            binding.ivBadge2.imageTintList = android.content.res.ColorStateList.valueOf(warningColor)
        } else {
            binding.bgBadge2.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge2.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }

        // 3. Badge 14 Hari (Eco Champion) - Tosca
        if (streak >= 14) {
            binding.bgBadge3.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E0F2F1"))
            binding.ivBadge3.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#00796B"))
        } else {
            binding.bgBadge3.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge3.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }

        // 4. Badge 30 Hari (Guardian Bumi) - Ungu
        if (streak >= 30) {
            binding.bgBadge4.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F3E5F5"))
            binding.ivBadge4.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))
        } else {
            binding.bgBadge4.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge4.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }
    }

    // Fungsi untuk mendapatkan 7 hari terakhir secara dinamis
    private fun getLast7DaysLabels(): Array<String> {
        val labels = Array(7) { "" }
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEE", Locale("id", "ID")) // Format hari: Sen, Sel, Rab...

        for (i in 6 downTo 0) {
            labels[i] = sdf.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -1) // Mundur 1 hari
        }
        return labels
    }

    // Fungsi untuk menggambar grafik dengan data asli
    private fun setupMyChart(barEntries: List<BarEntry>, lineEntries: List<Entry>, days: Array<String>) {
        val chart = binding.combinedChart

        // 1. Setup Bar (Batang Hijau)
        val barDataSet = BarDataSet(barEntries, "Emisi Karbon")
        barDataSet.color = Color.parseColor("#81C784")
        barDataSet.setDrawValues(false)

        // 2. Setup Line (Garis Biru)
        val lineDataSet = LineDataSet(lineEntries, "Tren")
        lineDataSet.color = Color.parseColor("#64B5F6")
        lineDataSet.setCircleColor(Color.parseColor("#64B5F6"))
        lineDataSet.lineWidth = 3f
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Agar garisnya melengkung mulus
        lineDataSet.setDrawValues(false)

        // 3. Gabungkan Data
        val data = CombinedData()
        data.setData(BarData(barDataSet))
        data.setData(LineData(lineDataSet))
        chart.data = data

        // 4. Konfigurasi Sumbu X (Hari)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(days)
            setDrawGridLines(false)
            granularity = 1f
            // Agar batang pertama dan terakhir tidak terpotong
            axisMinimum = -0.5f
            axisMaximum = barEntries.size - 0.5f
        }

        // 5. Konfigurasi Tampilan Umum
        chart.axisRight.isEnabled = false
        chart.axisLeft.axisMinimum = 0f // Pastikan grafik selalu mulai dari 0 di bawah
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        // 6. Segarkan Grafik
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}