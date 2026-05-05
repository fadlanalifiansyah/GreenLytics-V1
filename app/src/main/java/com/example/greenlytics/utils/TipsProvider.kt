package com.example.greenlytics.utils

import com.example.greenlytics.data.local.EmissionEntity
import com.example.greenlytics.data.model.TipsModel
import java.util.Calendar

object TipsProvider {

    val allTips = listOf(
        // LISTRIK
        TipsModel(1, "Listrik", "General", "Cabut Colokan Listrik", "Vampir listrik tetap menyedot energi meski alat mati. Yuk cabut charger HP!", "Energy.gov", "Mudah"),
        TipsModel(2, "Listrik", "General", "Atur Suhu AC 24-26°C", "Mengatur suhu AC lebih sejuk sedikit bisa menghemat penggunaan kompresor.", "IESR", "Mudah"),
        TipsModel(3, "Listrik", "General", "Cuci & Setrika Sekaligus", "Tarikan listrik awal setrika sangat besar. Kerjakan sekaligus!", "ESDM", "Sedang"),

        // TRANSPORTASI
        TipsModel(4, "Transportasi", "BUS", "Naik Transjakarta", "Satu bus mengurangi belasan motor di jalan.", "ITDP", "Sedang"),
        TipsModel(5, "Transportasi", "KRL", "Prioritaskan Commuter Line", "Kereta berbasis rel adalah moda transportasi paling rendah emisi.", "BRIN", "Mudah"),
        TipsModel(6, "Transportasi", "MOBIL", "Coba Carpooling Yuk!", "Ajak teman searah pergi bareng agar emisi dibagi berempat.", "Kemenhub", "Sedang"),
        TipsModel(7, "Transportasi", "MOTOR", "Servis Rutin Berkala", "Mesin terawat pastikan pembakaran sempurna dan emisi rendah.", "KLHK", "Sedang"),

        // SAMPAH
        TipsModel(8, "Sampah", "ORGANIK", "Mulai Membuat Kompos", "Sampah makanan di TPA hasilkan gas metana berbahaya.", "KLHK", "Sulit"),
        TipsModel(9, "Sampah", "ANORGANIK", "Setor ke Bank Sampah", "Plastik butuh ratusan tahun terurai. Jangan dibakar!", "SWI", "Sedang"),
        TipsModel(10, "Sampah", "CAMPURAN", "Mulailah Memilah Sampah", "Sampah tercampur sulit diproses. Pisahkan minimal dua wadah.", "SIPSN", "Sedang")
    )

    fun getRelevantTips(emissionsToday: List<EmissionEntity>): List<TipsModel> {
        val resultTips = mutableListOf<TipsModel>()

        // 1. HITUNG EMISI MASING-MASING SUB-KATEGORI HARI INI
        val emisiMotor = emissionsToday.filter { it.subKategori.equals("Motor", true) }.sumOf { it.emisiKarbon }
        val emisiMobil = emissionsToday.filter { it.subKategori.equals("Mobil", true) }.sumOf { it.emisiKarbon }
        val emisiBus = emissionsToday.filter { it.subKategori.equals("Bus Transjakarta", true) }.sumOf { it.emisiKarbon }
        val emisiKRL = emissionsToday.filter { it.subKategori.equals("KRL", true) }.sumOf { it.emisiKarbon }

        val emisiOrganik = emissionsToday.filter { it.subKategori.equals("Organik", true) }.sumOf { it.emisiKarbon }
        val emisiAnorganik = emissionsToday.filter { it.subKategori.equals("Anorganik", true) }.sumOf { it.emisiKarbon }
        val emisiCampuran = emissionsToday.filter { it.subKategori.equals("Campuran", true) }.sumOf { it.emisiKarbon }

        val totalListrik = emissionsToday.filter { it.kategori.equals("Listrik", true) }.sumOf { it.emisiKarbon }

        // 2. EVALUASI SATU PER SATU (Bisa muncul banyak di layar)

        // Transportasi > 5.0 kg
        if (emisiMotor > 5.0) allTips.find { it.subKategori == "MOTOR" }?.let { resultTips.add(it) }
        if (emisiMobil > 5.0) allTips.find { it.subKategori == "MOBIL" }?.let { resultTips.add(it) }
        if (emisiBus > 5.0) allTips.find { it.subKategori == "BUS" }?.let { resultTips.add(it) }
        if (emisiKRL > 5.0) allTips.find { it.subKategori == "KRL" }?.let { resultTips.add(it) }

        // Sampah > 5.0 kg
        if (emisiOrganik > 5.0) allTips.find { it.subKategori == "ORGANIK" }?.let { resultTips.add(it) }
        if (emisiAnorganik > 5.0) allTips.find { it.subKategori == "ANORGANIK" }?.let { resultTips.add(it) }
        if (emisiCampuran > 5.0) allTips.find { it.subKategori == "CAMPURAN" }?.let { resultTips.add(it) }

        // Listrik > 5.0 kg
        if (totalListrik > 5.0) {
            val tipsListrik = allTips.filter { it.kategori == "Listrik" }
            // Menggunakan hari dalam setahun agar tipsnya tidak acak/berubah-ubah tiap kali kamu input data baru
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val tipIndex = dayOfYear % tipsListrik.size
            resultTips.add(tipsListrik[tipIndex])
        }

        return resultTips.distinctBy { it.id }.map { it.copy() }
    }
}