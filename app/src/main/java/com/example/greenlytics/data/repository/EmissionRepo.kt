package com.example.greenlytics.data.repository

import com.example.greenlytics.data.local.EmissionDao
import com.example.greenlytics.data.local.EmissionEntity
import javax.inject.Inject

class EmissionRepo @Inject constructor(
    private val dao: EmissionDao
) {

    // 1. Sesuaikan nama fungsi dengan yang ada di DAO (getTotalCarbonEmission)
    suspend fun getTotalCarbon(): Double {
        return dao.getTotalCarbonEmission() ?: 0.0
    }

    // 2. Ini sudah benar
    suspend fun getTotalActivitiesCount(): Int {
        return dao.getTotalActivitiesCount()
    }

    // 3. Fungsi untuk menyimpan data dari kalkulator
    suspend fun insertEmission(emission: EmissionEntity) {
        dao.insertEmission(emission)
    }

    // Tambahkan ini di dalam class EmissionRepo
    suspend fun getAllEmissions(): List<EmissionEntity> {
        return dao.getAllEmissions()
    }

    suspend fun deleteEmission(id: Int) {
        dao.deleteEmissionById(id)
    }

    suspend fun getEmissionsBetweenDates(start: Long, end: Long): Double {
        return dao.getEmissionsBetweenDates(start, end) ?: 0.0
    }

    suspend fun getLowestEmission(): Double {
        return dao.getLowestEmission() ?: 0.0
    }
}