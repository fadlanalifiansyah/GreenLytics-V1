package com.example.greenlytics.ui.input.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenlytics.utils.CarbonCalculator

class TransportViewModel : ViewModel() {

    // Menyimpan pilihan kendaraan (Default: Mobil)
    private val _selectedVehicle = MutableLiveData(CarbonCalculator.VehicleType.MOBIL)
    val selectedVehicle: LiveData<CarbonCalculator.VehicleType> = _selectedVehicle

    // Menyimpan estimasi emisi yang tampil di kartu
    private val _estimatedCarbon = MutableLiveData(0.0)
    val estimatedCarbon: LiveData<Double> = _estimatedCarbon

    fun setVehicle(type: CarbonCalculator.VehicleType) {
        _selectedVehicle.value = type
    }

    fun updateEstimation(distance: Double) {
        val vehicle = _selectedVehicle.value ?: CarbonCalculator.VehicleType.MOBIL
        _estimatedCarbon.value = CarbonCalculator.calculateTransport(distance, vehicle)
    }
}