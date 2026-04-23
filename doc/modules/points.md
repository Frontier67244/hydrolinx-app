# Module: Points (P3)

> **Prioritas 3.** Target waktu: 1.5 jam.

## Tujuan Fitur

Wallet tampilkan Total Redeem, Total Poin, Token, dan Bank Poin. User bisa tap **REDEEM** untuk menukar 100 poin menjadi 1 token (batas 1 kali per hari). Setelah punya token, user bisa tap **SCAN** untuk simulasi ambil air di stasiun (batas 1 token per hari).

---

## Struktur File

```
feature/points/
├── ui/PointsScreen.kt
├── state/
│   ├── PointsUiState.kt
│   └── PointsEvent.kt
├── viewmodel/PointsViewModel.kt
└── logic/RedeemRules.kt
```

---

## Konstanta — RedeemRules (Locked)

File `feature/points/logic/RedeemRules.kt` memegang seluruh nilai aturan. Daftar konstanta:

| Nama | Nilai | Arti |
|---|---|---|
| POINTS_PER_TOKEN | 100 | Biaya poin untuk menghasilkan 1 token |
| DAILY_ACTIVE_POINT_CAP | 100 | Cap harian Total Poin aktif |
| BANK_POINT_CAP | 300 | Cap Bank Poin (penampungan overflow) |
| DAILY_REDEEM_LIMIT | 1 | Maksimal 1 redeem per hari |
| DAILY_TOKEN_USE_LIMIT | 1 | Maksimal 1 token dipakai per hari |

### Fungsi yang Harus Ada di RedeemRules

Dua fungsi murni (tidak menyentuh repository, hanya kalkulasi):

1. **Hitung Redeem.** Menerima `activePoints` dan `bankPoints` saat ini. Mengembalikan hasil perhitungan berisi `newActive`, `newBank`, dan `tokenGained`, atau mengembalikan nilai kosong jika kedua sumber tidak mencukupi.
   - **Prioritas pengurangan**: kalau `activePoints` sudah cukup (≥ 100), kurangi dari Total Poin dulu. Kalau tidak cukup tapi `bankPoints` ≥ 100, baru kurangi dari Bank. Kalau keduanya tidak cukup, kembalikan kosong.
   - Setiap kali berhasil, `tokenGained` selalu 1.

2. **Overflow ke Bank.** Menerima `incomingPoints`, `currentActive`, dan `currentBank`. Mengembalikan `newActive`, `newBank`, dan `droppedPoints`.
   - Pertama isi dulu slot kosong di Total Poin sampai cap 100. Sisa dialirkan ke Bank Poin sampai cap 300. Sisa setelah Bank penuh dianggap hilang (`droppedPoints`), bisa dibiarkan untuk lomba.

---

## State — PointsUiState

Field yang disimpan:

| Field | Tipe | Default | Sumber |
|---|---|---|---|
| totalRedeem | Int | 0 | wallet |
| activePoints | Int | 0 | wallet |
| bankPoints | Int | 0 | wallet |
| tokenCount | Int | 0 | wallet |
| lastRedeemDateIso | String? | null | wallet |
| todayIso | String | "" | dihitung di ViewModel via LocalDate saat init |
| isNotifyPanelOpen | Boolean | false | event toggle |
| isScanOpen | Boolean | false | event open/close scan |
| selectedScanVolumeMl | Int | 500 | default volume pilih di sheet |
| lastNotifyMessage | String | "" | pesan terakhir untuk panel notifikasi |

Field turunan (dikomputasi, bukan disimpan):

- **canRedeemToday**: true jika `lastRedeemDateIso` tidak sama dengan `todayIso` **dan** salah satu dari (`activePoints ≥ 100` atau `bankPoints ≥ 100`).
- **canScan**: true jika `tokenCount > 0`.

---

## Events — PointsEvent (sealed)

| Event | Payload | Keterangan |
|---|---|---|
| TapRedeem | — | User menekan tombol REDEEM |
| OpenScan | — | User menekan tombol SCAN, buka sheet |
| CloseScan | — | User dismiss scan sheet |
| SelectScanVolume | Int ml | Pilih 250, 500, atau 1000 Ml |
| ConfirmScan | — | User tekan SCAN di dalam sheet |
| ToggleNotifyPanel | — | Buka atau tutup panel notifikasi |

---

## ViewModel — Tugas

Dibangun dengan parameter `repository: LocalRepository`.

### Saat Init

1. Observe wallet via `observePointWallet()` dari repository. Setiap emisi, salin nilai ke state: `totalRedeem`, `activePoints`, `bankPoints`, `tokenCount`, `lastRedeemDateIso`.
2. Set `todayIso` dengan `LocalDate.now().toString()` agar `canRedeemToday` bisa dikomputasi dengan benar.

### Saat onEvent(TapRedeem)

1. Baca state saat ini.
2. Jika `canRedeemToday` false, keluar tanpa melakukan apa-apa.
3. Panggil fungsi **Hitung Redeem** dari RedeemRules. Kalau null, keluar.
4. Panggil repository `applyRedeem(newActive, newBank, tokenDelta=1, redeemDateIso=todayIso)`.
5. Emit `NotificationEvent.RedeemSuccess(tokenGained=1)` lewat NotificationBus.
6. Update state: buka notify panel, set `lastNotifyMessage = "Redeem berhasil • +1 token"`.

### Saat onEvent(ConfirmScan)

1. Baca state.
2. Jika `canScan` false, keluar.
3. Ambil `selectedScanVolumeMl`.
4. Panggil repository `consumeToken()`.
5. Emit `NotificationEvent.ScanSuccess(volumeMl=<volume>)`.
6. Tutup scan sheet, buka notify panel, set `lastNotifyMessage = "Scan berhasil • <volume> Ml siap diambil"`.

### Event sederhana lainnya

- **OpenScan / CloseScan**: hanya toggle `isScanOpen`.
- **SelectScanVolume**: simpan ke `selectedScanVolumeMl`.
- **ToggleNotifyPanel**: flip `isNotifyPanelOpen`.

---

## Repository Methods yang Dipakai

Ditambahkan di interface `LocalRepository`:

| Method | Tipe | Keterangan |
|---|---|---|
| observePointWallet | Flow of wallet entity | Jika DB belum punya baris, emit entity default kosong |
| applyRedeem(newActive, newBank, tokenDelta, redeemDateIso) | suspend Unit | Upsert wallet: set active & bank, tambahkan token (cap 1), increment totalRedeem, simpan tanggal |
| consumeToken | suspend Unit | Kalau tokenCount > 0, kurangi 1. Kalau sudah 0, no-op aman |
| addPoints(amount) | suspend Unit | Dipanggil dari modul Hydration. Internal menerapkan overflow Active → Bank |

### Catatan implementasi repository (dalam prosa)

- **observePointWallet**: mengobservasi singleton DAO, lalu map null menjadi entity default (semua nilai 0, tanggal null).
- **applyRedeem**: baca singleton, salin dengan nilai baru. Batasi `tokenCount` hasil akhir maksimum `DAILY_TOKEN_USE_LIMIT` (yaitu 1). Increment `totalRedeem` +1. Upsert.
- **consumeToken**: baca singleton. Kalau null atau `tokenCount ≤ 0`, langsung keluar. Kalau tidak, upsert dengan `tokenCount - 1`.
- **addPoints**: baca singleton. Panggil fungsi **Overflow ke Bank** dari RedeemRules dengan `incomingPoints = amount`. Upsert dengan `newActive` dan `newBank` hasil fungsi. Abaikan `droppedPoints`.

---

## UI — PointsScreen

Instansiasi ViewModel dengan pola factory manual (lihat CODING_CONVENTIONS.md §4).

### Layout

```
┌──────────────────────────────────┐
│  REDEEM                          │  ← title
│                                  │
│  ┌────────────────────────────┐  │
│  │  Wallet Card               │  │
│  │  Total Redeem: 0           │  │
│  │                            │  │
│  │  Total Poin  Token  Bank   │  │
│  │  100/100     0/1    0/300  │  │
│  └────────────────────────────┘  │
│                                  │
│  [Notify]  [REDEEM]  [SCAN]      │  ← action row
│                                  │
└──────────────────────────────────┘
```

- **Wallet Card**: Card ber-border cyan. Baris pertama `Total Redeem: <n>`. Baris kedua tiga kolom sejajar (SpaceBetween): Total Poin `x/100`, Token `x/1`, Bank Poin `x/300`.
- **Action Row**: tiga tombol horizontal dengan jarak 12dp.
  - **Notify**: tombol outlined. Toggle panel notifikasi.
  - **REDEEM**: tombol filled. Disable kalau `canRedeemToday` false.
  - **SCAN**: tombol filled. Disable kalau `canScan` false.

### Scan Bottom Sheet

Muncul ketika `isScanOpen` true. Komponen Material3 `ModalBottomSheet`, dismiss memanggil `CloseScan`.

```
┌──────────────────────────────────┐
│  Pilih volume                    │
│  [ 250 Ml ] [ 500 Ml ] [1000 Ml] │  ← FilterChip, highlight yang dipilih
│                                  │
│  ┌──────────────────────────┐    │
│  │                          │    │
│  │   Kamera QR (simulasi)   │    │  ← Box hitam, teks putih di center
│  │                          │    │
│  └──────────────────────────┘    │
│                                  │
│  [          SCAN           ]     │  ← full width, disable kalau no token
└──────────────────────────────────┘
```

- Chip volume: tiga pilihan 250, 500, 1000 Ml (tidak ada angka lain). Klik memanggil `SelectScanVolume(ml)`.
- Kotak kamera: hanya placeholder visual. Tidak ada integrasi CameraX.
- Tombol SCAN di dalam sheet: panggil `ConfirmScan`.

### Notify Panel

Muncul ketika `isNotifyPanelOpen` true. Bisa berupa card sederhana yang overlay di atas layar, atau Snackbar. Tampilkan `lastNotifyMessage`, plus tombol close yang memanggil `ToggleNotifyPanel`.

---

## Happy Path Demo

1. Buka Points tab dalam kondisi baru: wallet 0 di semua stats.
2. (Prasyarat) User sudah melakukan logging minum di modul Hydration sampai `activePoints` mencapai 100.
3. Tombol REDEEM enabled. User tap REDEEM → wallet jadi: activePoints 0, tokenCount 1, totalRedeem 1.
4. Panel notifikasi muncul: "Redeem berhasil • +1 token".
5. Tombol SCAN sekarang enabled. User tap SCAN → scan sheet muncul.
6. User pilih chip 500 Ml → tekan tombol SCAN di dalam sheet.
7. tokenCount jadi 0, sheet tertutup, panel notifikasi: "Scan berhasil • 500 Ml siap diambil".
8. Baik REDEEM maupun SCAN kini disabled sampai hari berikutnya (REDEEM karena `lastRedeemDateIso == todayIso`, SCAN karena `tokenCount == 0`).

---

## Edge Cases

| Kasus | Penanganan |
|---|---|
| Poin < 100 tapi user tap REDEEM | Tombol sudah disabled via `canRedeemToday`. Tidak perlu dialog error. |
| Redeem 2x di hari sama | `lastRedeemDateIso == todayIso` → tombol disabled. |
| Token 0 tapi user tap SCAN | Tombol disabled via `canScan`. |
| `addPoints` menyebabkan overflow | Fungsi Overflow ke Bank handle. Sisa setelah Bank penuh di-drop (acceptable untuk lomba). |
| Active dan Bank keduanya < 100 | Fungsi Hitung Redeem kembalikan null → handler ViewModel no-op. |
| Pergantian hari saat app running | `todayIso` dihitung saat init. Untuk lomba, cukup. Kalau mau presisi, hitung ulang saat Flow wallet baru emit. |
| Wallet entity belum ada di DB | Repository observer map null → default entity semua 0. |

---

## Strategi Mock

- **Kamera QR**: **Skip CameraX sepenuhnya.** Kotak hitam sebagai placeholder visual. Tombol SCAN langsung `consumeToken()`.
- **Validasi backend**: tidak ada. Scan berhasil = token berkurang 1, selesai.
- **Volume pilihan di scan sheet**: tiga nilai hardcoded 250, 500, 1000 Ml (berbeda dengan ukuran minum di Hydration yang 100/250/350).

---

## Skip Kalau Waktu Mepet

- CameraX integration (pakai placeholder saja)
- Daily reset cron / WorkManager (observe wallet + date comparison sudah cukup)
- Visualisasi animasi bank overflow
- Animasi wallet card
- Animasi shift panel notifikasi (fade in/out sederhana cukup)

**Prioritas absolut**: wallet render dengan nilai yang benar, REDEEM mengurangi poin dan menambah token, SCAN consume token, batas harian ter-enforce.
