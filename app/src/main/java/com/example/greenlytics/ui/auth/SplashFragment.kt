package com.example.greenlytics.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.greenlytics.R
import com.example.greenlytics.ui.auth.AuthViewModel

class SplashFragment : Fragment(R.layout.fragment_splash) {

    // Menghubungkan ke ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengamati data 'shouldNavigate' dari ViewModel
        viewModel.shouldNavigate.observe(viewLifecycleOwner) { ready ->
            if (ready) {
                // Berpindah ke WelcomeFragment menggunakan ID action di nav_graph
                findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)

                // Memberitahu ViewModel bahwa navigasi sudah dilakukan
                viewModel.onNavigated()
            }
        }
    }
}