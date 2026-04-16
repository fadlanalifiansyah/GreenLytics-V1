package com.example.greenlytics.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.greenlytics.R

class SplashFragment : Fragment() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startSplashScreenTimer()

        viewModel.navigateToWelcome.observe(viewLifecycleOwner) { harusPindah ->
            if (harusPindah) {
                findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)
            }
        }
    }
}