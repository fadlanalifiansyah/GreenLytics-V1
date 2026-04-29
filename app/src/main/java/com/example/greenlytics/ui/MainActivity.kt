package com.example.greenlytics.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.greenlytics.R
import com.example.greenlytics.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint // <-- Ini import untuk stiker Hilt

@AndroidEntryPoint // <-- STIKER IZIN HILT DITEMPEL DI SINI
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding (Ini menggantikan setContentView yang lama)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil NavController dari NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Hubungkan Bottom Navigation dengan NavController
        binding.bottomNavView.setupWithNavController(navController)

        // 3. LOGIKA UNTUK MENYEMBUNYIKAN NAV BAR
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Daftar halaman yang TIDAK BOLEH ada Nav Bar
                R.id.splashFragment,
                R.id.welcomeFragment,
                R.id.loginFragment -> {
                    // Sembunyikan Nav Bar
                    binding.bottomNavView.visibility = View.GONE
                }
                // Halaman lainnya (Beranda, Input, Progress, dll)
                else -> {
                    // Tampilkan kembali Nav Bar
                    binding.bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }
}