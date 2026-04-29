package com.example.greenlytics.ui.input.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.greenlytics.R
import com.example.greenlytics.databinding.FragmentTransportBinding
import com.example.greenlytics.ui.input.SharedInputViewModel
import com.example.greenlytics.utils.CarbonCalculator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransportFragment : Fragment(R.layout.fragment_transport) {

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransportViewModel by viewModels()
    private val sharedViewModel: SharedInputViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransportBinding.bind(view)

        setupVehicleSelection()
        setupListeners()
        observeViewModel()
    }

    private fun setupVehicleSelection() {
        // Logic Klik Kartu Kendaraan
        binding.cardMobil.setOnClickListener { viewModel.setVehicle(CarbonCalculator.VehicleType.MOBIL) }
        binding.cardMotor.setOnClickListener { viewModel.setVehicle(CarbonCalculator.VehicleType.MOTOR) }
        binding.cardBus.setOnClickListener { viewModel.setVehicle(CarbonCalculator.VehicleType.BUS) }
        binding.cardKRL.setOnClickListener { viewModel.setVehicle(CarbonCalculator.VehicleType.KRL) }
    }

    private fun setupListeners() {
        // Update estimasi tiap kali angka jarak berubah
        binding.etDistance.doAfterTextChanged { recalculate() }

        // Logic Slider Bus
        binding.sliderPassenger.addOnChangeListener { _, value, _ ->
            binding.tvPassengerLabel.text = "Jumlah Penumpang: ${value.toInt()} Orang"
            recalculate()
        }

        // Tombol Simpan Permanen ke Database
        binding.btnSaveTransport.setOnClickListener {
            val distance = binding.etDistance.text.toString().toDoubleOrNull() ?: 0.0
            if (distance > 0) {
                val vehicle = viewModel.selectedVehicle.value ?: CarbonCalculator.VehicleType.MOBIL
                val passengers = binding.sliderPassenger.value.toInt()

                sharedViewModel.saveTransport(distance, vehicle, passengers)

                Toast.makeText(context, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                binding.etDistance.text?.clear()
            } else {
                Toast.makeText(context, "Masukkan jarak tempuh!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Update UI saat kendaraan berubah (Ganti warna Stroke/BG)
        viewModel.selectedVehicle.observe(viewLifecycleOwner) { vehicle ->
            updateCardUI(vehicle)
            recalculate()
        }

        // Update angka di Kartu Gradasi (Hasil Hitung)
        viewModel.estimatedCarbon.observe(viewLifecycleOwner) { result ->
            binding.tvResultEmissions.text = String.format("≈ %.2f", result)
        }
    }

    private fun recalculate() {
        val distance = binding.etDistance.text.toString().toDoubleOrNull() ?: 0.0
        val passengers = binding.sliderPassenger.value.toInt()
        viewModel.updateEstimation(distance, passengers)
    }
    private fun updateCardUI(selected: CarbonCalculator.VehicleType) {
        val lightGreen = android.graphics.Color.parseColor("#E8F5E9")
        val white = android.graphics.Color.WHITE
        val grayStroke = android.graphics.Color.parseColor("#E0E0E0")
        val grayTextIcon = android.graphics.Color.parseColor("#757575")
        val primaryColor = resources.getColor(R.color.primary, null)

        // 1. Ambil Font dari resources
        val fontSemiBold = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
        val fontRegular = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins)

        val cards = listOf(binding.cardMobil, binding.cardMotor, binding.cardBus, binding.cardKRL)
        val icons = listOf(binding.iconMobil, binding.iconMotor, binding.iconBus, binding.iconKRL)
        val textLabels = listOf(binding.textMobil, binding.textMotor, binding.textBus, binding.textKRL)

        // 2. Reset SEMUA (Warna & Font)
        cards.forEach {
            it.setCardBackgroundColor(white)
            it.setStrokeColor(grayStroke)
        }
        icons.forEach {
            it.imageTintList = android.content.res.ColorStateList.valueOf(grayTextIcon)
        }
        textLabels.forEach {
            it.setTextColor(grayTextIcon)
            it.typeface = fontRegular // Reset ke Regular
        }

        // 3. Tentukan mana yang dipilih
        val (selectedCard, selectedIcon, selectedText) = when (selected) {
            CarbonCalculator.VehicleType.MOBIL -> Triple(binding.cardMobil, binding.iconMobil, binding.textMobil)
            CarbonCalculator.VehicleType.MOTOR -> Triple(binding.cardMotor, binding.iconMotor, binding.textMotor)
            CarbonCalculator.VehicleType.BUS -> Triple(binding.cardBus, binding.iconBus, binding.textBus)
            CarbonCalculator.VehicleType.KRL -> Triple(binding.cardKRL, binding.iconKRL, binding.textKRL)
        }

        // 4. Terapkan Style Aktif (Warna & SemiBold)
        selectedCard.setCardBackgroundColor(lightGreen)
        selectedCard.setStrokeColor(primaryColor)
        selectedIcon.imageTintList = android.content.res.ColorStateList.valueOf(primaryColor)

        selectedText.setTextColor(primaryColor)
        selectedText.typeface = fontSemiBold // Set ke SemiBold

        binding.layoutPassenger.visibility = if (selected == CarbonCalculator.VehicleType.BUS) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}