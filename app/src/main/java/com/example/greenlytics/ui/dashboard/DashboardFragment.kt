package com.example.greenlytics.ui.dashboard

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.greenlytics.R
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == false) {
            Toast.makeText(requireContext(), "Lokasi ditolak. Peta tidak akan akurat.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setupObservers(view)
        setupClickListeners(view)
    }

    private fun setupObservers(view: View) {
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val tvEmisiValue = view.findViewById<TextView>(R.id.tvEmisiValue)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarEmission)

        val tvValueTransport = view.findViewById<TextView>(R.id.tvValueTransport)
        val tvValueElectric = view.findViewById<TextView>(R.id.tvValueElectric)
        val tvValueWaste = view.findViewById<TextView>(R.id.tvValueWaste)
        val chart = view.findViewById<CombinedChart>(R.id.combinedChart)
        val tvStreakCount = view.findViewById<TextView>(R.id.tvStreakCount)

        // 1. Sapaan & Total
        viewModel.greetingText.observe(viewLifecycleOwner) { tvGreeting.text = it }
        viewModel.todayTotal.observe(viewLifecycleOwner) { total ->
            tvEmisiValue.text = String.format("%.1f", total)
            progressBar.progress = ((total / 8.0) * 100).toInt().coerceAtMost(100)
        }

        // 2. Kategori
        viewModel.transportTotal.observe(viewLifecycleOwner) { tvValueTransport.text = String.format("%.1f kg", it) }
        viewModel.electricTotal.observe(viewLifecycleOwner) { tvValueElectric.text = String.format("%.1f kg", it) }
        viewModel.wasteTotal.observe(viewLifecycleOwner) { tvValueWaste.text = String.format("%.1f kg", it) }

        // 3. Setup Grafik Combined
        viewModel.chartData.observe(viewLifecycleOwner) { dataList ->
            setupChart(chart, dataList)
        }

        // 4. Update Streak Real-time
        viewModel.streakCount.observe(viewLifecycleOwner) { streak ->
            tvStreakCount.text = "$streak Hari Beruntun"
        }
    }

    private fun setupClickListeners(view: View) {
        val cardStreak = view.findViewById<CardView>(R.id.cardStreak)
        cardStreak.setOnClickListener {
            Toast.makeText(context, "Cek detail Streak dan Badge kamu di menu Profil/Progress!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupChart(chart: CombinedChart, dataList: List<Pair<String, Float>>) {
        val barEntries = ArrayList<BarEntry>()
        val lineEntries = ArrayList<com.github.mikephil.charting.data.Entry>()
        val labels = ArrayList<String>()

        for ((index, data) in dataList.withIndex()) {
            val xPos = index.toFloat()
            barEntries.add(BarEntry(xPos, data.second))
            lineEntries.add(com.github.mikephil.charting.data.Entry(xPos, data.second))
            labels.add(data.first)
        }

        // Setup Bar (Hijau)
        val barDataSet = BarDataSet(barEntries, "Emisi Karbon")
        barDataSet.color = Color.parseColor("#81C784")
        barDataSet.setDrawValues(false)

        // Setup Line (Biru Melengkung)
        val lineDataSet = com.github.mikephil.charting.data.LineDataSet(lineEntries, "Tren")
        lineDataSet.color = Color.parseColor("#64B5F6")
        lineDataSet.setCircleColor(Color.parseColor("#64B5F6"))
        lineDataSet.lineWidth = 3f
        lineDataSet.mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawValues(false)

        val combinedData = CombinedData()
        combinedData.setData(BarData(barDataSet))
        combinedData.setData(com.github.mikephil.charting.data.LineData(lineDataSet))
        chart.data = combinedData

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setDrawGridBackground(false)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            textSize = 10f
            textColor = Color.parseColor("#757575")
            axisMinimum = -0.5f
            axisMaximum = dataList.size - 0.5f
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
}