# EXECUTION PLAN — 8 Jam Live Coding

> Timeline berbasis jam. Setiap fase punya checkpoint. Jika fase N molor lebih dari 15 menit, lompat ke fase N+1 dan kembali jika sempat.

---

## Ringkasan Timeline

| Jam | Fase | Output Utama |
|---|---|---|
| 00:00 – 01:00 | Fase 0 — Foundation | Project siap, Gradle hijau, Database aktif, navigasi tiga tab jalan |
| 01:00 – 03:00 | Fase 1 — Maps (P1) | State machine explore, ekspansi radius, pin, detail sheet, route intent |
| 03:00 – 05:00 | Fase 2 — Hydration (P2) | Set target, log sesi, progress ring, history panel |
| 05:00 – 06:30 | Fase 3 — Points (P3) | Wallet, redeem, scan stub, batas harian dipaksakan |
| 06:30 – 07:00 | Fase 4 — Onboarding + Notifications | Splash satu halaman, in-app notification bus, guidance panel |
| 07:00 – 08:00 | Fase 5 — Demo Polish | Integration test manual, fix bug demo path, rehearsal |

---

## Fase 0 — Foundation (00:00 – 01:00)

### 0.1 Bikin Project Android (00:00 – 00:15)

Langkah:
1. Buka Android Studio. Pilih *New Project* dengan template *Empty Activity* berbasis Compose.
2. Set application id ke `com.mahendra.android.hydrolink`.
3. Set bahasa Kotlin, minimum SDK 26, target SDK 36.
4. Lokasi project: folder `hydrolink-app/` di workspace.

### 0.2 Konfigurasi Dependency (00:15 – 00:25)

Library yang harus disiapkan (nama saja, biarkan AI agent isi versi terbaru saat generate):

- Jetpack Compose (BoM atau standalone)
- Material 3 components untuk Compose
- Navigation Compose
- Lifecycle ViewModel Compose
- Lifecycle ViewModel KTX (untuk viewModelScope)
- Room Runtime
- Room KTX
- Room Compiler (sebagai KSP processor, bukan annotation processor lama)
- Google Maps Compose
- Google Play Services Maps
- Google Play Services Location
- Coroutines core dan android

Plugin Gradle yang harus aktif:

- Plugin Android Application
- Plugin Kotlin Android
- Plugin Kotlin Compose
- Plugin KSP

Gradle sync. Pastikan hijau sebelum lanjut.

### 0.3 Konfigurasi Manifest (00:25 – 00:30)

Yang harus disiapkan di Manifest:

- Permission akses lokasi (fine dan coarse)
- Permission internet (untuk Google Maps tile loading)
- Pendaftaran kelas Application kustom (`HydrolinkApplication`)
- Meta-data Maps API key dengan placeholder dari build config

Di file properties lokal (yang tidak di-commit), simpan API key Google Maps dengan kunci `MAPS_API_KEY`. Build script harus inject ini sebagai manifest placeholder.

### 0.4 Layer Data (00:30 – 00:50)

Buat seluruh komponen data layer:

- Empat entity di folder `data/local/entity/` sesuai schema di `DATA_MODEL.md`
- Empat DAO di folder `data/local/dao/` dengan method sesuai pola yang dijelaskan di DATA_MODEL.md
- Satu kelas Database di `data/local/db/HydrolinkDatabase.kt` yang mendaftarkan keempat entity
- Satu seeder dummy di `data/local/seed/DummyLocationSeeder.kt` yang berisi sekitar 20 entri stasiun air dengan koordinat sebar di sekitar Jakarta (default user location)
- Satu interface `LocalRepository.kt` di `data/repository/` dengan method yang didefinisikan di `DATA_MODEL.md`
- Satu implementasi `LocalRepositoryImpl.kt` yang menyuntikkan keempat DAO via constructor

**Tip:** pakai prompt template `ai_prompts/generate_module.txt` dengan target *"layer data lengkap berdasarkan DATA_MODEL.md"*.

### 0.5 Application, Activity, Navigasi (00:50 – 00:57)

Buat:

- `HydrolinkApplication.kt` yang menginisialisasi Database lewat builder Room dan membangun instance Repository, lalu meng-expose keduanya sebagai property publik
- `MainActivity.kt` sebagai single activity yang memanggil setContent dengan tema Hydrolink dan host Navigation
- `core/navigation/AppRoute.kt` berupa sealed class dengan empat case: Onboarding, MainHome, dan tiga tab (Maps, Hydration, Points)
- `core/navigation/HydrolinkNavHost.kt` yang setup NavController dan NavHost. Start destination menuju Onboarding, lalu MainHome (yang berisi bottom navigation dengan tiga tab)

### 0.6 Theme dan Infrastruktur Core (00:57 – 01:00)

Buat:

- `core/ui/theme/Color.kt` dengan warna utama cyan (kode hex 21BEDC), aksen hijau (2ED4A1), aksen pink (E5159A), aksen oranye (F28B3C), background light gray (EFEFEF)
- `core/ui/theme/Type.kt` dengan font Roboto, lima sampai enam ukuran skala, tracking 0.08em untuk uppercase
- `core/ui/theme/Theme.kt` yang membungkus Material3 dengan color scheme dan typography
- `core/notification/NotificationEvent.kt` berupa sealed interface dengan lima tipe event (lihat `modules/notifications.md`)
- `core/notification/NotificationBus.kt` berupa singleton object yang membungkus SharedFlow
- `core/guidance/GuidanceMessage.kt` berupa sealed class dengan lima varian pesan (lihat `modules/notifications.md`)

### Checkpoint Fase 0

- [ ] Aplikasi build dan jalan di emulator
- [ ] Bottom navigation tiga tab bisa di-tap dan switch screen (placeholder kosong dulu)
- [ ] File database `hydrolink.db` terbuat di storage internal device
- [ ] Membuka tab Maps tidak crash (boleh tampil placeholder, asal tidak crash)

---

## Fase 1 — Maps (01:00 – 03:00) — Prioritas 1

Baca lengkap `modules/maps.md` sebelum mulai.

### 1.1 State dan Event (01:00 – 01:10)

Buat:

- `feature/maps/state/MapsUiState.kt` — data class yang menampung seluruh field state Maps (lihat tabel state di `modules/maps.md`)
- `feature/maps/state/MapsEvent.kt` — sealed interface dengan event yang didefinisikan di `modules/maps.md`

### 1.2 Algoritma Scan (01:10 – 01:20)

Buat `feature/maps/logic/ScanAlgorithm.kt` sebagai object murni Kotlin (tanpa import Android atau Compose). Berisi:

- Konstanta radius awal (200 meter)
- Konstanta step radius (100 meter)
- Konstanta batas radius dasar (600 meter untuk attempt pertama)
- Konstanta increment per attempt (400 meter)
- Fungsi untuk menghitung batas radius berdasarkan nomor attempt
- Fungsi untuk menghitung jarak antar dua koordinat (pakai pendekatan equirectangular, cukup akurat untuk jarak di bawah 5 km)
- Fungsi untuk merangking kandidat stasiun berdasarkan jarak dari user, dengan filter id yang dikecualikan

### 1.3 ViewModel (01:20 – 01:50)

Buat `feature/maps/viewmodel/MapsViewModel.kt`. Fokus implementasi:

- State flow yang bisa di-observe dari UI
- Handler `onEvent` yang menangani semua event di sealed interface
- Logika `startScan` yang loop ekspansi radius dengan delay 800 milidetik per step (jangan lupa cancel job sebelumnya)
- Logika `excludeAndRetry` yang menambah id stasiun ke set excluded lalu memanggil scan ulang
- Logika `openDetail` yang memilih stasiun aktif dari list found
- Logika `resetToHome` yang membersihkan state ke kondisi awal
- Job reference untuk scan agar bisa di-cancel saat user tap explore lagi atau back

### 1.4 Layar Maps Root + GoogleMap (01:50 – 02:20)

Buat `feature/maps/ui/MapsScreen.kt`:

- Permission launcher untuk request lokasi (fine dan coarse) saat layar pertama dibuka
- Setelah permission granted, ambil last known location dari FusedLocationProviderClient dan kirim event `UserLocationUpdated` ke ViewModel
- Layer GoogleMap dengan camera position state yang di-init ke posisi user (atau default Jakarta jika belum tersedia)
- Properties Map dengan flag `isMyLocationEnabled` yang bergantung ke status permission
- Saat state `isScanning` aktif, render Circle composable di sekitar user dengan radius dari state
- Loop di seluruh `foundLocations` untuk render Marker composable

### 1.5 Pin Popup, Detail Sheet, Action Bar (02:20 – 02:45)

Pecah jadi beberapa file composable:

- `feature/maps/ui/PinPopup.kt` — popup yang muncul di atas pin saat selected, berisi nama stasiun, status, dan jarak
- `feature/maps/ui/LocationDetailSheet.kt` — ModalBottomSheet dengan alamat lengkap, tombol salin alamat (pakai ClipboardManager dari context), pilihan moda transportasi, dan tombol ROUTE yang membuka Intent ke aplikasi Google Maps eksternal
- `feature/maps/ui/MapsActionBar.kt` — tombol EXPLORE besar yang jadi entry utama, plus toggle moda transportasi

### 1.6 Integrasi dan Test Manual (02:45 – 03:00)

Jalankan aplikasi. Skenario test:

1. Buka tab Maps, grant permission lokasi
2. Pastikan peta render dengan posisi user
3. Tap EXPLORE, lihat lingkaran radius mengembang dari 200m
4. Pastikan pin muncul setelah ada hit
5. Tap pin, pastikan popup muncul
6. Tap DETAIL, pastikan sheet muncul dengan address
7. Tap ROUTE, pastikan aplikasi Google Maps eksternal terbuka

Fix bug critical. Kalau API key error, cek file properties lokal.

### Checkpoint Fase 1

- [ ] Map render dengan blue dot user location
- [ ] EXPLORE memicu animasi lingkaran ekspansi
- [ ] Pin muncul saat scan menemukan hit
- [ ] Tap pin menampilkan popup
- [ ] Tap DETAIL menampilkan sheet dengan alamat
- [ ] Tap ROUTE membuka Google Maps eksternal

---

## Fase 2 — Hydration (03:00 – 05:00) — Prioritas 2

Baca lengkap `modules/hydration.md` sebelum mulai.

### 2.1 Logic Constants, State, Event (03:00 – 03:15)

- `feature/hydration/logic/HydrationRules.kt` — object Kotlin berisi konstanta target (min 1500, max 2500, step 100, default 2000), konstanta Ml per gelas (100), maksimal jumlah gelas, daftar ukuran minum (100, 250, 350), fungsi konversi Ml ke poin, dan fungsi konversi target ke jumlah gelas
- `feature/hydration/state/HydrationUiState.kt` — data class dengan field state Hydration (lihat tabel di `modules/hydration.md`)
- `feature/hydration/state/HydrationEvent.kt` — sealed interface dengan event Hydration

### 2.2 ViewModel (03:15 – 03:45)

`feature/hydration/viewmodel/HydrationViewModel.kt`. Yang harus di-implement:

- Init: load profil user dari repository, ambil sesi hari ini, hitung total Ml hari ini, set step awal (Started kalau target 0, Main kalau sudah ada target)
- Init bagian dua: subscribe ke Flow sesi hari ini dari repository, update state secara reaktif
- Handler event TapLetsGo: pindah step ke Configure, set draftTarget ke default (2000)
- Handler event UpdateDraftTarget: update draftTarget dengan coerce ke rentang valid
- Handler event SaveTarget: panggil repository setUserTargetMl, update state target dan pindah step ke Main
- Handler event OpenDrinkSheet, CloseDrinkSheet, SelectDrinkSize: update flag dan selected size di state
- Handler event ConfirmDrink: hitung poin, insert sesi via repository, panggil repository addPoints, emit NotificationEvent.HydrationLogged ke bus, tutup sheet, buka notify panel
- Handler event ToggleHistoryPanel dan ToggleNotifyPanel: toggle flag

### 2.3 Layar Hydration (03:45 – 04:45)

`feature/hydration/ui/HydrationScreen.kt`. Tiga sub-view berdasarkan step:

**View Started:** ilustrasi sederhana, tagline pendek, tombol besar LET'S GO.

**View Configure:** judul *"Atur target harian"*, tampilan angka draft target besar di atas (contoh: *"2000 Ml"*), Slider Material3 dengan rentang 1500 sampai 2500 step 100, visualisasi gelas (kotak grid 5x5 atau row, gelas yang aktif diwarnai cyan sesuai jumlah dari `glassesForTarget`), tombol SAVE di bawah.

**View Main:** ring lingkaran progress di tengah (pakai `CircularProgressIndicator` Material3 dengan size besar, atau Canvas custom kalau sempat), tampilkan total Ml hari ini per target di tengah ring, baris tiga tombol di bawah ring (icon History kiri, tombol MINUM tengah besar, icon Notify kanan).

**Bottom sheet pilih ukuran:** tiga FilterChip dengan label *"100 Ml"*, *"250 Ml"*, *"350 Ml"*. Tombol MINUM di bawah untuk konfirmasi.

**History panel overlay:** LazyColumn berisi list sesi hari ini, tiap item menampilkan ukuran dan jam.

**Notify panel overlay:** menampilkan pesan poin terakhir dengan format *"+X poin • (jam)"*.

### 2.4 Integrasi dan Test Manual (04:45 – 05:00)

Skenario test:

1. Buka Hydration tab pertama kali, lihat tombol LET'S GO
2. Tap LET'S GO, masuk Configure
3. Geser slider ke 2000, pastikan visualisasi gelas update
4. Tap SAVE, masuk Main dengan ring 0%
5. Tap MINUM tengah, sheet muncul, pilih 250 Ml, tap MINUM
6. Pastikan ring update, notify panel muncul dengan *"+2 poin • (jam)"*
7. Tutup aplikasi, buka lagi, pastikan target dan log hari ini tetap

### Checkpoint Fase 2

- [ ] Slider target tersimpan dan terload kembali setelah restart
- [ ] Progress ring update setelah log minum
- [ ] History panel menampilkan log dengan timestamp
- [ ] Poin masuk ke wallet (cek dengan buka Points tab)

---

## Fase 3 — Points (05:00 – 06:30) — Prioritas 3

Baca lengkap `modules/points.md` sebelum mulai.

### 3.1 Logic, State, Event (05:00 – 05:15)

- `feature/points/logic/RedeemRules.kt` — object Kotlin dengan konstanta poin per token (100), cap activePoints (100), cap bankPoints (300), batas redeem harian (1), batas pakai token harian (1), fungsi `computeRedeem` (return hasil atau null kalau tidak cukup), fungsi `overflowToBank` (untuk distribusi poin masuk antara active dan bank)
- `feature/points/state/PointsUiState.kt` — data class dengan field wallet plus flag UI (lihat tabel di `modules/points.md`). Sertakan computed property `canRedeemToday` dan `canScan`
- `feature/points/state/PointsEvent.kt` — sealed interface dengan event Points

### 3.2 ViewModel (05:15 – 05:45)

`feature/points/viewmodel/PointsViewModel.kt`. Yang harus di-implement:

- Init: subscribe ke Flow wallet dari repository, update state setiap ada perubahan, set field todayIso ke tanggal hari ini (format ISO YYYY-MM-DD)
- Handler event TapRedeem: cek flag canRedeemToday, panggil `RedeemRules.computeRedeem`, kalau hasil null abort, kalau sukses panggil repository applyRedeem dengan parameter lengkap, emit RedeemSuccess ke bus, buka notify panel
- Handler event OpenScan, CloseScan, SelectScanVolume: update flag UI
- Handler event ConfirmScan: cek flag canScan, panggil repository consumeToken, emit ScanSuccess ke bus dengan volume yang dipilih, tutup scan sheet, buka notify panel
- Handler event ToggleNotifyPanel: toggle flag

### 3.3 Layar Points (05:45 – 06:20)

`feature/points/ui/PointsScreen.kt`. Komposisi:

**Header:** judul *"REDEEM"* dengan style title large.

**Wallet card:** outlined card dengan border cyan, padding 20dp. Isi: baris atas tampilkan *"Total Redeem: X"*, baris bawah tiga kolom (rata sisi) — Total Poin (format *"X/100"*), Token (format *"X/1"*), Bank Poin (format *"X/300"*).

**Action row:** tiga tombol horizontal dengan jarak 12dp. Tombol Notify (outlined, kiri), tombol REDEEM (filled, tengah, disable kalau `canRedeemToday` false), tombol SCAN (filled, kanan, disable kalau `canScan` false).

**Scan sheet (ModalBottomSheet):** judul *"Pilih volume"*, tiga FilterChip (250 Ml, 500 Ml, 1000 Ml), area kamera placeholder (kotak hitam dengan teks *"Kamera QR (simulasi)"* di tengah, tinggi 200dp), tombol SCAN besar di bawah.

**Notify panel overlay:** menampilkan pesan terakhir (redeem berhasil atau scan berhasil) dengan tombol close.

### 3.4 Logika Reset Harian (06:20 – 06:30)

ViewModel mengecek apakah `lastRedeemDateIso` di wallet sama dengan `todayIso` (tanggal hari ini). Kalau sama, redeem sudah dilakukan hari ini dan tombol REDEEM disable. Kalau berbeda atau null, redeem tersedia.

Tanggal hari ini dihitung saat init ViewModel pakai `LocalDate.now().toString()` (otomatis format ISO YYYY-MM-DD).

### Checkpoint Fase 3

- [ ] Wallet menampilkan angka yang benar
- [ ] REDEEM disable kalau total + bank kurang dari 100
- [ ] Redeem sukses memberikan token dan kurangi poin sesuai prioritas (active dulu, bank fallback)
- [ ] Coba redeem dua kali di hari sama, kedua harus ditolak
- [ ] SCAN consume token (boleh stub kamera)

---

## Fase 4 — Onboarding dan Notifications (06:30 – 07:00)

### 4.1 Onboarding Minimal (06:30 – 06:45)

Pilih opsi yang paling cepat sesuai sisa waktu:

**Opsi A (rekomendasi untuk lomba):** splash satu halaman dengan logo (Icon WaterDrop dari Material), nama aplikasi *"HydroLink"* dengan style display medium, tagline pendek (*"Hidrasi komunitas dalam genggaman"*), dan tombol GET STARTED. Saat tap, navigasi ke MainHome dengan popUpTo agar onboarding tidak bisa di-back.

**Opsi B (kalau waktu mepet):** skip total. Set start destination NavHost langsung ke MainHome.

### 4.2 Wiring Notification Bus (06:45 – 06:55)

Di setiap layar utama (Maps, Hydration, Points):

- `LaunchedEffect` dengan key `Unit` yang subscribe ke `NotificationBus.events`
- Saat event masuk, mapping ke pesan teks sesuai jenis event (lihat `modules/notifications.md`)
- Tampilkan pesan via state lokal yang trigger composable `NotificationShiftingPanel`

Format pesan:

- HydrationLogged: *"+X poin • Tepat waktu (HH.mm)"*
- RedeemSuccess: *"Redeem berhasil • +X token"*
- ScanSuccess: *"Scan berhasil • X Ml siap diambil"*
- MapsFound: *"Sumber air ditemukan: nama_stasiun"*

### 4.3 Guidance Panel (06:55 – 07:00)

Tampilkan composable `GuidancePanel` di posisi atas content (di bawah top bar) di tiga layar utama. ViewModel masing-masing layar punya computed property yang memilih varian pesan sesuai state.

Pemilihan varian:

- Maps home (mode Home): pakai varian MapsHome
- Hydration home (totalMl 0 hari ini): pakai varian HydrationHomeEmpty
- Hydration home (totalMl > 0): pakai varian HydrationHomeProgress
- Points (poin >= 100): pakai varian PointsReady
- Points (poin < 100): pakai varian PointsLow

Sembunyikan guidance panel saat ada overlay aktif (notify panel, detail sheet) supaya tidak menumpuk.

### Checkpoint Fase 4

- [ ] Onboarding atau splash tampil saat first launch
- [ ] Notify panel muncul setelah log drink atau redeem
- [ ] Guidance panel tampil di minimal satu layar dengan varian yang sesuai state

---

## Fase 5 — Demo Polish (07:00 – 08:00)

### 5.1 Run-Through Demo Lengkap (07:00 – 07:15)

Jalankan skenario demo lengkap dari `SYSTEM_OVERVIEW.md` bagian *Alur User Sederhana*:

1. Launch → Onboarding → MainHome
2. Maps → EXPLORE → pin → DETAIL → ROUTE
3. Hydration → log minum → notify
4. Ulangi minum sampai poin penuh
5. Points → REDEEM → SCAN

Catat semua bug yang muncul, prioritaskan yang critical (crash, layout patah, state stuck).

### 5.2 Fix Bug Critical (07:15 – 07:45)

Pakai prompt template `ai_prompts/debug_fix.txt` untuk setiap bug. Prioritas urutan fix:

1. Crash atau ANR
2. State tidak update setelah aksi user
3. Layout patah (overflow, overlap berlebihan)
4. Visual inkonsistensi (warna salah, padding aneh)

### 5.3 Polish Visual (07:45 – 07:55)

- Padding konsisten 20dp horizontal di seluruh konten utama
- Button punya state ripple dan disable yang jelas
- Loading state di Maps saat permission masih pending
- Empty state di history panel kalau belum ada sesi

### 5.4 Rehearsal Final (07:55 – 08:00)

Run demo sekali lagi end-to-end. Pastikan empat skenario inti jalan mulus tanpa error. Backup APK kalau memungkinkan.

---

## Aturan Saat Waktu Mepet

| Situasi | Tindakan |
|---|---|
| Fase 1 Maps molor lebih dari 15 menit | Skip pecah composable child, tulis semua di `MapsScreen.kt` |
| Fase 2 progress ring custom Canvas terlalu kompleks | Pakai `CircularProgressIndicator` standar Material3 |
| Fase 3 kamera CameraX gagal | Pakai tombol *Simulate Scan* sederhana |
| Fase 4 onboarding macet | Skip total, langsung MainHome |
| Sisa kurang dari 15 menit | Berhenti tambah fitur. Fokus fix bug di demo path |

---

## Penggunaan AI Agent per Fase

| Fase | Template Prompt yang Dipakai |
|---|---|
| 0.4 (Layer Data) | `ai_prompts/generate_module.txt` dengan target *"data layer lengkap"* |
| 1.1 sampai 1.3 (Maps state, logic, VM) | `ai_prompts/generate_viewmodel.txt` dengan target Maps |
| 1.4 sampai 1.5 (Maps UI) | `ai_prompts/generate_ui.txt` dengan target MapsScreen |
| 2.2 dan 3.2 (ViewModel) | `ai_prompts/generate_viewmodel.txt` |
| 2.3 dan 3.3 (UI) | `ai_prompts/generate_ui.txt` |
| Bug apapun | `ai_prompts/debug_fix.txt` |

**Tip pembagian agent:** Claude Code CLI Opus untuk fase kompleks (Maps state machine, ViewModel logic). Codex Desktop untuk sub-composable yang lebih independen (PinPopup, LocationDetailSheet, ScanSheet).
