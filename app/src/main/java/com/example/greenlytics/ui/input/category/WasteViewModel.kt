package com.example.greenlytics.ui.input.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenlytics.utils.CarbonCalculator

class WasteViewModel : ViewModel() {

    // Default: Organik
    private val _selectedWasteType = MutableLiveData(CarbonCalculator.WasteType.ORGANIK)
    val selectedWasteType: LiveData<CarbonCalculator.WasteType> = _selectedWasteType

    private val _estimatedCarbon = MutableLiveData(0.0)
    val estimatedCarbon: LiveData<Double> = _estimatedCarbon

    fun setWasteType(type: CarbonCalculator.WasteType) {
        _selectedWasteType.value = type
    }

    fun updateEstimation(weight: Double) {
        val type = _selectedWasteType.value ?: CarbonCalculator.WasteType.ORGANIK
        _estimatedCarbon.value = CarbonCalculator.calculateWaste(weight, type)
    }
}