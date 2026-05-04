package com.example.greenlytics.ui.dashboard

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView // TAMBAHAN: Import TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.greenlytics.R

class DashboardFragment : Fragment() {

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private val viewModel: DashboardViewModel by viewModels()

    // 1. MESIN PENANGKAP IZIN LOKASI
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            }
            else -> {
                // Pengguna menolak izin.
                Toast.makeText(
                    requireContext(),
                    "Lokasi ditolak. Fitur Peta Emisi Kota tidak akan mencatat lokasimu nanti.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MEMANGGIL POP-UP OTOMATIS (Tetap dipertahankan)
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        // Tambahan : Hubungkan UI tvGreeting dengan data dari ViewModel
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            val greetingText = "Selamat Pagi,\n$name!"
            tvGreeting.text = greetingText
        }
    }
}