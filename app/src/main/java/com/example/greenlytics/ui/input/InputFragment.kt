package com.example.greenlytics.ui.input

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.greenlytics.databinding.FragmentInputBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputFragment : Fragment() {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Menggunakan ViewBinding untuk memanggil ID di XML
        _binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Adapter yang sudah kamu buat
        val adapter = InputPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // 2. Judul untuk masing-masing Tab
        val tabTitles = arrayOf("Transport", "Listrik", "Sampah")

        // 3. Menghubungkan TabLayout dan ViewPager2 (TabLayoutMediator)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Mematikan fitur geser (User harus klik tab) jika ingin lebih stabil
        // binding.viewPager.isUserInputEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}