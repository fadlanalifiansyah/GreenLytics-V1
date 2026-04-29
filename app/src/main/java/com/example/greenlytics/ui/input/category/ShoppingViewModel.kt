package com.example.greenlytics.ui.input.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenlytics.utils.CarbonCalculator

class ShoppingViewModel : ViewModel() {

    // Default kategori: Makanan & Minuman
    private val _selectedCategory = MutableLiveData(CarbonCalculator.ShoppingCategory.MAKANAN_MINUMAN)
    val selectedCategory: LiveData<CarbonCalculator.ShoppingCategory> = _selectedCategory

    private val _estimatedCarbon = MutableLiveData(0.0)
    val estimatedCarbon: LiveData<Double> = _estimatedCarbon

    fun setCategory(category: CarbonCalculator.ShoppingCategory) {
        _selectedCategory.value = category
    }

    fun updateEstimation(amountRp: Double) {
        val category = _selectedCategory.value ?: CarbonCalculator.ShoppingCategory.MAKANAN_MINUMAN
        _estimatedCarbon.value = CarbonCalculator.calculateShopping(amountRp, category)
    }
}