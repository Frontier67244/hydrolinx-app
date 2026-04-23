package com.mahendra.android.hydrolinx.data.local.seed

import com.mahendra.android.hydrolinx.data.local.dao.WaterLocationDao
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity

/**
 * Menyemai 20 lokasi stasiun air dummy di sekitar Jakarta (-6.20, 106.81).
 * Hanya dijalankan saat tabel water_locations masih kosong.
 */
class DummyLocationSeeder(private val dao: WaterLocationDao) {

    suspend fun seedIfEmpty() {
        if (dao.count() > 0) return
        dao.insertAll(DUMMY_LOCATIONS)
    }

    companion object {
        private val DUMMY_LOCATIONS: List<WaterLocationEntity> = listOf(
            WaterLocationEntity("loc-001", "Pos Air Cikini", -6.1953, 106.8411, "Jl. Cikini Raya No. 12, Menteng", true, true),
            WaterLocationEntity("loc-002", "Stasiun Air Salemba", -6.1980, 106.8481, "Jl. Salemba Raya No. 23, Jakarta Pusat", true, true),
            WaterLocationEntity("loc-003", "Pos Air Tebet", -6.2276, 106.8494, "Jl. Tebet Barat Dalam Raya No. 7", true, false),
            WaterLocationEntity("loc-004", "Pos Air Menteng", -6.1960, 106.8320, "Jl. HOS Cokroaminoto No. 45, Menteng", false, true),
            WaterLocationEntity("loc-005", "Stasiun Air Senayan", -6.2250, 106.8020, "Jl. Gerbang Pemuda, Senayan", true, true),
            WaterLocationEntity("loc-006", "Pos Air Sudirman", -6.2093, 106.8213, "Jl. Jend. Sudirman Kav. 52", true, true),
            WaterLocationEntity("loc-007", "Pos Air Thamrin", -6.1926, 106.8229, "Jl. MH Thamrin No. 10", true, true),
            WaterLocationEntity("loc-008", "Stasiun Air Kuningan", -6.2340, 106.8294, "Jl. HR Rasuna Said Blok X-2", false, false),
            WaterLocationEntity("loc-009", "Pos Air Matraman", -6.2004, 106.8619, "Jl. Matraman Raya No. 30", true, true),
            WaterLocationEntity("loc-010", "Pos Air Cawang", -6.2451, 106.8705, "Jl. Dewi Sartika No. 15, Cawang", true, true),
            WaterLocationEntity("loc-011", "Stasiun Air Mampang", -6.2462, 106.8255, "Jl. Mampang Prapatan No. 22", true, false),
            WaterLocationEntity("loc-012", "Pos Air Gatot Subroto", -6.2307, 106.8210, "Jl. Gatot Subroto Kav. 34", true, true),
            WaterLocationEntity("loc-013", "Pos Air Kebayoran", -6.2430, 106.7955, "Jl. Sisingamangaraja No. 8", true, true),
            WaterLocationEntity("loc-014", "Stasiun Air Blok M", -6.2446, 106.7986, "Jl. Melawai Raya No. 54", false, true),
            WaterLocationEntity("loc-015", "Pos Air Pasar Minggu", -6.2805, 106.8442, "Jl. Raya Pasar Minggu No. 66", true, true),
            WaterLocationEntity("loc-016", "Pos Air Kemang", -6.2622, 106.8130, "Jl. Kemang Raya No. 18", true, true),
            WaterLocationEntity("loc-017", "Stasiun Air Manggarai", -6.2093, 106.8500, "Jl. Minangkabau No. 5", true, true),
            WaterLocationEntity("loc-018", "Pos Air Kampung Melayu", -6.2236, 106.8668, "Jl. Jatinegara Barat No. 12", true, false),
            WaterLocationEntity("loc-019", "Pos Air Palmerah", -6.2070, 106.7902, "Jl. Palmerah Barat No. 45", true, true),
            WaterLocationEntity("loc-020", "Stasiun Air Tanah Abang", -6.1866, 106.8108, "Jl. Kebon Jati No. 27", false, true),
        )
    }
}
