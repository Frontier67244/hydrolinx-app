package com.mahendra.android.hydrolinx.data.local.seed

import com.mahendra.android.hydrolinx.data.local.dao.WaterLocationDao
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity

/**
 * Menyemai 20 lokasi stasiun air dummy di sekitar Blotongan, UKSW, dan pusat Salatiga.
 * Data selalu di-upsert agar seed lama (mis. Jakarta) ikut tergantikan saat app dibuka ulang.
 */
class DummyLocationSeeder(private val dao: WaterLocationDao) {

    suspend fun seedIfEmpty() {
        dao.insertAll(DUMMY_LOCATIONS)
    }

    companion object {
        private val DUMMY_LOCATIONS: List<WaterLocationEntity> = listOf(
            WaterLocationEntity(
                id = "loc-001",
                name = "Refill Station Blotongan Barat",
                latitude = -7.3236,
                longitude = 110.5011,
                address = "Jl. Blotongan Raya No. 8, Blotongan, Sidorejo, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-002",
                name = "Refill Point Blotongan Timur",
                latitude = -7.3244,
                longitude = 110.5018,
                address = "Jl. Blotongan Timur No. 3, Blotongan, Sidorejo, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-003",
                name = "Pos Air Gang Cendana",
                latitude = -7.3228,
                longitude = 110.5003,
                address = "Gang Cendana, Blotongan, Sidorejo, Salatiga",
                isOpen = true,
                hasCapacity = false,
            ),
            WaterLocationEntity(
                id = "loc-004",
                name = "Refill Station Blotongan Selatan",
                latitude = -7.3251,
                longitude = 110.4992,
                address = "Jl. Argowiyoto No. 11, Blotongan, Sidorejo, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-005",
                name = "Refill Station UKSW A",
                latitude = -7.3235,
                longitude = 110.5030,
                address = "Depan Gerbang UKSW, Jl. Diponegoro, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-006",
                name = "Refill Station UKSW Perpustakaan",
                latitude = -7.3219,
                longitude = 110.5038,
                address = "Area Perpustakaan UKSW, Jl. Kartini, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-007",
                name = "Pos Air Kampus Diponegoro",
                latitude = -7.3260,
                longitude = 110.5024,
                address = "Jl. Diponegoro No. 52, Sidorejo, Salatiga",
                isOpen = false,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-008",
                name = "Refill Point Sidorejo Satu",
                latitude = -7.3208,
                longitude = 110.5016,
                address = "Jl. Merak No. 6, Sidorejo Lor, Salatiga",
                isOpen = true,
                hasCapacity = false,
            ),
            WaterLocationEntity(
                id = "loc-009",
                name = "Pos Air Blotongan Utara",
                latitude = -7.3274,
                longitude = 110.5008,
                address = "Jl. Nakula Sadewa No. 14, Blotongan, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-010",
                name = "Refill Station Taman Tingkir",
                latitude = -7.3188,
                longitude = 110.5025,
                address = "Jl. Osamaliki No. 21, Tingkir Tengah, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-011",
                name = "Refill Station Daren View",
                latitude = -7.2960,
                longitude = 110.4921,
                address = "Area View Jembatan Tol Daren, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-012",
                name = "Pos Air Mutiara Atas",
                latitude = -7.2972,
                longitude = 110.4927,
                address = "Jl. Mutiara Atas, Daren, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-013",
                name = "Refill Point Sembrir Barat",
                latitude = -7.2950,
                longitude = 110.4904,
                address = "Jl. Sembrir Barat, Sidomukti, Salatiga",
                isOpen = true,
                hasCapacity = false,
            ),
            WaterLocationEntity(
                id = "loc-014",
                name = "Pos Air Daren Selatan",
                latitude = -7.2990,
                longitude = 110.4925,
                address = "Jl. Daren Selatan, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-015",
                name = "Refill Station Sembrir Tengah",
                latitude = -7.2947,
                longitude = 110.4946,
                address = "Jl. Sembrir Tengah, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-016",
                name = "Pos Air Jembatan Daren",
                latitude = -7.2987,
                longitude = 110.4893,
                address = "Dekat Jembatan Tol Daren, Salatiga",
                isOpen = true,
                hasCapacity = false,
            ),
            WaterLocationEntity(
                id = "loc-017",
                name = "Refill Point Mutiara Ridge",
                latitude = -7.2918,
                longitude = 110.4938,
                address = "Jl. Mutiara Ridge, Sidomukti, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-018",
                name = "Refill Station Sembrir Timur",
                latitude = -7.2970,
                longitude = 110.4975,
                address = "Jl. Sembrir Timur, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-019",
                name = "Pos Air Daren Barat",
                latitude = -7.2935,
                longitude = 110.4875,
                address = "Jl. Daren Barat, Salatiga",
                isOpen = true,
                hasCapacity = true,
            ),
            WaterLocationEntity(
                id = "loc-020",
                name = "Refill Point Jalur Tol Salatiga",
                latitude = -7.3010,
                longitude = 110.4950,
                address = "Koridor Jalur Tol Salatiga, Daren, Salatiga",
                isOpen = false,
                hasCapacity = true,
            ),
        )
    }
}
