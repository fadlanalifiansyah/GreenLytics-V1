package com.example.greenlytics.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface EmissionDao {

    // Untuk menyimpan 1 data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmission(emission: EmissionEntity)

    // 1. Memasukkan BANYAK aktivitas emisi sekaligus (Untuk Tarik Data dari Firebase)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEmissions(emissions: List<EmissionEntity>)

    // 2. Mengambil semua riwayat aktivitas, diurutkan dari yang paling baru (DESC)
    @Query("SELECT * FROM emission_table ORDER BY tanggalInput DESC")
    suspend fun getAllEmissions(): List<EmissionEntity>

    // 3. Mengambil KHUSUS data yang belum diunggah ke Firebase (isSynced = false / 0)
    @Query("SELECT * FROM emission_table WHERE isSynced = 0")
    suspend fun getUnsyncedEmissions(): List<EmissionEntity>

    // 4. Mengupdate data yang sudah ada (Misal: mengubah isSynced jadi true setelah internet menyala)
    @Update
    suspend fun updateEmission(emission: EmissionEntity)

    // 5. Mengambil total seluruh emisi karbon yang pernah diinput pengguna
    @Query("SELECT SUM(emisiKarbon) FROM emission_table")
    suspend fun getTotalCarbonEmission(): Double?

    // 6. Menghapus data emisi (jika pengguna salah input)
    @Query("DELETE FROM emission_table WHERE id = :emissionId")
    suspend fun deleteEmissionById(emissionId: Int)

    // 7. Menghitung jumlah TOTAL AKTIVITAS yang sudah diinput (Untuk profil: "42 aktivitas")
    @Query("SELECT COUNT(*) FROM emission_table")
    suspend fun getTotalActivitiesCount(): Int

    // 8. Menjumlahkan emisi khusus di rentang waktu tertentu (Untuk profil: "Total emisi bulan ini")
    @Query("SELECT SUM(emisiKarbon) FROM emission_table WHERE tanggalInput BETWEEN :startTime AND :endTime")
    suspend fun getEmissionsBetweenDates(startTime: Long, endTime: Long): Double?

    // 9. Mengambil nilai emisi paling rendah (terbaik)
    @Query("SELECT MIN(emisiKarbon) FROM emission_table")
    suspend fun getLowestEmission(): Double?

    // Menarik daftar lengkap emisi dalam rentang waktu (Untuk Dashboard)
    @Query("SELECT * FROM emission_table WHERE tanggalInput BETWEEN :startTime AND :endTime")
    suspend fun getEmissionsListBetweenDates(startTime: Long, endTime: Long): List<EmissionEntity>
}