package com.example.greenlytics.utils

/**
 * Mesin Utama Perhitungan Emisi Karbon GreenLytics.
 * Satuan hasil akhir adalah kg CO2e (Kilogram Karbon Dioksida Ekuivalen).
 */
object CarbonCalculator {

    // ==========================================
    // 1. TRANSPORTASI
    // ==========================================
    enum class VehicleType(val factor: Double) {
        MOBIL(0.170), // Which form of transport has the smallest carbon footprint? (2023) https://ourworldindata.org/travel-carbon-footprint
        MOTOR(0.114), // Which form of transport has the smallest carbon footprint? (2023) https://ourworldindata.org/travel-carbon-footprint
        BUS(0.097), // Which form of transport has the smallest carbon footprint? (2023) https://ourworldindata.org/travel-carbon-footprint
        KRL(0.034) // RISET BRIN: KAI COMMUTER LINE https://kci.id/informasi-publik/berita/riset-brin-commuter-line-ramah-lingkungan-jejak-karbon-lebih-rendah-dukung-pelestarian-lingkungan-kai-commuter-tegaskan-komitmen-hadirkan-transportasi-yang-ramah-lingkungan
    }

    /**
     * Hitung emisi transportasi per kilometer.
     */
    fun calculateTransport(distanceKm: Double, type: VehicleType): Double {
        return distanceKm * type.factor
    }


    // ==========================================
    // 2. LISTRIK (GEF CM JAMALI = 0.87 kg CO2/kWh) https://gatrik.esdm.go.id/assets/uploads/download_index/files/96d7c-nilai-fe-grk-sistem-ketenagalistrikan-tahun-2019.pdf
    // ==========================================
    private const val EF_ELECTRICITY = 0.87

    enum class ElectricTariff(val ratePerKwh: Double, val label: String) { //Kategori harga mengambil dari data https://web.pln.co.id/statics/uploads/2026/01/202601-Tarif-Listrik.jpeg
        R1_900VA(1352.00, "R-1 (900 VA)"),
        R1_1300_2200VA(1444.70, "R-1 (1300-2200 VA)"),
        R2_3500_5500VA(1699.53, "R-2 (3500-5500 VA)")
    }

    /**
     * Hitung listrik jika user menginput pemakaian dalam satuan kWh.
     * @param isMonthlyTagihan Set true jika angka yang dimasukkan adalah total 1 bulan.
     */
    fun calculateElectricityFromKwh(kwh: Double, isMonthlyTagihan: Boolean = false): Double {
        val totalEmisi = kwh * EF_ELECTRICITY
        return if (isMonthlyTagihan) totalEmisi / 30.0 else totalEmisi
    }

    /**
     * Hitung listrik jika user menginput nominal uang tagihan (Rupiah).
     * @param isMonthlyTagihan Set true jika angka yang dimasukkan adalah tagihan 1 bulan.
     */
    fun calculateElectricityFromRupiah(billRp: Double, tariff: ElectricTariff, isMonthlyTagihan: Boolean = true): Double {
        // Listrik: Tagihan(Rp) / Tarif(Rp/kWh)
        val kwh = billRp / tariff.ratePerKwh
        val totalEmisi = kwh * EF_ELECTRICITY
        return if (isMonthlyTagihan) totalEmisi / 30.0 else totalEmisi
    }

    // ==========================================
    // 3. BELANJA (Spend-Based Method USEEIO)
    // ==========================================
    // kurs USD ke IDR 17.779,30
    private const val KURS_USD_TO_IDR = 17779.3

    enum class ShoppingCategory(val factorUsd: Double) {
        MAKANAN_MINUMAN(0.755),
        FASHION(0.155),
        ELEKTRONIK(0.078),
        PERABOT(0.145),
        KECANTIKAN(0.130),
        HIBURAN(0.114)
    }

    /**
     * Hitung emisi dari pengeluaran belanja (Rupiah).
     * Otomatis dikonversi ke USD di dalam fungsi.
     */
    fun calculateShopping(spendRp: Double, category: ShoppingCategory): Double {
        val spendUsd = spendRp / KURS_USD_TO_IDR
        return spendUsd * category.factorUsd
    }

    // ==========================================
    // 4. SAMPAH (IPCC First Order Decay) https://www.ipcc-nggip.iges.or.jp/public/2006gl/pdf/5_Volume5/IPCC_Waste_Model.xls
    // ==========================================
    enum class WasteType(val factor: Double) {
        ORGANIK(0.887),
        KERTAS(2.366),
        CAMPURAN(0.887)
    }

    fun calculateWaste(weightKg: Double, type: WasteType): Double {
        return weightKg * type.factor
    }
}