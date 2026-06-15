package com.example.greenlytics.ui.dashboard

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.greenlytics.R
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private val sharedPrefs by lazy {
        requireContext().getSharedPreferences("GreenLyticsPrefs", android.content.Context.MODE_PRIVATE)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == false) {
            Toast.makeText(requireContext(), "Lokasi ditolak. Peta tidak akan akurat.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- TAMBAHAN: Izin Notifikasi ---
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            triggerLocalNotification()
        } else {
            Toast.makeText(requireContext(), "Notifikasi tidak aktif", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        setupObservers(view)
        setupClickListeners(view)
    }

    private fun setupObservers(view: View) {
        // ... (Kode Observer tetap sama) ...
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val tvEmisiValue = view.findViewById<TextView>(R.id.tvEmisiValue)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarEmission)
        val tvLimit = view.findViewById<TextView>(R.id.tvLimit)
        val tvValueTransport = view.findViewById<TextView>(R.id.tvValueTransport)
        val tvValueElectric = view.findViewById<TextView>(R.id.tvValueElectric)
        val tvValueShopping = view.findViewById<TextView>(R.id.tvValueShopping)
        val tvValueWaste = view.findViewById<TextView>(R.id.tvValueWaste)
        val chart = view.findViewById<CombinedChart>(R.id.combinedChart)
        val tvStreakCount = view.findViewById<TextView>(R.id.tvStreakCount)

        viewModel.greetingText.observe(viewLifecycleOwner) { tvGreeting.text = it }
        viewModel.todayTotal.observe(viewLifecycleOwner) { total ->
            tvEmisiValue.text = String.format("%.1f", total)
            val targetString = sharedPrefs.getString("TARGET_EMISI", "8") ?: "8"
            val targetEmisi = targetString.toDoubleOrNull() ?: 8.0
            tvLimit.text = "Batas: $targetString kg"
            progressBar.progress = ((total / targetEmisi) * 100).toInt().coerceAtMost(100)
        }
        viewModel.transportTotal.observe(viewLifecycleOwner) { tvValueTransport.text = String.format("%.1f kg", it) }
        viewModel.electricTotal.observe(viewLifecycleOwner) { tvValueElectric.text = String.format("%.1f kg", it) }
        viewModel.shoppingTotal.observe(viewLifecycleOwner) { tvValueShopping.text = String.format("%.1f kg", it) }
        viewModel.wasteTotal.observe(viewLifecycleOwner) { tvValueWaste.text = String.format("%.1f kg", it) }
        viewModel.chartData.observe(viewLifecycleOwner) { setupChart(chart, it) }
        viewModel.streakCount.observe(viewLifecycleOwner) { tvStreakCount.text = "$it Hari Beruntun" }
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<CardView>(R.id.cardStreak).setOnClickListener {
            Toast.makeText(context, "Cek detail di Profil/Progress!", Toast.LENGTH_SHORT).show()
        }

        // --- TAMBAHAN: Logika Klik Tombol Notifikasi ---
        view.findViewById<CardView>(R.id.btnNotification).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    triggerLocalNotification()
                } else {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                triggerLocalNotification()
            }
        }
    }

    private fun triggerLocalNotification() {
        val channelId = "greenlytics_notif_channel"
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pengingat Harian", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_bell) // Pastikan icon ini ada
            .setContentTitle("Halo GreenLytics! 🍃")
            .setContentText("Ayo kurangi jejak karbonmu hari ini.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(1001, builder.build())
    }

    private fun setupChart(chart: CombinedChart, dataList: List<Pair<String, Float>>) {
        // ... (Kode setupChart kamu tetap sama) ...
        val barEntries = dataList.mapIndexed { i, d -> BarEntry(i.toFloat(), d.second) }
        val lineEntries = dataList.mapIndexed { i, d -> Entry(i.toFloat(), d.second) }
        val labels = dataList.map { it.first }

        val barDataSet = BarDataSet(barEntries, "Emisi").apply { color = Color.parseColor("#81C784"); setDrawValues(false) }
        val lineDataSet = LineDataSet(lineEntries, "Tren").apply {
            color = Color.parseColor("#64B5F6"); setCircleColor(Color.parseColor("#64B5F6")); lineWidth = 3f; mode = LineDataSet.Mode.CUBIC_BEZIER; setDrawValues(false)
        }
        chart.data = CombinedData().apply { setData(BarData(barDataSet)); setData(LineData(lineDataSet)) }
        chart.description.isEnabled = false; chart.legend.isEnabled = false; chart.axisRight.isEnabled = false; chart.setDrawGridBackground(false)
        chart.xAxis.apply { position = XAxis.XAxisPosition.BOTTOM; setDrawGridLines(false); setDrawAxisLine(false); valueFormatter = IndexAxisValueFormatter(labels); granularity = 1f }
        chart.axisLeft.apply { setDrawGridLines(true); gridColor = Color.parseColor("#F0F0F0"); setDrawAxisLine(false); axisMinimum = 0f }
        chart.animateY(1000)
        chart.invalidate()
    }
}