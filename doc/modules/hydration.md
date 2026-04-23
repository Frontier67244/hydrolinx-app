# Module: Hydration (P2)

> Prioritas 2. Target waktu: 2 jam.

## Tujuan Fitur

User mengatur target minum harian (1500 sampai 2500 Ml) lalu log sesi minum dengan ukuran 100, 250, atau 350 Ml. UI menampilkan progress ring + history. Setiap log otomatis menambah poin di wallet (modul Points).

---

## Struktur File

```
feature/hydration/
├── ui/HydrationScreen.kt
├── state/
│   ├── HydrationUiState.kt
│   └── HydrationEvent.kt
├── viewmodel/HydrationViewModel.kt
└── logic/HydrationRules.kt
```

---

## Konstanta di HydrationRules

`HydrationRules` adalah object Kotlin murni (tanpa dependensi Android atau Compose). Berisi:

| Konstanta / Fungsi | Nilai / Spesifikasi |
|---|---|
| MIN_TARGET_ML | 1500 |
| MAX_TARGET_ML | 2500 |
| STEP_TARGET_ML | 100 |
| DEFAULT_TARGET_ML | 2000 |
| ML_PER_GLASS | 100 (1 gelas = 100 Ml untuk visualisasi) |
| MAX_GLASSES (computed) | MAX_TARGET_ML dibagi ML_PER_GLASS = 25 gelas |
| DRINK_SIZES_ML | List berisi tiga nilai: 100, 250, 350 (LOCKED, jangan diubah) |
| pointsFor(amountMl) | Mengembalikan amountMl dibagi ML_PER_GLASS. Konversi 1 poin per 100 Ml |
| glassesForTarget(targetMl) | Mengembalikan jumlah gelas yang harus diwarnai di indikator. Hitung targetMl dibagi ML_PER_GLASS, di-coerce ke rentang 0 sampai MAX_GLASSES |

---

## State Hydration

Field di `HydrationUiState`:

| Field | Tipe | Default | Makna |
|---|---|---|---|
| step | enum (Started, Configure, Main) | Started | Tahap UI saat ini |
| targetMl | bilangan bulat | 0 | Target tersimpan. 0 berarti belum di-set |
| draftTargetMl | bilangan bulat | 2000 | Nilai slider saat di Configure (sebelum SAVE) |
| totalMlToday | bilangan bulat | 0 | Akumulasi Ml hari ini |
| todaySessions | list sesi | kosong | Daftar sesi minum hari ini (sumber data history panel) |
| isDrinkSheetOpen | boolean | false | Apakah bottom sheet pilih ukuran terbuka |
| selectedDrinkSizeMl | bilangan bulat | 250 | Ukuran terpilih di sheet (default 250) |
| isHistoryPanelOpen | boolean | false | Apakah history panel terbuka |
| isNotifyPanelOpen | boolean | false | Apakah notify panel terbuka |
| lastAwardedPoints | bilangan bulat | 0 | Poin terakhir yang diberikan (untuk konten notify panel) |

**Step enum:**

- **Started** — user belum pernah set target. UI menampilkan tombol LET'S GO.
- **Configure** — user sedang setting target via slider. UI menampilkan slider, visualisasi gelas, tombol SAVE.
- **Main** — operasi normal. UI menampilkan progress ring, tombol minum, history dan notify icon.

---

## Event Hydration

Event di sealed interface `HydrationEvent`:

| Event | Trigger | Efek |
|---|---|---|
| TapLetsGo | User tap tombol LET'S GO di view Started | Pindah step ke Configure, set draftTarget ke DEFAULT_TARGET_ML |
| UpdateDraftTarget(ml) | User geser slider | Update draftTarget dengan coerce ke rentang valid |
| SaveTarget | User tap SAVE | Persist target ke repository, pindah step ke Main |
| OpenDrinkSheet | User tap tombol minum | Set isDrinkSheetOpen true |
| CloseDrinkSheet | User dismiss sheet | Set isDrinkSheetOpen false |
| SelectDrinkSize(ml) | User pilih chip 100/250/350 | Update selectedDrinkSizeMl |
| ConfirmDrink | User tap MINUM di sheet | Insert sesi, tambah poin, emit notifikasi, tutup sheet, buka notify panel |
| ToggleHistoryPanel | User tap history icon | Toggle isHistoryPanelOpen |
| ToggleNotifyPanel | User tap notify icon | Toggle isNotifyPanelOpen |

---

## Tugas ViewModel

### Init (Saat ViewModel Dibuat)

1. **Load profil user** dari repository (suspend get)
2. **Tentukan step awal:** kalau target di profil > 0, step = Main. Kalau target = 0, step = Started
3. **Load sesi hari ini** dari repository (suspend get list)
4. **Hitung totalMlToday** dengan menjumlahkan amountMl dari list sesi
5. **Update state awal** dengan field-field hasil di atas
6. **Subscribe ke Flow sesi hari ini** dari repository — setiap kali ada perubahan, update todaySessions dan totalMlToday di state secara reaktif

### Handler ConfirmDrink (Yang Paling Penting)

Saat user tap MINUM di sheet:

1. Ambil `selectedDrinkSizeMl` dari state
2. Hitung poin pakai `HydrationRules.pointsFor(amountMl)` — hasil = amountMl / 100
3. Ambil timestamp sekarang (millis epoch)
4. Insert sesi baru ke repository dengan amountMl, pointsAwarded, dan timestamp
5. Tambah poin ke wallet via repository (yang internal apply aturan overflow)
6. Emit event `NotificationEvent.HydrationLogged(points, timestamp)` ke `NotificationBus`
7. Update state: tutup drink sheet, buka notify panel, simpan lastAwardedPoints

### Handler SaveTarget

1. Ambil draftTargetMl dari state
2. Panggil `repository.setUserTargetMl(draftTargetMl)` (suspend, upsert ke user_profiles)
3. Update state: targetMl ke nilai baru, step pindah ke Main

---

## Layout Layar Hydration

### View Started

```
┌──────────────────────────────────────────┐
│                                          │
│         [Ilustrasi sederhana]            │
│       (icon air drop besar cyan)         │
│                                          │
│         "Mulai perjalanan                │
│          hidrasi kamu"                   │
│                                          │
│                                          │
│       ┌────────────────────────┐         │
│       │      LET'S GO          │         │
│       └────────────────────────┘         │
│                                          │
└──────────────────────────────────────────┘
```

Konten: ilustrasi, headline, satu tombol besar.

### View Configure

```
┌──────────────────────────────────────────┐
│  Atur target harian                      │
│                                          │
│         2000 Ml                          │
│         (display medium)                 │
│                                          │
│   ────●───────────────────────           │
│   1500              2500                 │
│                                          │
│   ┌──┐┌──┐┌──┐┌──┐┌──┐                   │
│   │■ ││■ ││■ ││■ ││■ │   (gelas terisi)  │
│   └──┘└──┘└──┘└──┘└──┘                   │
│   ┌──┐┌──┐┌──┐┌──┐┌──┐                   │
│   │■ ││■ ││■ ││■ ││■ │                   │
│   └──┘└──┘└──┘└──┘└──┘                   │
│   ... 20 gelas terisi cyan, 5 abu-abu    │
│                                          │
│       ┌────────────────────────┐         │
│       │        SAVE            │         │
│       └────────────────────────┘         │
└──────────────────────────────────────────┘
```

Komponen:

- Judul *"Atur target harian"*
- Display angka draft target besar (contoh *"2000 Ml"*)
- Slider Material3 dengan rentang MIN sampai MAX, step 100. Saat geser, update draftTarget via event
- Visualisasi 25 gelas dalam grid 5 kolom. Jumlah gelas terisi cyan = `glassesForTarget(draftTarget)`. Sisanya abu-abu
- Tombol SAVE besar di bawah

### View Main

```
┌──────────────────────────────────────────┐
│                                          │
│            ╭────────╮                    │
│           │   45%   │                    │
│           │ 900/2000│                    │
│            ╰────────╯                    │
│         (progress ring)                  │
│                                          │
│   [📋 history]  [💧 MINUM]  [🔔 notify] │
│   (icon)        (button)    (icon)       │
│                                          │
└──────────────────────────────────────────┘
```

Komponen:

- Progress ring di tengah dengan teks dalam ring (total/target Ml dan persentase)
- Baris tiga tombol di bawah ring: icon History (kiri), tombol MINUM besar (tengah), icon Notify (kanan)

### Bottom Sheet Drink Size

Komponen di sheet:

- Judul *"Pilih ukuran minum"*
- Tiga FilterChip horizontal dengan label *"100 Ml"*, *"250 Ml"*, *"350 Ml"*. Chip yang terpilih diwarnai cyan
- Tombol MINUM besar di bawah (memicu ConfirmDrink)

### History Panel Overlay

LazyColumn berisi item-item sesi hari ini, urutan dari paling baru. Tiap item menampilkan:

- Ukuran sesi (*"250 Ml"*)
- Jam sesi (format HH.mm)
- Poin yang didapat (*"+2 poin"*)

### Notify Panel Overlay

Card berisi pesan dengan format: *"+X poin • Tepat waktu (HH.mm)"* di mana X adalah lastAwardedPoints. Tombol close di pojok.

---

## Repository Method yang Dibutuhkan

(Detail di `DATA_MODEL.md`. Ringkasan untuk modul ini:)

- `getUserProfile()` — suspend, return profile atau null
- `setUserTargetMl(ml)` — suspend, upsert profile dengan target baru
- `getTodaySessions()` — suspend, return list sesi hari ini
- `observeTodaySessions()` — return Flow of list sesi hari ini (reactive)
- `insertSession(session)` — suspend, insert sesi baru
- `addPoints(amount)` — suspend, internal apply aturan overflow ke Bank Poin

**Implementasi addPoints di repository (penjelasan tanpa kode):**

1. Ambil wallet saat ini dari DAO (atau default kosong kalau null)
2. Panggil `RedeemRules.overflowToBank(amount, currentActive, currentBank)` (lihat `modules/points.md`)
3. Update wallet dengan nilai newActive dan newBank dari hasil
4. Upsert wallet kembali ke database

---

## Query "Sesi Hari Ini" — Cara Kerja

Hari ini didefinisikan dimulai dari pukul 00:00 lokal user. Cara hitung di Kotlin:

> Ambil tanggal hari ini dengan `LocalDate.now()` → konversi ke instant awal hari di zona waktu sistem dengan `atStartOfDay(ZoneId.systemDefault())` → konversi ke epoch millis dengan `toInstant().toEpochMilli()`

Hasil epoch millis ini di-pass ke DAO sebagai parameter filter. Query Room: ambil semua row dari `hydration_sessions` di mana `sessionTimestampEpochMillis >= startOfDay`, urutkan descending berdasarkan timestamp.

DAO menyediakan dua method untuk query ini:

- Versi suspend (return list sekali ambil)
- Versi Flow (return Flow of list, untuk reactive observation)

---

## Happy Path Demo

1. Buka tab Hydration. UI menampilkan view Started dengan tombol LET'S GO.
2. User tap LET'S GO. Pindah ke view Configure. Slider di posisi 2000.
3. User geser slider ke 2000 Ml. Visualisasi gelas menampilkan 20 gelas cyan, 5 abu-abu.
4. User tap SAVE. Pindah ke view Main. Ring di 0%, total 0 / 2000 Ml.
5. User tap tombol MINUM tengah. Bottom sheet naik dengan tiga chip.
6. User pilih chip *"250 Ml"*. User tap MINUM di sheet.
7. Sheet tutup. Ring update jadi sekitar 12%. Notify panel muncul di atas dengan teks *"+2 poin • Tepat waktu (jam saat ini)"*.
8. User tap icon history. Panel menampilkan satu item: *"250 Ml • (jam)"*.
9. User tutup aplikasi, buka lagi. Target tetap, sesi hari ini tetap muncul.

---

## Edge Case (Hanya yang Critical untuk Demo)

| Case | Handling |
|---|---|
| Target belum di-set, lalu user reopen aplikasi | Init ViewModel detect targetMl == 0, set step ke Started, tampilkan LET'S GO |
| User geser slider melampaui rentang | Coerce di handler UpdateDraftTarget ke MIN..MAX |
| User log minum melampaui target | Tetap log. Progress ring di-cap di 100% secara visual (pakai coerceAtMost di kalkulasi progress) |
| List sesi hari ini panjang | History panel pakai LazyColumn agar performant |
| Wallet poin sudah cap 100 | Repository.addPoints internal panggil overflowToBank, sisa otomatis ke Bank Poin |
| Midnight rollover saat aplikasi terbuka | Flow observeTodaySessions otomatis filter hari ini saja, list akan refresh sendiri (asal startOfDay dihitung ulang per query — boleh skip optimization untuk lomba) |

---

## Strategi Mock untuk Lomba

| Komponen | Strategi |
|---|---|
| User profile | Single row default di Room dengan id 0. Target = nilai dari slider |
| Timestamp sesi | Pakai `System.currentTimeMillis()` real |
| Logic on-time vs late | **Disederhanakan untuk lomba** — semua minum dianggap on-time, semua dapat 1 poin per 100 Ml. Skip pengecekan window jadwal |

---

## Skip Kalau Waktu Mepet

Yang boleh di-skip dari prioritas terbawah:

- Custom Canvas progress ring → pakai `CircularProgressIndicator` standar Material3
- Animasi visualisasi gelas → tampilkan teks sederhana *"20 / 25 gelas"*
- Animasi slide history panel → langsung show/hide tanpa transisi
- Pembedaan on-time vs late → semua on-time dengan rate yang sama
- Reactive Flow observeTodaySessions → cukup refresh manual setelah ConfirmDrink (panggil ulang getTodaySessions)

**Yang absolut wajib jalan:** set target via slider, log sesi minum, ring update, poin masuk ke wallet, persistence (target dan sesi tetap setelah restart).
