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
import com.example.greenlytics.databinding.FragmentElectricBinding
import com.example.greenlytics.ui.input.SharedInputViewModel
import com.example.greenlytics.utils.CarbonCalculator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectricFragment : Fragment(R.layout.fragment_electric) {

    private var _binding: FragmentElectricBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ElectricViewModel by viewModels()
    private val sharedViewModel: SharedInputViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentElectricBinding.bind(view)

        setupSelectionListeners()
        setupInputListeners()
        observeViewModel()
    }

    private fun setupSelectionListeners() {
        // Switch Metode (Rupiah / kWh)
        binding.cardRupiah.setOnClickListener { viewModel.setMode(true) }
        binding.cardKwh.setOnClickListener { viewModel.setMode(false) }

        // Switch Golongan Tarif
        binding.card900.setOnClickListener { viewModel.setTariff(CarbonCalculator.ElectricTariff.R1_900VA) }
        binding.card1300.setOnClickListener { viewModel.setTariff(CarbonCalculator.ElectricTariff.R1_1300_2200VA) }
        binding.card3500.setOnClickListener { viewModel.setTariff(CarbonCalculator.ElectricTariff.R2_3500_5500VA) }
    }

    private fun setupInputListeners() {
        binding.etListrik.doAfterTextChanged {
            val input = it.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(input)
        }

        binding.btnSaveElectric.setOnClickListener {
            val input = binding.etListrik.text.toString().toDoubleOrNull() ?: 0.0

            // 1. Ambil hasil emisi yang sudah dihitung akurat oleh kalkulator
            val carbonResult = viewModel.estimatedCarbon.value ?: 0.0

            // 2. Cek apakah user sedang pakai mode Rupiah atau kWh
            val isRupiah = viewModel.isRupiahMode.value == true
            val unit = if (isRupiah) "IDR" else "kWh"

            if (input > 0) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val locationHelper = LocationHelper(requireContext())
                    // 🔥 Update: Mengambil paket komplit (Kota, Lat, Lon)
                    val locDetail = locationHelper.getCurrentLocation()

                    // 3. Simpan ke database melalui SharedViewModel dengan data yang lengkap
                    sharedViewModel.saveElectricity(
                        inputValue = input,
                        unit = unit,
                        carbonResult = carbonResult,
                        cityName = locDetail.cityName,
                        lat = locDetail.lat,
                        lon = locDetail.lon
                    )

                    Toast.makeText(context, "Data Listrik Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                    binding.etListrik.text?.clear()
                }
            } else {
                Toast.makeText(context, "Masukkan nilai pemakaian!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // UI Perubahan Metode
        viewModel.isRupiahMode.observe(viewLifecycleOwner) { isRupiah ->
            updateMethodUI(isRupiah)
            val input = binding.etListrik.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(input)
        }

        // UI Perubahan Tarif
        viewModel.selectedTariff.observe(viewLifecycleOwner) { tariff ->
            updateTariffUI(tariff)
            val input = binding.etListrik.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(input)
        }

        // Update Hasil Akhir
        viewModel.estimatedCarbon.observe(viewLifecycleOwner) { result ->
            binding.tvResultEmissions.text = String.format("≈ %.2f", result)
        }
    }

    private fun updateMethodUI(isRupiah: Boolean) {
        val lightGreen = android.graphics.Color.parseColor("#E8F5E9")
        val white = android.graphics.Color.WHITE
        val grayStroke = android.graphics.Color.parseColor("#E0E0E0")
        val grayText = android.graphics.Color.parseColor("#757575")
        val primaryColor = resources.getColor(R.color.primary, null)

        // Ambil Font
        val fontSemiBold = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
        val fontRegular = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins)

        if (isRupiah) {
            // Mode Rupiah Aktif
            binding.cardRupiah.setCardBackgroundColor(lightGreen)
            binding.cardRupiah.setStrokeColor(primaryColor)
            binding.textRupiah.setTextColor(primaryColor)
            binding.textRupiah.typeface = fontSemiBold

            // Mode kWh Mati
            binding.cardKwh.setCardBackgroundColor(white)
            binding.cardKwh.setStrokeColor(grayStroke)
            binding.textKwh.setTextColor(grayText)
            binding.textKwh.typeface = fontRegular

            binding.layoutTariff.visibility = View.VISIBLE
            binding.layoutInputListrik.prefixText = "Rp "
            binding.layoutInputListrik.suffixText = null
            binding.etListrik.hint = "Contoh: 150000"
            binding.tvInputLabel.text = "Total Tagihan Bulanan"
        } else {
            // Mode kWh Aktif
            binding.cardKwh.setCardBackgroundColor(lightGreen)
            binding.cardKwh.setStrokeColor(primaryColor)
            binding.textKwh.setTextColor(primaryColor)
            binding.textKwh.typeface = fontSemiBold

            // Mode Rupiah Mati
            binding.cardRupiah.setCardBackgroundColor(white)
            binding.cardRupiah.setStrokeColor(grayStroke)
            binding.textRupiah.setTextColor(grayText)
            binding.textRupiah.typeface = fontRegular

            binding.layoutTariff.visibility = View.GONE
            binding.layoutInputListrik.prefixText = null
            binding.layoutInputListrik.suffixText = "kWh"
            binding.etListrik.hint = "Contoh: 200"
            binding.tvInputLabel.text = "Total Pemakaian Bulanan (kWh)"
        }
    }

    private fun updateTariffUI(selected: CarbonCalculator.ElectricTariff) {
        val lightGreen = android.graphics.Color.parseColor("#E8F5E9")
        val white = android.graphics.Color.WHITE
        val grayStroke = android.graphics.Color.parseColor("#E0E0E0")
        val grayText = android.graphics.Color.parseColor("#757575")
        val primaryColor = resources.getColor(R.color.primary, null)

        val fontSemiBold = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
        val fontRegular = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins)

        // List komponen untuk mempermudah reset
        val cards = listOf(binding.card900, binding.card1300, binding.card3500)
        val texts = listOf(binding.text900, binding.text1300, binding.text3500)

        // Reset semua ke default
        cards.forEach { it.setCardBackgroundColor(white); it.setStrokeColor(grayStroke) }
        texts.forEach { it.setTextColor(grayText); it.typeface = fontRegular }

        // Highlight yang dipilih
        val (selectedCard, selectedText) = when(selected) {
            CarbonCalculator.ElectricTariff.R1_900VA -> binding.card900 to binding.text900
            CarbonCalculator.ElectricTariff.R1_1300_2200VA -> binding.card1300 to binding.text1300
            CarbonCalculator.ElectricTariff.R2_3500_5500VA -> binding.card3500 to binding.text3500
        }

        selectedCard.setCardBackgroundColor(lightGreen)
        selectedCard.setStrokeColor(primaryColor)
        selectedText.setTextColor(primaryColor)
        selectedText.typeface = fontSemiBold
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}