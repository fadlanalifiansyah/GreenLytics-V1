package com.example.greenlytics.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.greenlytics.R
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ProgressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chart = view.findViewById<CombinedChart>(R.id.combinedChart)

        if (chart != null) {
            chart.setNoDataText("Sedang memuat grafik...")
            setupMyChart(chart)
        }
    }

    private fun setupMyChart(chart: CombinedChart) {
        val days = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

        val barEntries = ArrayList<BarEntry>()
        barEntries.add(BarEntry(0f, 30f))
        barEntries.add(BarEntry(1f, 45f))
        barEntries.add(BarEntry(2f, 25f))
        barEntries.add(BarEntry(3f, 50f))
        barEntries.add(BarEntry(4f, 65f))

        val barDataSet = BarDataSet(barEntries, "Emisi")
        barDataSet.color = Color.parseColor("#81C784")
        barDataSet.setDrawValues(false)

        val lineEntries = ArrayList<Entry>()
        lineEntries.add(Entry(0f, 40f))
        lineEntries.add(Entry(1f, 55f))
        lineEntries.add(Entry(2f, 45f))
        lineEntries.add(Entry(3f, 60f))
        lineEntries.add(Entry(4f, 75f))

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

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(days)
            setDrawGridLines(false)
            granularity = 1f
        }

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.notifyDataSetChanged()
        chart.invalidate()
    }
}