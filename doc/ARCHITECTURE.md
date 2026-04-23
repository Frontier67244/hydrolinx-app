# ARCHITECTURE

## Stack Teknologi

| Layer | Teknologi | Catatan |
|---|---|---|
| UI | Jetpack Compose | Tidak ada XML layout |
| State | StateFlow + ViewModel | Dari library lifecycle Android |
| Navigation | Navigation Compose | Route didefinisikan via sealed class |
| Database lokal | Room | Pakai KSP compiler. Offline-only |
| Maps | Google Maps Compose | Plus Play Services Location |
| Coroutines | Coroutines Kotlin | Pakai viewModelScope |
| Dependency Injection | Manual | Tidak pakai Hilt, Koin, atau library DI |
| Bahasa | Kotlin | 100% Kotlin |
| Min SDK | 26 | |
| Target SDK | 36 | |

---

## Struktur Folder Wajib

```
app/src/main/java/com/mahendra/android/hydrolink/
├── HydrolinkApplication.kt        ← kelas Application, inisialisasi DB + Repository
├── MainActivity.kt                ← single activity, host Navigation
│
├── core/                          ← infrastruktur lintas-fitur (tidak punya layar sendiri)
│   ├── navigation/
│   │   ├── AppRoute.kt
│   │   └── HydrolinkNavHost.kt
│   ├── ui/
│   │   ├── components/            ← composable shared antar fitur
│   │   │   └── ModulePlaceholderScreen.kt
│   │   └── theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── guidance/
│   │   └── GuidanceMessage.kt
│   └── notification/
│       ├── NotificationEvent.kt
│       └── NotificationBus.kt
│
├── data/                          ← layer persistence
│   ├── local/
│   │   ├── dao/                   ← akses Room
│   │   │   ├── UserProfileDao.kt
│   │   │   ├── WaterLocationDao.kt
│   │   │   ├── HydrationSessionDao.kt
│   │   │   └── PointWalletDao.kt
│   │   ├── db/
│   │   │   └── HydrolinkDatabase.kt
│   │   ├── entity/                ← representasi tabel
│   │   │   ├── UserProfileEntity.kt
│   │   │   ├── WaterLocationEntity.kt
│   │   │   ├── HydrationSessionEntity.kt
│   │   │   └── PointWalletEntity.kt
│   │   └── seed/
│   │       └── DummyLocationSeeder.kt
│   └── repository/
│       ├── LocalRepository.kt     ← interface, satu pintu ke data
│       └── LocalRepositoryImpl.kt
│
└── feature/                       ← fitur dengan layar sendiri
    ├── onboarding/
    │   └── ui/OnboardingScreen.kt
    ├── maps/
    │   ├── ui/
    │   │   ├── MapsScreen.kt
    │   │   ├── MapsTopBar.kt
    │   │   ├── MapsActionBar.kt
    │   │   ├── PinPopup.kt
    │   │   ├── LocationDetailSheet.kt
    │   │   └── NotificationShiftingPanel.kt
    │   ├── state/
    │   │   ├── MapsUiState.kt
    │   │   └── MapsEvent.kt
    │   ├── viewmodel/
    │   │   └── MapsViewModel.kt
    │   └── logic/
    │       └── ScanAlgorithm.kt
    ├── hydration/
    │   ├── ui/HydrationScreen.kt
    │   ├── state/
    │   │   ├── HydrationUiState.kt
    │   │   └── HydrationEvent.kt
    │   ├── viewmodel/HydrationViewModel.kt
    │   └── logic/HydrationRules.kt
    └── points/
        ├── ui/PointsScreen.kt
        ├── state/
        │   ├── PointsUiState.kt
        │   └── PointsEvent.kt
        ├── viewmodel/PointsViewModel.kt
        └── logic/RedeemRules.kt
```

---

## Pola Arsitektur: MVVM + Unidirectional Data Flow

Aplikasi mengikuti pola **Model-View-ViewModel** dengan aliran data **satu arah**.

### Diagram Aliran

```
┌─────────────────────────────────────────────────────┐
│ UI (Composable)                                     │
│   - Stateless                                       │
│   - Mengamati UiState (read-only)                   │
│   - Meneruskan aksi user via callback Event         │
└────────────┬───────────────────────┬────────────────┘
             │ observe state         │ kirim event
             ▼                       │
┌─────────────────────────────────────────────────────┐
│ ViewModel                                           │
│   - Memegang state aplikasi (mutable di dalam)      │
│   - Memproses event jadi perubahan state            │
│   - Memanggil repository untuk akses data           │
└────────────┬────────────────────────────────────────┘
             │ panggil suspend function
             ▼
┌─────────────────────────────────────────────────────┐
│ LocalRepository (interface)                         │
│   - Satu pintu menuju Room                          │
│   - Mengembalikan Flow atau hasil suspend           │
└────────────┬────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────┐
│ Room Database (DAO + Entity + Database class)       │
└─────────────────────────────────────────────────────┘
```

### Folder Logic (Sampingan)

Folder `logic/` di tiap fitur berisi aturan bisnis murni Kotlin (tanpa dependensi Android atau Compose). Dipanggil dari ViewModel. Contoh: aturan redeem, aturan hidrasi, algoritma scan radius.

---

## Aturan Arah Dependency

Modul satu hanya boleh bergantung ke modul lain mengikuti arah yang ditentukan di bawah ini. Pelanggaran akan membuat kode sulit di-test dan rentan circular dependency.

| Komponen | Boleh bergantung ke |
|---|---|
| `feature/<x>/ui/` | `feature/<x>/state/` dan `feature/<x>/viewmodel/` |
| `feature/<x>/viewmodel/` | `feature/<x>/logic/`, `data/repository/LocalRepository` (interface), `core/notification/NotificationBus` |
| `feature/<x>/logic/` | hanya `data/local/entity/` dan stdlib Kotlin |
| `feature/<x>/state/` | hanya `data/local/entity/` dan stdlib Kotlin |
| `core/*` | dipakai semua fitur, tidak bergantung ke fitur manapun |
| `data/repository/` | `data/local/*` |
| `data/local/*` | tidak bergantung ke layer lain |

### Larangan Silang (Hard Rules)

- Modul fitur **tidak boleh** mengimpor modul fitur lain. Komunikasi antar fitur via `NotificationBus` di `core/notification/` atau via shared state di database.
- Folder `ui/` **tidak boleh** mengimpor `data/local/` langsung. Selalu lewat ViewModel.
- Folder `logic/` dan `state/` **tidak boleh** mengimpor apapun dari Compose atau Android framework.

---

## Komunikasi Antar Fitur

Tiga jalur yang diperbolehkan:

1. **Database (lewat repository).** Untuk state yang harus disimpan. Contoh: setelah user log minum di Hydration, wallet poin di-update via repository. Modul Points membaca wallet via Flow yang sama, sehingga otomatis terupdate.
2. **NotificationBus (event ephemeral).** Untuk feedback sesaat yang tidak perlu disimpan. Contoh: setelah log minum, emit event "+10 poin" yang ditangkap oleh layar manapun yang sedang aktif untuk menampilkan panel notif.
3. **Navigation.** Untuk perpindahan layar oleh user (tap tab, deep link).

---

## Manual Dependency Injection

Karena tidak pakai Hilt, instansiasi dependency dilakukan manual.

**Pola yang dipakai:**

- Kelas `HydrolinkApplication` adalah satu-satunya tempat membuat instance Database dan Repository
- Kelas Application meng-expose dua property publik (database dan repository) sehingga bisa diakses dari composable manapun
- ViewModel diinstansiasi dengan factory pattern (pakai `viewModelFactory` builder bawaan AndroidX Lifecycle), bukan Hilt
- Composable mengakses Application instance via `LocalContext.current.applicationContext`, lalu cast ke `HydrolinkApplication`

**Aturan:**

- Hanya kelas Application yang punya referensi ke Database
- Repository menerima DAO via constructor (manual wiring di Application)
- ViewModel menerima repository via constructor (di-pass via factory)

---

## Bagaimana Modul Komunikasi via Bus Notifikasi

`NotificationBus` adalah singleton object di `core/notification/`. Ia menyimpan satu SharedFlow yang bisa dipancarkan dan diobservasi dari manapun.

**Alur tipikal:**

1. ViewModel modul Hydration memproses event `ConfirmDrink` dari user
2. Setelah berhasil simpan sesi ke database, ViewModel memanggil fungsi emit di `NotificationBus` dengan event `HydrationLogged`
3. Layar manapun yang sedang aktif (yang sudah subscribe ke bus dalam `LaunchedEffect`) menerima event tersebut
4. Layar tampilkan panel overlay dengan pesan sesuai jenis event

**Tipe event yang ada:**

| Tipe Event | Dipicu Saat | Data yang Dibawa |
|---|---|---|
| HydrationLogged | User berhasil log sesi minum | jumlah poin, timestamp |
| RedeemSuccess | User berhasil tukar poin jadi token | jumlah token yang didapat |
| ScanSuccess | User berhasil scan token di stasiun | volume air yang dipilih |
| MapsFound | Sistem berhasil menemukan stasiun saat explore | nama stasiun |
| GenericInfo | Pesan generik fallback | teks pesan bebas |

---

## Empat Tabel Database

Skema lengkap setiap tabel ada di `DATA_MODEL.md`. Ringkasannya:

| Tabel | Isi | Pola Penggunaan |
|---|---|---|
| `water_locations` | Daftar stasiun air komunitas | Read-only, di-seed sekali saat pertama kali jalan dari `DummyLocationSeeder` |
| `hydration_sessions` | Log setiap sesi minum | Append-only. Tidak ada update atau delete |
| `point_wallet` | Wallet poin user (single row, id selalu 0) | Selalu upsert. Hanya satu baris di tabel ini |
| `user_profiles` | Profil user (single row default) | Untuk menyimpan target hidrasi harian. Hanya satu baris |

---

## Urutan Pembuatan File Saat Live Coding

Urutan rekomendasi (detail waktu di `EXECUTION_PLAN.md`):

1. Konfigurasi Gradle, Manifest, dan dependency dasar (foundation)
2. Empat entity, empat DAO, satu Database, satu Seeder dummy
3. Interface dan implementasi Repository
4. Kelas Application dan MainActivity
5. AppRoute, NavHost, dan placeholder tiga fitur
6. Theme dan NotificationBus di core
7. Modul Maps (state → logic → viewmodel → screen)
8. Modul Hydration (state → logic → viewmodel → screen)
9. Modul Points (state → logic → viewmodel → screen)
10. Onboarding stub, guidance panel, polish demo

---

## Decision Log

| Keputusan | Alasan |
|---|---|
| Tidak pakai Hilt atau Koin | Setup hemat waktu, app kecil, manual injection cukup |
| Tidak pakai XML | Konsisten Compose-only, lebih cepat eksekusi |
| Single activity | Navigation Compose menangani semua route, tidak perlu multi-activity |
| Offline-only | Tidak ada dependency backend yang bisa fail saat demo |
| Folder `logic/` terpisah dari `viewmodel/` | Logic murni Kotlin testable, reusable, batas yang jelas dengan layer Android |
| Bottom navigation tiga tab (Maps, Hydration, Points) | Onboarding punya route sendiri di luar bottom nav |
| SharedFlow untuk event bus | Lebih sederhana daripada library event bus pihak ketiga |
| Single row pattern untuk wallet dan user profile | Aplikasi single-user, tidak perlu multi-akun |
