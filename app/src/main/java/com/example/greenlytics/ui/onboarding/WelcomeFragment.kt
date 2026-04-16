package com.example.greenlytics.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.greenlytics.R

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Layout ini yang akan ditampilkan
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cari tombol di dalam view yang baru saja dibuat
        val btnNext = view.findViewById<Button>(R.id.btn_mulai_sekarang)

        btnNext.setOnClickListener {
            // Navigasi menggunakan ID yang ada di nav_graph.xml
            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
        }
    }
}