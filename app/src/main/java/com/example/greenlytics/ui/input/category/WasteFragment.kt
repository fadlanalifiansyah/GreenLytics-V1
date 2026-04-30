package com.example.greenlytics.ui.input.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.greenlytics.utils.LocationHelper
import kotlinx.coroutines.launch
import com.example.greenlytics.R
import com.example.greenlytics.databinding.FragmentWasteBinding
import com.example.greenlytics.ui.input.SharedInputViewModel
import com.example.greenlytics.utils.CarbonCalculator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WasteFragment : Fragment(R.layout.fragment_waste) {

    private var _binding: FragmentWasteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WasteViewModel by viewModels()
    private val sharedViewModel: SharedInputViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWasteBinding.bind(view)

        setupSelectionListeners()
        setupInputListeners()
        observeViewModel()
    }

    private fun setupSelectionListeners() {
        binding.cardOrganik.setOnClickListener { viewModel.setWasteType(CarbonCalculator.WasteType.ORGANIK) }
        binding.cardKertas.setOnClickListener { viewModel.setWasteType(CarbonCalculator.WasteType.KERTAS) }
        binding.cardCampuran.setOnClickListener { viewModel.setWasteType(CarbonCalculator.WasteType.CAMPURAN) }
    }

    private fun setupInputListeners() {
        binding.etBerat.doAfterTextChanged {
            val weight = it.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(weight)
        }

        binding.btnSaveWaste.setOnClickListener {
            val weight = binding.etBerat.text.toString().toDoubleOrNull() ?: 0.0
            if (weight > 0) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val locationHelper = LocationHelper(requireContext())
                    // 🔥 Update: Mengambil paket komplit (Kota, Lat, Lon)
                    val locDetail = locationHelper.getCurrentLocation()
                    val type = viewModel.selectedWasteType.value ?: CarbonCalculator.WasteType.ORGANIK

                    // Simpan ke database
                    sharedViewModel.saveWaste(
                        weight = weight,
                        type = type,
                        cityName = locDetail.cityName,
                        lat = locDetail.lat,
                        lon = locDetail.lon
                    )

                    Toast.makeText(context, "Aktivitas sampah berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    binding.etBerat.text?.clear()
                }
            } else {
                Toast.makeText(context, "Masukkan berat sampah!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.selectedWasteType.observe(viewLifecycleOwner) { type ->
            updateUI(type)
            val weight = binding.etBerat.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(weight)
        }

        viewModel.estimatedCarbon.observe(viewLifecycleOwner) { result ->
            binding.tvResultEmissions.text = String.format("≈ %.2f", result)
        }
    }

    private fun updateUI(selected: CarbonCalculator.WasteType) {
        // 1. Siapkan Referensi Warna & Font
        val lightGreen = android.graphics.Color.parseColor("#E8F5E9")
        val white = android.graphics.Color.WHITE
        val grayStroke = android.graphics.Color.parseColor("#E0E0E0")
        val grayTextIcon = android.graphics.Color.parseColor("#757575")
        val primaryColor = resources.getColor(R.color.primary, null)

        val fontSemiBold = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
        val fontRegular = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins)

        // 2. Kumpulkan Komponen (Organik, Kertas, Campuran)
        val cards = listOf(binding.cardOrganik, binding.cardKertas, binding.cardCampuran)
        val icons = listOf(binding.iconOrganik, binding.iconKertas, binding.iconCampuran)
        val texts = listOf(binding.textOrganik, binding.textKertas, binding.textCampuran)

        // 3. Reset Semua ke Default (Abu-abu & Regular)
        cards.forEach {
            it.setCardBackgroundColor(white)
            it.setStrokeColor(grayStroke)
        }
        icons.forEach {
            it.imageTintList = android.content.res.ColorStateList.valueOf(grayTextIcon)
        }
        texts.forEach {
            it.setTextColor(grayTextIcon)
            it.typeface = fontRegular
        }

        // 4. Tentukan Komponen yang Dipilih
        val (selectedCard, selectedIcon, selectedText) = when (selected) {
            CarbonCalculator.WasteType.ORGANIK -> Triple(binding.cardOrganik, binding.iconOrganik, binding.textOrganik)
            CarbonCalculator.WasteType.KERTAS -> Triple(binding.cardKertas, binding.iconKertas, binding.textKertas)
            CarbonCalculator.WasteType.CAMPURAN -> Triple(binding.cardCampuran, binding.iconCampuran, binding.textCampuran)
        }

        // 5. Terapkan Style Aktif (Hijau & SemiBold)
        selectedCard.setCardBackgroundColor(lightGreen)
        selectedCard.setStrokeColor(primaryColor)
        selectedIcon.imageTintList = android.content.res.ColorStateList.valueOf(primaryColor)
        selectedText.setTextColor(primaryColor)
        selectedText.typeface = fontSemiBold
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}