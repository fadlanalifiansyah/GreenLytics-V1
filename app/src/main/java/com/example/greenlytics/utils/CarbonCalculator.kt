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
        MOBIL(3.579),
        MOTOR(1.802),
        BUS(0.948),
        KRL(0.345)
    }

    /**
     * Hitung emisi transportasi per kilometer.
     */
    fun calculateTransport(distanceKm: Double, type: VehicleType): Double {
        return distanceKm * type.factor
    }


    // ==========================================
    // 2. LISTRIK (GEF CM JAMALI = 0.87 kg CO2/kWh)
    // ==========================================
    private const val EF_ELECTRICITY = 0.87

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