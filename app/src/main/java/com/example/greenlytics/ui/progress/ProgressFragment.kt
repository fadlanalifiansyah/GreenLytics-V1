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

        // Ubah teks filter agar sama dengan Dashboard
        binding.tvChartFilter.text = "Minggu Ini"

        binding.combinedChart.setNoDataText("Sedang memuat grafik...")
        viewModel.loadProgressData()

        // Observasi Data Grafik yang sudah menerima format Triple
        viewModel.chartData.observe(viewLifecycleOwner) { (barEntries, lineEntries, labels) ->
            setupMyChart(barEntries, lineEntries, labels)
        }

        viewModel.streakCount.observe(viewLifecycleOwner) { streak ->
            binding.tvStreakTitle.text = "$streak Hari"

            val progressPercentage = (streak.toFloat() / 14f * 100).toInt()
            binding.pbStreak.progress = progressPercentage.coerceAtMost(100)
            binding.tvStreakTarget.text = "$streak/14 hari"

            updateBadgesUI(streak)
        }
    }

    private fun updateBadgesUI(streak: Int) {
        val lockedBg = Color.parseColor("#EEEEEE")
        val lockedIcon = Color.parseColor("#9E9E9E")

        if (streak >= 3) {
            binding.bgBadge1.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
            binding.ivBadge1.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))
        } else {
            binding.bgBadge1.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge1.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }

        if (streak >= 7) {
            val warningColor = resources.getColor(R.color.warning, null)
            binding.bgBadge2.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            binding.ivBadge2.imageTintList = android.content.res.ColorStateList.valueOf(warningColor)
        } else {
            binding.bgBadge2.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge2.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }

        if (streak >= 14) {
            binding.bgBadge3.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E0F2F1"))
            binding.ivBadge3.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#00796B"))
        } else {
            binding.bgBadge3.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge3.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }

        if (streak >= 30) {
            binding.bgBadge4.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F3E5F5"))
            binding.ivBadge4.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))
        } else {
            binding.bgBadge4.backgroundTintList = android.content.res.ColorStateList.valueOf(lockedBg)
            binding.ivBadge4.imageTintList = android.content.res.ColorStateList.valueOf(lockedIcon)
        }
    }

    private fun setupMyChart(barEntries: List<BarEntry>, lineEntries: List<Entry>, days: Array<String>) {
        val chart = binding.combinedChart

        val barDataSet = BarDataSet(barEntries, "Emisi Karbon")
        barDataSet.color = Color.parseColor("#81C784")
        barDataSet.setDrawValues(false)

        val lineDataSet = LineDataSet(lineEntries, "Tren")
        lineDataSet.color = Color.parseColor("#64B5F6")
        lineDataSet.setCircleColor(Color.parseColor("#64B5F6"))
        lineDataSet.lineWidth = 3f
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawValues(false)

        val data = CombinedData()
        data.setData(BarData(barDataSet))
        data.setData(LineData(lineDataSet))
        chart.data = data

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setDrawGridBackground(false)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(days)
            setDrawGridLines(false)
            setDrawAxisLine(false)
            granularity = 1f
            textSize = 10f
            textColor = Color.parseColor("#757575")
            axisMinimum = -0.5f
            axisMaximum = barEntries.size - 0.5f
        }

        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#F0F0F0")
            setDrawAxisLine(false)
            axisMinimum = 0f
            textColor = Color.parseColor("#757575")
        }

        chart.animateY(1000)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}