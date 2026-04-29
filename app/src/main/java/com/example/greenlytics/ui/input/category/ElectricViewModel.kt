package com.example.greenlytics.ui.input.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenlytics.utils.CarbonCalculator

class ElectricViewModel : ViewModel() {

    // Status metode: true untuk Rupiah, false untuk kWh
    private val _isRupiahMode = MutableLiveData(true)
    val isRupiahMode: LiveData<Boolean> = _isRupiahMode

    private val _selectedTariff = MutableLiveData(CarbonCalculator.ElectricTariff.R1_900VA)
    val selectedTariff: LiveData<CarbonCalculator.ElectricTariff> = _selectedTariff

    private val _estimatedCarbon = MutableLiveData(0.0)
    val estimatedCarbon: LiveData<Double> = _estimatedCarbon

    fun setMode(isRupiah: Boolean) {
        _isRupiahMode.value = isRupiah
    }

    fun setTariff(tariff: CarbonCalculator.ElectricTariff) {
        _selectedTariff.value = tariff
    }

    fun updateEstimation(input: Double) {
        val isRupiah = _isRupiahMode.value ?: true
        val tariff = _selectedTariff.value ?: CarbonCalculator.ElectricTariff.R1_900VA

        _estimatedCarbon.value = if (isRupiah) {
            CarbonCalculator.calculateElectricityFromRupiah(input, tariff, isMonthlyTagihan = true)
        } else {
            CarbonCalculator.calculateElectricityFromKwh(input, isMonthlyTagihan = true)
        }
    }
}