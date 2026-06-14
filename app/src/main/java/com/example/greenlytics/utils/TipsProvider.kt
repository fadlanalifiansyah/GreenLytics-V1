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
        TipsModel(10, "Sampah", "CAMPURAN", "Mulailah Memilah Sampah", "Sampah tercampur sulit diproses. Pisahkan minimal dua wadah.", "SIPSN", "Sedang"),

        // BELANJA (Tambahan Baru)
        TipsModel(11, "Belanja", "FASHION", "Kurangi Fast Fashion", "Industri pakaian menyumbang emisi besar. Coba thrifting atau beli baju berkualitas yang awet.", "UNEP", "Sedang"),
        TipsModel(12, "Belanja", "MAKANAN_MINUMAN", "Pilih Produk Lokal", "Makanan impor punya jejak karbon transportasi yang tinggi. Beli di pasar lokal!", "FAO", "Mudah"),
        TipsModel(13, "Belanja", "ELEKTRONIK", "Cek Label Hemat Energi", "Pilih perangkat elektronik dengan rating hemat energi untuk menekan emisi jangka panjang.", "ESDM", "Sedang"),
        TipsModel(14, "Belanja", "General", "Bawa Tas Belanja Kain", "Kurangi penggunaan kantong plastik sekali pakai dengan selalu membawa tas kain.", "KLHK", "Mudah")
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

        // Variabel tambahan untuk Belanja
        val emisiFashion = emissionsToday.filter { it.subKategori.equals("FASHION", true) }.sumOf { it.emisiKarbon }
        val emisiMakanan = emissionsToday.filter { it.subKategori.equals("MAKANAN_MINUMAN", true) }.sumOf { it.emisiKarbon }
        val emisiElektronik = emissionsToday.filter { it.subKategori.equals("ELEKTRONIK", true) }.sumOf { it.emisiKarbon }
        val totalBelanjaLainnya = emissionsToday.filter {
            it.kategori.equals("Belanja", true) &&
                    !it.subKategori.equals("FASHION", true) &&
                    !it.subKategori.equals("MAKANAN_MINUMAN", true) &&
                    !it.subKategori.equals("ELEKTRONIK", true)
        }.sumOf { it.emisiKarbon }

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

        // Belanja > 5.0 kg (Tambahan Baru)
        if (emisiFashion > 5.0) allTips.find { it.subKategori == "FASHION" }?.let { resultTips.add(it) }
        if (emisiMakanan > 5.0) allTips.find { it.subKategori == "MAKANAN_MINUMAN" }?.let { resultTips.add(it) }
        if (emisiElektronik > 5.0) allTips.find { it.subKategori == "ELEKTRONIK" }?.let { resultTips.add(it) }
        if (totalBelanjaLainnya > 5.0) allTips.find { it.kategori == "Belanja" && it.subKategori == "General" }?.let { resultTips.add(it) }

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