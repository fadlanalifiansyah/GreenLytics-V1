package com.example.greenlytics.ui.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenlytics.data.local.EmissionEntity
import com.example.greenlytics.data.repository.EmissionRepo
import com.example.greenlytics.utils.CarbonCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedInputViewModel @Inject constructor(
    private val repository: EmissionRepo
) : ViewModel() {

    /* Fungsi Simpan untuk Transportasi */
    fun saveTransport(distance: Double, type: CarbonCalculator.VehicleType, passengers: Int = 1, cityName: String? = null, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            val result = CarbonCalculator.calculateTransport(distance, type, passengers)
            // Kategori: Transportasi, Sub: Jenis Kendaraan (Motor/Mobil/Bus), dst.
            insertToDatabase("Transportasi", type.name, distance, "km", result, cityName, lat, lon)
        }
    }

    /* Fungsi Simpan untuk Listrik (Menerima hasil dari ElectricViewModel */
    fun saveElectricity(inputValue: Double, unit: String, carbonResult: Double, cityName: String? = null, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            // Langsung masukkan carbonResult ke database, tidak perlu dihitung ulang di sini
            insertToDatabase("Listrik", "Rumah Tangga", inputValue, unit, carbonResult, cityName, lat, lon)
        }
    }

    /**
     * Fungsi Simpan untuk Sampah
     */
    fun saveWaste(weight: Double, type: CarbonCalculator.WasteType, cityName: String? = null, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            val result = CarbonCalculator.calculateWaste(weight, type)
            insertToDatabase("Sampah", type.name, weight, "kg", result, cityName, lat, lon)
        }
    }

    /**
     * Fungsi Simpan untuk Belanja (Spend-based)
     */
    fun saveShopping(spendRp: Double, category: CarbonCalculator.ShoppingCategory, cityName: String? = null, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            val result = CarbonCalculator.calculateShopping(spendRp, category)
            insertToDatabase("Belanja", category.name, spendRp, "IDR", result, cityName, lat, lon)
        }
    }

    /**
     * Fungsi Private untuk membungkus data ke dalam EmissionEntity dan menyimpannya ke Database
     */
    private suspend fun insertToDatabase(
        cat: String,
        subCat: String,
        valInput: Double,
        unit: String,
        carbon: Double,
        cityName: String? = null,
        lat: Double? = null,
        lon: Double? = null
    ) {
        val newEmission = EmissionEntity(
            kategori = cat,
            subKategori = subCat,
            nilaiInput = valInput,
            satuan = unit,
            emisiKarbon = carbon,
            tanggalInput = System.currentTimeMillis(),
            latitude = lat,
            longitude = lon,
            cityName = cityName,
            isSynced = false,
            userId = ""
        )
        repository.insertEmission(newEmission)
    }
}