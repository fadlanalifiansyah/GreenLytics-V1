package com.example.greenlytics.utils

/**
 * Mesin Utama Perhitungan Emisi Karbon GreenLytics.
 * Berdasarkan dokumen proposal (ESDM, IPCC, USEEIO, NAICS).
 * Satuan hasil akhir adalah kg CO2e (Kilogram Karbon Dioksida Ekuivalen).
 */
object CarbonCalculator {

    // ==========================================
    // 1. TRANSPORTASI
    // ==========================================
    enum class VehicleType(val factor: Double) {
        MOBIL(0.229),
        MOTOR(0.038),
        BUS(0.527), // Ingat: Bus harus dibagi jumlah penumpang
        KRL(0.0152) // KRL sudah per penumpang
    }

    /**
     * Hitung emisi transportasi per kilometer.
     * Khusus untuk Bus, masukkan jumlah penumpang agar emisi dibagi rata.
     */
    fun calculateTransport(distanceKm: Double, type: VehicleType, passengerCount: Int = 1): Double {
        return if (type == VehicleType.BUS) {
            // Bus: Jarak * (0.527 / jumlah penumpang)
            distanceKm * (type.factor / passengerCount.coerceAtLeast(1))
        } else {
            distanceKm * type.factor
        }
    }


    // ==========================================
    // 2. LISTRIK (GEF OM JAMALI = 0.80 kg CO2/kWh)
    // ==========================================
    private const val EF_ELECTRICITY = 0.80

    enum class ElectricTariff(val ratePerKwh: Double, val label: String) {
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
    // Asumsi kurs USD ke IDR tahun 2024 (Bisa disesuaikan jika perlu)
    private const val KURS_USD_TO_IDR = 15500.0

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
    // 4. SAMPAH (IPCC First Order Decay)
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