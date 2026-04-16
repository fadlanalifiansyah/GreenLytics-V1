package com.example.greenlytics.ui.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.greenlytics.R

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Menghubungkan fragment dengan layout XML res/layout/fragment_splash.xml
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logika Timer: Menjalankan perintah setelah jeda waktu tertentu
        Handler(Looper.getMainLooper()).postDelayed({

            // Periksa apakah fragment masih aktif sebelum navigasi untuk menghindari crash
            if (isAdded) {
                // Perintah pindah halaman menggunakan ID action di nav_graph.xml
                findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)
            }

        }, 3000) // 3000 milidetik = 3 Detik
    }
}