# CODING CONVENTIONS

> Aturan ini wajib diikuti AI agent dan developer saat membangun aplikasi. Tidak ada pengecualian.

---

## 1. Penamaan File

| Jenis Komponen | Pola Nama | Contoh |
|---|---|---|
| Root package | `com.mahendra.android.hydrolink.<layer>.<modul>` | `feature.maps.ui` |
| Composable layar utama | berakhiran `Screen` | `MapsScreen.kt` |
| State data class | berakhiran `UiState` | `HydrationUiState.kt` |
| Sealed event | berakhiran `Event` | `PointsEvent.kt` |
| ViewModel | berakhiran `ViewModel` | `MapsViewModel.kt` |
| Logic / business rules | nama deskriptif, **bukan** `Utils` | `RedeemRules.kt`, `ScanAlgorithm.kt`, `HydrationRules.kt` |
| Step enum | berakhiran `Step` | `HydrationStep` |
| Room entity | berakhiran `Entity` | `WaterLocationEntity.kt` |
| Room DAO | berakhiran `Dao` | `WaterLocationDao.kt` |

### Nama File yang Dilarang

- `Utils.kt`, `Helpers.kt`, `Common.kt`, `Misc.kt`, `Manager.kt`
- Nama generik seperti `Data.kt`, `Model.kt`, `Item.kt`
- Prefix huruf I di interface (jangan `IRepository`, cukup `Repository`)

---

## 2. Struktur Folder per Fitur

Setiap fitur di dalam `feature/` wajib mengikuti pola folder berikut:

```
feature/<nama>/
├── ui/          ← composable (stateless, render dari UiState, kirim Event)
├── state/       ← UiState dan Event
├── viewmodel/   ← ViewModel yang memegang state dan handle event
└── logic/       ← aturan bisnis murni Kotlin (opsional, hanya jika kompleks)
```

### Aturan Ketat

- **Logic bisnis tidak boleh di `ui/`.** Folder `ui/` hanya untuk render dan forward event.
- **`state/` dan `logic/` tidak boleh import Compose.** Keduanya pure Kotlin.
- **`logic/` tidak boleh import Android atau Context.** Murni domain logic.
- **Tidak ada folder baru** di `feature/` untuk concern lintas-fitur. Kalau dibutuhkan, taruh di `core/`.
- **ViewModel hanya bergantung ke interface repository**, tidak ke implementasi konkretnya.

---

## 3. Pola Unidirectional Data Flow

Setiap fitur wajib mengikuti pola aliran berikut:

> User memicu aksi di Composable → Composable memanggil callback `onEvent` dengan tipe Event yang sesuai → ViewModel menerima Event dan memutuskan perubahan state → ViewModel mengupdate StateFlow → Composable membaca StateFlow yang baru dan re-compose.

Aturan turunan:

- State **hanya** berubah di dalam ViewModel
- Composable **tidak boleh** menyimpan business state lokal (yang boleh hanya state UI ephemeral seperti scroll position, focus state, atau temporary input field)
- Event **selalu** dikirim sebagai instance dari sealed type, bukan callback bebas

---

## 4. Instansiasi ViewModel

Karena tidak pakai Hilt, ViewModel dengan parameter constructor dibuat lewat **factory pattern bawaan AndroidX Lifecycle**.

Pola yang dipakai:

1. Composable mengakses kelas Application via `LocalContext.current.applicationContext`, lalu cast ke `HydrolinkApplication`
2. Composable memanggil composable function `viewModel` (dari Lifecycle Compose) dengan parameter `factory` yang dibangun lewat `viewModelFactory` builder
3. Di dalam factory, instance ViewModel dibuat dengan menyuntikkan repository dari Application

---

## 5. Akses Database — Satu Pintu

Semua akses Room **wajib** lewat interface `LocalRepository`. Aturan:

- **Dilarang** menyuntikkan `HydrolinkDatabase` langsung ke ViewModel
- **Dilarang** menyuntikkan DAO langsung ke ViewModel
- **Dilarang** mengakses `HydrolinkApplication.database` dari ViewModel manapun

Saat butuh method baru di repository:

1. Tambahkan signature method di interface `LocalRepository`
2. Implementasikan di `LocalRepositoryImpl`
3. Commit kedua file bersamaan, jangan setengah-setengah

---

## 6. Best Practices Compose

- **Stateless composable.** Terima state dan callback, jangan hold state sendiri kecuali UI ephemeral.
- **Observe StateFlow** dengan composable function `collectAsStateWithLifecycle`, **bukan** `collectAsState` biasa. Ini lebih efisien karena pause saat lifecycle inactive.
- **`rememberSaveable`** dipakai untuk state yang harus survive config change (rotasi layar, dark mode toggle), contohnya posisi kamera Maps.
- **Preview annotation** dianjurkan untuk setiap composable publik agar developer bisa cek visual tanpa run aplikasi.
- **Pecah file** kalau Composable lebih dari 300 baris. Target Composable root layar di bawah 200 baris.

---

## 7. Pusat Konstanta (Single Source of Truth)

Nilai yang muncul di banyak tempat **wajib** dipusatkan di file logic, jangan di-hardcode tersebar:

| Konstanta | Lokasi |
|---|---|
| Aturan poin dan redeem (cap, batas, rate konversi) | `feature/points/logic/RedeemRules.kt` |
| Aturan hidrasi (rentang target, ukuran minum, konversi poin) | `feature/hydration/logic/HydrationRules.kt` |
| Parameter scan radius (radius awal, step, batas max) | `feature/maps/logic/ScanAlgorithm.kt` |
| String UI | hardcode langsung di Composable (untuk lomba 8 jam tidak perlu file `strings.xml`) |

Daftar nilai konkret ada di masing-masing dokumen modul (`modules/<nama>.md`).

---

## 8. Ukuran Minum — Final dan Locked

**Hanya tiga ukuran:** 100 Ml, 250 Ml, dan 350 Ml. Tidak boleh ada ukuran lain.

Daftar ukuran ini wajib ditaruh sebagai konstanta di file logic Hydration, **bukan** di-hardcode di UI bottom sheet.

---

## 9. Copy UI — Istilah Baku

Konsistensi penulisan istilah berikut wajib di seluruh aplikasi:

| Pakai Ini | Hindari Ini |
|---|---|
| EXPLORE | SEARCH, FIND, CARI |
| Ml | ml, ML |
| poin | Point, POIN, Poin |
| token | Token, TOKEN |
| MINUM | DRINK, LOG |
| REDEEM | TUKAR, EXCHANGE |
| SCAN | SCAN QR (cukup SCAN saja) |

---

## 10. Git Hygiene

- Commit setiap 30 sampai 45 menit (setelah satu sub-task selesai)
- Format pesan commit: `[fase-N-modul] deskripsi singkat`
  - Contoh: `[p1-maps] tambah algoritma scan radius`
  - Contoh: `[p2-hydration] wire viewmodel ke repository`
- Pull rebase sebelum push
- Jangan commit file: `local.properties`, folder `.idea/`, folder `build/`, folder `.gradle/`

---

## 11. Daftar Larangan Mutlak (Hard No)

- Menambah library Dependency Injection (Hilt, Koin, Kodein, dll)
- Menambah network layer untuk business logic (Retrofit, OkHttp client)
- Membuat file dengan nama generik (`Utils.kt`, `Helpers.kt`, `Common.kt`)
- Memakai `GlobalScope` atau meluncurkan coroutine tanpa scope yang jelas
- Memakai `runBlocking` di dalam ViewModel (bisa freeze UI thread)
- Memutasi state langsung dari Composable (semua perubahan harus lewat event)
- Mengimpor satu modul fitur ke modul fitur lain
- Menulis XML layout (semua harus Compose)
- Pakai pola lama `findViewById` atau `Activity.findNavController` style
- Menambahkan `LiveData` baru (pakai `StateFlow` saja)
- Memakai `runCatching` tanpa handling — minimal log error ke Logcat

---

## 12. Testing — Skip untuk Lomba

Lomba 8 jam tidak ada anggaran waktu untuk unit test maupun UI test. Validasi dilakukan manual:

- Run di emulator setiap selesai satu fase
- Monitor Logcat saat debugging
- Pakai Preview Composable untuk cek tampilan tanpa run

Test bisa di-backfill setelah lomba kalau pengembangan dilanjutkan.

---

## 13. Header Komentar (Opsional)

Kalau ingin menambah header di awal file, pakai format singkat:

- Baris pertama: nama owner atau pemilik file (untuk lomba solo, boleh diabaikan)
- Baris kedua: deskripsi satu kalimat tujuan file
- Baris ketiga: referensi dokumen relevan (opsional)

**Jangan tulis komentar panjang di atas setiap fungsi.** Kode sebaiknya self-explanatory lewat penamaan yang baik.

---

## 14. Jika AI Agent Mulai Melanggar

Beberapa pelanggaran umum yang harus segera dihentikan:

| Gejala | Tindakan |
|---|---|
| AI mencoba menambah Hilt | Stop, rujuk aturan poin 11. Suruh pakai manual injection |
| AI membuat file `Utils.kt` | Stop, minta nama deskriptif sesuai isinya |
| AI menaruh business logic di Composable | Stop, minta refactor ke ViewModel |
| AI generate XML layout | Stop, minta versi Compose |
| AI mengimpor satu modul fitur ke fitur lain | Stop, minta refactor lewat NotificationBus atau repository |
| AI menambah dependency baru tanpa diminta | Stop, tanya alasannya. Tolak kalau tidak kritikal |

Semua aturan di atas non-negotiable untuk menjaga arsitektur tetap bersih dan konsisten.
