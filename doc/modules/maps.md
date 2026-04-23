# Module: Maps (P1)

> Prioritas tertinggi. Target waktu: 2 jam. Modul paling kompleks.

## Tujuan Fitur

User menemukan stasiun air komunitas terdekat dari posisinya. Alur intinya:

> Home → tap EXPLORE → radius mengembang → pin muncul → tap pin → detail sheet → route ke aplikasi maps eksternal

---

## Struktur File

```
feature/maps/
├── ui/
│   ├── MapsScreen.kt                  ← root composable, di bawah 200 baris
│   ├── MapsTopBar.kt                  ← header dengan label mode (Home / Explore)
│   ├── MapsActionBar.kt               ← tombol EXPLORE besar dan toggle moda transportasi
│   ├── PinPopup.kt                    ← popup di atas pin saat selected
│   ├── LocationDetailSheet.kt         ← bottom sheet dengan alamat dan tombol route
│   └── NotificationShiftingPanel.kt   ← overlay panel notifikasi
├── state/
│   ├── MapsUiState.kt
│   └── MapsEvent.kt
├── viewmodel/
│   └── MapsViewModel.kt
└── logic/
    └── ScanAlgorithm.kt               ← murni Kotlin, tanpa dependensi Android
```

---

## State Maps

Field yang harus ada di `MapsUiState`:

| Nama Field | Tipe Konseptual | Default | Makna |
|---|---|---|---|
| viewMode | enum (Home, Explore) | Home | Mode tampilan saat ini |
| hasLocationPermission | boolean | false | Apakah user sudah grant izin lokasi |
| userLocation | koordinat (lat, lng) | default Jakarta (-6.20, 106.81) | Posisi user saat ini |
| isScanning | boolean | false | Apakah sedang aktif scanning radius |
| searchRadiusMeters | bilangan bulat | 0 | Radius lingkaran scan saat ini (untuk animasi) |
| lastMaxRadiusMeters | bilangan bulat | 0 | Batas maksimum radius pada attempt terakhir |
| searchAttempt | bilangan bulat | 0 | Nomor attempt (1, 2, 3, ...) |
| foundLocations | list stasiun | kosong | Stasiun yang ketemu pada attempt terakhir |
| excludedLocationIds | set string | kosong | Id stasiun yang di-exclude (dari retry) |
| showNoResultHint | boolean | false | Apakah tampilkan hint "tidak ada hasil" |
| noResultDetail | string atau null | null | Pesan detail untuk no-result |
| selectedLocation | stasiun atau null | null | Stasiun yang sedang aktif untuk detail |
| highlightedLocationId | string atau null | null | Id stasiun yang sedang di-highlight di peta |
| isDetailSheetOpen | boolean | false | Apakah detail sheet sedang terbuka |
| selectedTransportMode | enum (Walk, Motor, Car) | Walk | Moda transportasi terpilih |
| isNotifyOpen | boolean | false | Apakah notification panel terbuka |
| isRecentering | boolean | false | Apakah sedang recenter ke posisi user |

**Konstanta default user location:** koordinat sekitar Jakarta pusat. Disimpan sebagai constant di companion object dari UiState.

---

## Event Maps

Event yang harus ada di sealed interface `MapsEvent`:

| Nama Event | Trigger | Efek di ViewModel |
|---|---|---|
| PermissionResult(granted) | Hasil permission launcher | Update flag hasLocationPermission |
| UserLocationUpdated(koordinat) | Last known location dari Fused Provider | Update userLocation |
| RecenterRequested | User tap tombol recenter | Set isRecentering true sebentar |
| TapExplore | User tap tombol EXPLORE | Mulai algoritma scan |
| BackToHome | User tap back dari mode Explore | Reset state ke Home |
| SelectLocation(id) | User tap pin di peta | Buka detail untuk stasiun tersebut |
| DismissDetail | User close detail sheet | Tutup detail sheet, clear selected |
| SelectTransportMode(mode) | User pilih moda transportasi | Update selectedTransportMode |
| OpenRoute | User tap tombol ROUTE | Tidak update state, UI handle Intent |
| CopyAddress | User tap copy alamat | Tidak update state, UI handle Clipboard |
| ExcludeCurrentAndSearchAgain | User tap "cari yang lain" | Tambah id ke excluded, retry scan |
| ResetScanState | Reset scan ke kondisi awal | Bersihkan field scan dan exclusion |
| ToggleNotify | User tap notify icon | Toggle isNotifyOpen |
| ResetToHome | Bottom nav Maps di-tap ulang | Reset komplet ke Home |

---

## Algoritma Explore (Penjelasan Naratif)

Saat user menekan tombol EXPLORE, sistem melakukan langkah berikut:

1. **Cancel job scan sebelumnya** kalau ada (penting agar tidak tumpang tindih)
2. **Hitung nomor attempt baru** dengan menambah 1 dari attempt sebelumnya di state
3. **Hitung batas maksimum radius** untuk attempt ini:
   - Attempt 1: 600 meter
   - Attempt 2: 1000 meter (600 + 400)
   - Attempt 3: 1400 meter
   - Dan seterusnya, tambah 400 meter per attempt
4. **Set state awal scan:** isScanning true, searchAttempt baru, lastMaxRadius sesuai hitungan, sembunyikan no-result hint, viewMode pindah ke Explore
5. **Ambil pool stasiun** dari repository (semua stasiun air dari database)
6. **Rangking kandidat** berdasarkan jarak dari user, exclude stasiun yang ada di set excludedLocationIds, urutkan ascending dari yang terdekat
7. **Loop ekspansi radius:**
   - Mulai radius dari 200 meter (konstanta START_RADIUS_METERS)
   - Cek apakah ada kandidat dengan jarak ≤ radius saat ini
   - Kalau ada: ambil kandidat pertama, set isScanning false, set foundLocations berisi satu stasiun tersebut, keluar loop
   - Kalau tidak ada: delay 800 milidetik (untuk animasi visual lingkaran), tambah radius dengan 100 meter (konstanta STEP_RADIUS_METERS), update searchRadiusMeters di state
   - Lanjut loop selama radius ≤ batas maksimum
8. **Kalau loop habis tanpa hasil:** set isScanning false, showNoResultHint true, noResultDetail berisi pesan *"Tidak ada sumber air dalam radius X meter"* (X = lastMaxRadius)

**Konstanta yang dipakai (taruh di ScanAlgorithm sebagai object):**

| Konstanta | Nilai | Makna |
|---|---|---|
| START_RADIUS_METERS | 200 | Radius awal scan |
| STEP_RADIUS_METERS | 100 | Increment per step animasi |
| MAX_RADIUS_BASE_METERS | 600 | Batas radius untuk attempt 1 |
| MAX_RADIUS_INCREMENT | 400 | Penambahan batas per attempt berikutnya |

**Fungsi pembantu di ScanAlgorithm:**

- Fungsi untuk menghitung batas radius dari nomor attempt
- Fungsi untuk menghitung jarak antar dua koordinat (pakai pendekatan equirectangular dengan formula: konversi delta lat dan delta lng ke meter, lalu hitung sqrt dari kuadrat keduanya). Cukup akurat untuk jarak di bawah 5 km
- Fungsi untuk merangking kandidat: filter exclude, map ke pasangan (stasiun, jarak), sort ascending berdasarkan jarak

---

## Algoritma "Cari yang Lain" (Retry)

Saat user tap tombol *"cari yang lain"* di no-result hint atau pin popup:

1. Ambil id dari foundLocations[0] (stasiun yang sedang ditampilkan)
2. Tambahkan id tersebut ke excludedLocationIds (set tetap unique)
3. Bersihkan foundLocations
4. Panggil `startScan` ulang (yang akan auto-increment attempt dan perbesar batas radius)

---

## Layout Layar Maps

Struktur visual MapsScreen sebagai box dengan layer bertumpuk:

```
┌────────────────────────────────────────────┐
│  [GoogleMap fullscreen di belakang]        │
│                                            │
│   - Marker untuk tiap stasiun di found     │
│   - Circle radius saat sedang scanning     │
│   - Blue dot user location (built-in)      │
│                                            │
│  ┌──────────────────────────────────────┐  │
│  │ MapsTopBar (mode label + back)       │  │
│  └──────────────────────────────────────┘  │
│                                            │
│              [PinPopup di atas             │
│               pin yang selected]           │
│                                            │
│  ┌──────────────────────────────────────┐  │
│  │ MapsActionBar                        │  │
│  │ [EXPLORE big button]                 │  │
│  │ [Walk] [Motor] [Car] toggle          │  │
│  └──────────────────────────────────────┘  │
│                                            │
│  ▼ LocationDetailSheet (modal bottom)      │
│  ▼ NotificationShiftingPanel (overlay)     │
└────────────────────────────────────────────┘
```

### MapsTopBar

- Label mode: *"HOME"* atau *"EXPLORE"* (uppercase, tracking lebar)
- Tombol back kalau mode Explore (kembali ke Home reset)

### MapsActionBar

- **Mode Home:** tombol EXPLORE besar di tengah bawah, dengan label *"EXPLORE"* uppercase. Toggle moda transportasi (chip dengan icon: jalan, motor, mobil)
- **Mode Explore + scanning:** tombol disable, label berubah jadi *"Mencari..."*. Tampilkan progress radius (*"X meter"*)
- **Mode Explore + ada hasil:** tombol berubah jadi *"Cari Lainnya"* (memicu retry)
- **Mode Explore + no result:** speech bubble dengan pesan no-result, tombol *"Coba Lagi"* (perbesar batas radius)

### PinPopup

Muncul di atas pin saat stasiun selected. Isi:

- Nama stasiun (title medium)
- Status (Open / Closed) dengan dot warna (hijau / merah)
- Indikator kapasitas (icon penuh / kosong)
- Jarak dari user (*"X m"* atau *"X km"*)
- ETA berdasarkan moda transportasi terpilih (*"~Y menit jalan"*)
- Tombol DETAIL untuk buka sheet

### LocationDetailSheet (ModalBottomSheet)

Isi konten:

- Nama stasiun besar
- Status dan kapasitas (badge)
- Alamat lengkap (text multiline)
- Tombol icon "Salin Alamat" (pakai ClipboardManager dari context)
- Toggle moda transportasi tiga chip
- Estimasi jarak dan waktu sesuai moda
- Tombol ROUTE besar di bawah, label *"ROUTE"*. Saat di-tap, buka Intent ke aplikasi Google Maps eksternal

---

## Alur Data End-to-End

> User tap EXPLORE → ViewModel terima TapExplore → memanggil startScan → coroutine launch di viewModelScope → ambil daftar stasiun dari repository → rangking kandidat → loop ekspansi radius dengan delay → setiap iterasi update state searchRadiusMeters → UI re-compose → Circle di GoogleMap update radius → kalau ketemu hit, foundLocations terisi → Marker untuk pin baru render → user tap pin → ViewModel terima SelectLocation(id) → set selectedLocation dan isDetailSheetOpen true → UI render LocationDetailSheet → user tap ROUTE → UI build Intent ke aplikasi Google Maps eksternal dengan deep link berisi koordinat tujuan → startActivity

---

## Happy Path Demo

1. Buka tab Maps. Permission lokasi diminta. User grant.
2. Peta render dengan blue dot di posisi user.
3. User tap EXPLORE. Lingkaran radius mulai dari 200 meter, mengembang bertahap (300, 400, 500, 600 meter).
4. Pin muncul saat ada stasiun masuk radius. Lingkaran berhenti.
5. User tap pin. Popup muncul dengan info ringkas dan tombol DETAIL.
6. User tap DETAIL. Bottom sheet naik dengan alamat lengkap, copy button, dan tombol ROUTE.
7. User tap ROUTE. Aplikasi Google Maps eksternal terbuka dengan navigasi langsung ke koordinat stasiun.

---

## Edge Case (Hanya yang Critical untuk Demo)

| Case | Handling |
|---|---|
| User deny permission lokasi | Tampilkan placeholder *"Izin lokasi diperlukan untuk fitur ini"* dengan tombol *"Coba Lagi"* yang re-trigger permission launcher |
| Tidak ada stasiun dalam batas max radius | Set showNoResultHint true, tampilkan speech bubble dengan pesan dan tombol retry. Tap retry akan increment attempt dan perbesar batas radius +400m |
| Aplikasi Google Maps eksternal tidak terinstall | Fallback buka browser dengan URL Google Maps web (deep link versi web) |
| Last known location belum tersedia (FusedProvider return null) | Pakai default Jakarta sebagai fallback userLocation |
| User bolak-balik tab Maps | Tap ulang tab Maps memicu ResetToHome (state kembali ke awal) |
| User tap EXPLORE saat scanning sedang berjalan | Cancel job scan lama, mulai job baru (auto-handle via Job reference di ViewModel) |
| User tap pin lain saat sheet terbuka | Update selectedLocation ke pin baru, sheet tetap terbuka dengan konten baru |

---

## Strategi Mock untuk Lomba

| Komponen | Strategi |
|---|---|
| Stasiun air | Hardcode sekitar 20 entri di DummyLocationSeeder, sebar di sekitar koordinat default Jakarta dengan radius bervariasi (50m sampai 2km dari pusat) |
| User location | Pakai FusedLocationProviderClient real. Di emulator, set mock location lewat Extended Controls |
| Route ke stasiun | Real, pakai Intent ke aplikasi Google Maps eksternal |
| ETA per moda transportasi | Estimasi sederhana berdasarkan jarak: jalan 5 km/jam, motor 25 km/jam, mobil 30 km/jam (di kota) |

---

## Known Pitfalls (Hati-Hati Ini Sering Bikin Gagal)

Beberapa hal yang sering jadi sumber error saat implementasi Maps Compose. Pastikan AI agent diberitahu hal-hal ini:

1. **Import CameraPosition dari paket model.** Library Google Maps Compose punya beberapa class dengan nama mirip. Class `CameraPosition` (untuk init kamera) berasal dari paket model di Play Services Maps, bukan dari paket Compose-nya. Salah import sering bikin compile error yang membingungkan.

2. **`rememberCameraPositionState` perlu di-init dengan posisi awal.** Kalau lupa di-init, kamera default ke koordinat 0,0 (Atlantic Ocean) dan user bingung kenapa peta kosong. Init dengan posisi user atau default Jakarta.

3. **Permission perlu launch eksplisit di LaunchedEffect.** Tidak otomatis terjadi saat layar pertama dibuat. Pakai `rememberLauncherForActivityResult` dengan kontrak `RequestMultiplePermissions` lalu launch dengan array berisi dua permission (fine dan coarse).

4. **FusedLocationProviderClient butuh permission cek manual.** Sebelum panggil `lastLocation`, harus pastikan permission sudah granted. Kalau tidak, akan throw SecurityException.

5. **Cancel scan job sebelum start baru.** Kalau user spam tap EXPLORE, akan ada banyak coroutine berjalan paralel dan state update saling timpa. Pakai Job property di ViewModel dan cancel sebelum launch baru.

6. **Marker onClick harus return Boolean.** Library Maps Compose kontrak click listener-nya minta return Boolean (true berarti consume event, false berarti propagate ke handler default). Kelupaan return → compile error.

7. **`isMyLocationEnabled` perlu permission cek.** Property ini di MapProperties harus di-set conditional pada hasLocationPermission, jangan langsung true. Kalau permission belum granted dan di-set true, runtime crash.

8. **Intent ke Google Maps perlu setPackage agar deep link benar.** Set package ke `com.google.android.apps.maps` agar Android tahu intent ditujukan ke aplikasi Maps. Kalau tidak, system chooser muncul atau intent gagal.

---

## Skip Kalau Waktu Mepet

Hal yang boleh di-skip dari prioritas terbawah ke teratas:

- Toggle moda transportasi (hardcode Walk saja)
- Perhitungan ETA real (tampilkan placeholder *"-- menit"*)
- Animasi expanding sheet detail (pakai AnimatedVisibility default)
- Pecah UI jadi enam file (tulis semua di MapsScreen.kt dulu, asal di bawah 600 baris)
- Reset on bottom nav re-tap (langsung set viewMode = Home saja, tidak perlu reset komplet)

**Yang absolut wajib jalan:** peta render, EXPLORE memicu animasi ekspansi radius, pin muncul, detail sheet menampilkan alamat, tombol ROUTE membuka aplikasi Maps eksternal.
