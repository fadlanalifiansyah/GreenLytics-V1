package com.example.greenlytics.data.repository

import com.example.greenlytics.data.local.EmissionDao
import com.example.greenlytics.data.local.EmissionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmissionRepo @Inject constructor(
    private val dao: EmissionDao,
    private val firestore: FirebaseFirestore
) {

    // Mengambil UID akun yang sedang login saat ini
    private val currentUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getTotalCarbon(): Double {
        return dao.getTotalCarbonEmission() ?: 0.0
    }

    suspend fun getTotalActivitiesCount(): Int {
        return dao.getTotalActivitiesCount()
    }

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

    // Menambahkan UID sebelum masuk Room
    suspend fun insertEmission(emission: EmissionEntity) {
        withContext(Dispatchers.IO) {
            // 1. Sisipkan userId ke dalam entitas
            val emissionWithUser = emission.copy(userId = currentUid ?: "unknown")

            // 2. Simpan ke database lokal (Room) terlebih dahulu (Offline-First)
            dao.insertEmission(emissionWithUser)

            // 3. Kirim ke Firebase
            try {
                syncToFirebase(emissionWithUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Menjalankan 2 Tugas (Kota & Personal)
    private suspend fun syncToFirebase(emission: EmissionEntity) {
        val uid = currentUid ?: return // Batal kirim jika belum login

        // Peta Kota
        if (emission.cityName != null) {
            val cityDocRef = firestore.collection("city_emissions").document(emission.cityName)
            val updates = hashMapOf<String, Any>(
                "cityName" to emission.cityName,
                "totalEmissions" to FieldValue.increment(emission.emisiKarbon),
                "totalContributors" to FieldValue.increment(1)
            )
            cityDocRef.set(updates, SetOptions.merge()).await()
        }

        // Sebagai Penyimpanan Pribadi
        val personalRef = firestore
            .collection("users").document(uid)
            .collection("my_emissions").document(emission.tanggalInput.toString())

        // Simpan seluruh data secara utuh ke dalam brankas akun
        personalRef.set(emission).await()
    }

    suspend fun getEmissionsListBetweenDates(start: Long, end: Long): List<EmissionEntity> {
        return dao.getEmissionsListBetweenDates(start, end)
    }

    // Untuk Menarik data dari Firebase saat login di HP baru
    suspend fun restoreDataFromCloud() {
        val uid = currentUid ?: return

        withContext(Dispatchers.IO) {
            try {
                // 1. Brankas di Firebase
                val snapshot = firestore
                    .collection("users").document(uid)
                    .collection("my_emissions")
                    .get()
                    .await()

                // 2. Ubah data dari Firebase kembali menjadi format Room
                val restoredList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(EmissionEntity::class.java)?.copy(isSynced = true)
                }

                // 3. Masukkan semuanya ke database lokal (Room)
                if (restoredList.isNotEmpty()) {
                    dao.insertAllEmissions(restoredList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}