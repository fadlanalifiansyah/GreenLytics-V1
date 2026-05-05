package com.example.greenlytics.ui.tips

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenlytics.R
import com.example.greenlytics.databinding.FragmentTipsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TipsFragment : Fragment(R.layout.fragment_tips) {

    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TipsViewModel by viewModels()
    private lateinit var tipsAdapter: TipsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTipsBinding.bind(view)

        setupRecyclerView()
        setupFilterClicks()
        observeViewModel()

        // Memuat konten dari database saat pertama kali dibuka
        viewModel.loadContent()
    }

    private fun setupRecyclerView() {
        tipsAdapter = TipsAdapter(emptyList()) { tip ->
            // Feedback saat tombol "Tandai Selesai" diklik
            Toast.makeText(context, "Hebat! Kamu menyelesaikan: ${tip.judul}", Toast.LENGTH_SHORT).show()
        }

        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tipsAdapter
        }
    }

    private fun setupFilterClicks() {
        // Filter: Semua
        binding.btnFilterSemua.setOnClickListener {
            updateFilterUI(binding.btnFilterSemua)
            viewModel.loadContent()
        }

        // Filter: Transportasi
        binding.btnFilterTransport.setOnClickListener {
            updateFilterUI(binding.btnFilterTransport)
            filterByCategory("Transportasi")
        }

        // Filter: Listrik[cite: 1]
        binding.btnFilterListrik.setOnClickListener {
            updateFilterUI(binding.btnFilterListrik)
            filterByCategory("Listrik")
        }

        // Filter: Sampah[cite: 1]
        binding.btnFilterSampah.setOnClickListener {
            updateFilterUI(binding.btnFilterSampah)
            filterByCategory("Sampah")
        }
    }

    private fun filterByCategory(category: String) {
        viewModel.tipsList.value?.let { allTips ->
            val filtered = allTips.filter { it.kategori.equals(category, ignoreCase = true) }
            tipsAdapter.updateData(filtered)
        }
    }

    private fun updateFilterUI(activeView: View) {
        // Reset semua ke style inactive
        val filters = listOf(binding.btnFilterSemua, binding.btnFilterTransport,
            binding.btnFilterListrik, binding.btnFilterSampah)

        filters.forEach {
            it.setBackgroundResource(R.drawable.bg_chip_inactive)
            // Ganti warna teks ke abu-abu (sesuaikan dengan warna di XML)
            (it as? android.widget.TextView)?.setTextColor(android.graphics.Color.parseColor("#44474E"))
        }

        // Set yang diklik ke style active
        activeView.setBackgroundResource(R.drawable.bg_chip_active)
        (activeView as? android.widget.TextView)?.setTextColor(android.graphics.Color.WHITE)
    }

    private fun observeViewModel() {
        // Update list tips di RecyclerView
        viewModel.tipsList.observe(viewLifecycleOwner) { list ->
            tipsAdapter.updateData(list)
        }

        // Update angka potensi hemat di kartu Insight[cite: 1]
        viewModel.potentialSaving.observe(viewLifecycleOwner) { savingValue ->
            binding.tvPotentialSaving.text = savingValue
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}