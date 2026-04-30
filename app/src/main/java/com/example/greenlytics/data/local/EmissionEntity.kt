package com.example.greenlytics.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emission_table")
data class EmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val kategori: String,
    val subKategori: String,
    val nilaiInput: Double,
    val satuan: String,
    val emisiKarbon: Double,
    val tanggalInput: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cityName: String? = null,
    val isSynced: Boolean = false,
    val userId: String = ""
)