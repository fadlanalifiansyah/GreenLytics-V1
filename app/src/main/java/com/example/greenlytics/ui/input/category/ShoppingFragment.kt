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
import com.example.greenlytics.databinding.FragmentShoppingBinding
import com.example.greenlytics.ui.input.SharedInputViewModel
import com.example.greenlytics.utils.CarbonCalculator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingFragment : Fragment(R.layout.fragment_shopping) {

    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingViewModel by viewModels()
    private val sharedViewModel: SharedInputViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShoppingBinding.bind(view)

        setupCategorySelection()
        setupInputListeners()
        observeViewModel()
    }

    private fun setupCategorySelection() {
        binding.cardMakanan.setOnClickListener { viewModel.setCategory(CarbonCalculator.ShoppingCategory.MAKANAN_MINUMAN) }
        binding.cardFashion.setOnClickListener { viewModel.setCategory(CarbonCalculator.ShoppingCategory.FASHION) }
        binding.cardElektronik.setOnClickListener { viewModel.setCategory(CarbonCalculator.ShoppingCategory.ELEKTRONIK) }
        binding.cardRumah.setOnClickListener { viewModel.setCategory(CarbonCalculator.ShoppingCategory.PERABOT) }
        binding.cardKecantikan.setOnClickListener { viewModel.setCategory(CarbonCalculator.ShoppingCategory.KECANTIKAN) }
        binding.cardHiburan.setOnClickListener { viewModel.setCategory(CarbonCalculator.ShoppingCategory.HIBURAN) }
    }

    private fun setupInputListeners() {
        binding.etAmount.doAfterTextChanged {
            val amount = it.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(amount)
        }

        binding.btnSaveShopping.setOnClickListener {
            val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val locationHelper = LocationHelper(requireContext())
                    // 🔥 Update: Mengambil paket komplit (Kota, Lat, Lon)
                    val locDetail = locationHelper.getCurrentLocation()
                    val category = viewModel.selectedCategory.value ?: CarbonCalculator.ShoppingCategory.MAKANAN_MINUMAN

                    // Simpan ke database melalui SharedViewModel
                    sharedViewModel.saveShopping(
                        spendRp = amount,
                        category = category,
                        cityName = locDetail.cityName,
                        lat = locDetail.lat,
                        lon = locDetail.lon
                    )

                    Toast.makeText(context, "Aktivitas belanja berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    binding.etAmount.text?.clear()
                }
            } else {
                Toast.makeText(context, "Masukkan nominal belanja!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            updateUI(category)
            val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.updateEstimation(amount)
        }

        viewModel.estimatedCarbon.observe(viewLifecycleOwner) { result ->
            binding.tvResultEmissions.text = String.format("≈ %.2f", result)
        }
    }

    private fun updateUI(selected: CarbonCalculator.ShoppingCategory) {
        // 1. Warna & Font
        val lightGreen = android.graphics.Color.parseColor("#E8F5E9")
        val white = android.graphics.Color.WHITE
        val grayStroke = android.graphics.Color.parseColor("#E0E0E0")
        val grayTextIcon = android.graphics.Color.parseColor("#757575")
        val primaryColor = resources.getColor(R.color.primary, null)

        val fontSemiBold = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
        val fontRegular = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins)

        // 2. Kumpulkan semua komponen
        val cards = listOf(
            binding.cardMakanan, binding.cardFashion, binding.cardElektronik,
            binding.cardRumah, binding.cardKecantikan, binding.cardHiburan
        )
        val icons = listOf(
            binding.iconMakanan, binding.iconFashion, binding.iconElektronik,
            binding.iconRumah, binding.iconKecantikan, binding.iconHiburan
        )
        val texts = listOf(
            binding.textMakanan, binding.textFashion, binding.textElektronik,
            binding.textRumah, binding.textKecantikan, binding.textHiburan
        )

        // 3. Reset semua ke kondisi default (Abu-abu & Regular)
        cards.forEach { it.setCardBackgroundColor(white); it.setStrokeColor(grayStroke) }
        icons.forEach { it.imageTintList = android.content.res.ColorStateList.valueOf(grayTextIcon) }
        texts.forEach { it.setTextColor(grayTextIcon); it.typeface = fontRegular }

        // 4. Tentukan mana yang dipilih berdasarkan kategori
        val index = when (selected) {
            CarbonCalculator.ShoppingCategory.MAKANAN_MINUMAN -> 0
            CarbonCalculator.ShoppingCategory.FASHION -> 1
            CarbonCalculator.ShoppingCategory.ELEKTRONIK -> 2
            CarbonCalculator.ShoppingCategory.PERABOT -> 3
            CarbonCalculator.ShoppingCategory.KECANTIKAN -> 4
            CarbonCalculator.ShoppingCategory.HIBURAN -> 5
        }

        // 5. Apply warna Hijau & Font SemiBold ke yang dipilih
        cards[index].setCardBackgroundColor(lightGreen)
        cards[index].setStrokeColor(primaryColor)
        icons[index].imageTintList = android.content.res.ColorStateList.valueOf(primaryColor)
        texts[index].setTextColor(primaryColor)
        texts[index].typeface = fontSemiBold
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}