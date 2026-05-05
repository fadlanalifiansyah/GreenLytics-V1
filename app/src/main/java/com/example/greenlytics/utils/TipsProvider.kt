package com.example.greenlytics.utils

import com.example.greenlytics.data.local.EmissionEntity
import com.example.greenlytics.data.model.TipsModel

object TipsProvider {

    // Daftar semua tips berdasarkan riset
    val allTips = listOf(
        // KATEGORI LISTRIK
        TipsModel(1, "Listrik", "General", "Cabut Colokan Listrik yang Tidak Dipakai", "Vampir listrik tetap menyedot energi meski alat mati. Yuk cabut charger HP atau TV jika selesai!", "Link", "Mudah"),
        TipsModel(2, "Listrik", "General", "Atur Suhu AC 24-26°C", "Mengatur suhu AC lebih sejuk sedikit bisa menghemat penggunaan kompresor secara signifikan.", "Link", "Mudah"),

        // KATEGORI TRANSPORTASI (Threshold > 5.0 kg)
        TipsModel(4, "Transportasi", "BUS", "Lebih Hemat dengan Bus Transjakarta", "Satu bus mengurangi belasan motor di jalan. Pastikan gunakan rute integrasi agar maksimal.", "ITDP Indonesia", "Sedang"),
        TipsModel(5, "Transportasi", "KRL", "Kereta: Pilihan Rendah Emisi", "Commuter Line hanya menghasilkan ±34,03 gram CO₂ per penumpang-km.", "KAI Commuter - BRIN", "Mudah"),
        TipsModel(6, "Transportasi", "MOBIL", "Coba Carpooling Yuk!", "Ajak teman searah pergi bareng agar emisi satu mobil dibagi berempat.", "Kemenhub RI", "Sedang"),
        TipsModel(7, "Transportasi", "MOTOR", "Servis Rutin Motormu", "Mesin yang terawat memastikan pembakaran bensin lebih sempurna dan gas buang lebih rendah.", "KLHK", "Sedang"),

        // KATEGORI SAMPAH (Threshold > 2.0 kg)[cite: 1]
        TipsModel(8, "Sampah", "ORGANIK", "Olah Sampah Organik Jadi Kompos", "Sampah makanan di TPA menghasilkan gas metana yang 28x lebih berbahaya dari CO2.", "SIPSN - KLHK", "Sulit"),
        TipsModel(9, "Sampah", "ANORGANIK", "Setor ke Bank Sampah", "Plastik butuh ratusan tahun untuk terurai. Jangan dibakar! Serahkan ke bank sampah.", "SWI", "Sedang")
    )

    fun getRelevantTips(emissions: List<EmissionEntity>): List<TipsModel> {
        val resultTips = mutableListOf<TipsModel>()

        // Hitung total emisi riil dari database[cite: 1]
        val transportTotal = emissions.filter { it.kategori == "Transportasi" }.sumOf { it.emisiKarbon }
        val wasteTotal = emissions.filter { it.kategori == "Sampah" }.sumOf { it.emisiKarbon }

        // Identifikasi sub-kategori dominan[cite: 1]
        val dominantVehicle = emissions.filter { it.kategori == "Transportasi" }
            .maxByOrNull { it.emisiKarbon }?.subKategori ?: ""

        val dominantWaste = emissions.filter { it.kategori == "Sampah" }
            .maxByOrNull { it.emisiKarbon }?.subKategori ?: ""

        // Filter berdasarkan ambang batas riset[cite: 1]
        if (transportTotal > 5.0) {
            allTips.find { it.kategori == "Transportasi" && it.subKategori.equals(dominantVehicle, true) }?.let { resultTips.add(it) }
        }

        if (wasteTotal > 2.0) {
            allTips.find { it.kategori == "Sampah" && it.subKategori.equals(dominantWaste, true) }?.let { resultTips.add(it) }
        }

        // Selalu sertakan tips Listrik secara acak sebagai edukasi harian[cite: 1]
        val electricityTips = allTips.filter { it.kategori == "Listrik" }
        if (electricityTips.isNotEmpty()) resultTips.add(electricityTips.random())

        // Default jika database kosong
        if (resultTips.isEmpty()) resultTips.addAll(allTips.filter { it.kategori == "Listrik" }.take(2))

        return resultTips.distinctBy { it.id }
    }
}