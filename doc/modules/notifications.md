# Module: Notifications (P5)

> **Prioritas 5.** Target waktu: 30 menit. Hanya in-app. Push notification di-stub.

## Dua Komponen

1. **In-App Event Bus** (`NotificationBus`) berbasis SharedFlow. Dipakai untuk menampilkan overlay feedback setelah user action (drink, redeem, scan).
2. **Guidance Panel** — sub-header persistent yang menampilkan pesan contextual untuk menuntun user ke action selanjutnya.

---

## 1. NotificationBus (In-App Event Bus)

### File

- `core/notification/NotificationEvent.kt` — sealed interface yang mendefinisikan tipe event.
- `core/notification/NotificationBus.kt` — singleton object yang memegang SharedFlow dan menyediakan fungsi `emit`.

### NotificationEvent — Daftar Varian

| Varian | Payload | Kapan di-emit |
|---|---|---|
| HydrationLogged | points: Int, timestampMs: Long | Setelah insert sesi minum berhasil |
| RedeemSuccess | tokenGained: Int | Setelah redeem berhasil di Points VM |
| ScanSuccess | volumeMl: Int | Setelah consume token sukses |
| MapsFound | locationName: String | (Opsional) saat hasil scan radius pertama kali ketemu |
| GenericInfo | message: String | Fallback pesan bebas |

### Struktur NotificationBus

Object singleton (bukan class). Internal memegang `MutableSharedFlow<NotificationEvent>` dengan konfigurasi:

- `replay = 0` (tidak replay event ke subscriber baru; event lewat, ya lewat).
- `extraBufferCapacity = 16` (buffer kecil agar `tryEmit` tidak drop saat burst).

Expose `events` sebagai `SharedFlow<NotificationEvent>` read-only (via `asSharedFlow()`).

Fungsi publik `emit(event)` memanggil `tryEmit` — non-suspend supaya bisa dipanggil dari manapun, termasuk ViewModel tanpa scope.

### Cara Pakai di Screen

Di composable root tiap modul (Maps, Hydration, Points), buka satu `LaunchedEffect(Unit)` yang collect `NotificationBus.events`. Di dalam collector, pattern match tiap event dan isi state lokal (`toastMessage: String?`) dengan pesan yang mau ditampilkan.

Contoh mapping pesan:

| Event | Teks yang ditampilkan |
|---|---|
| HydrationLogged | "+{points} poin • Tepat waktu ({HH.mm})" |
| RedeemSuccess | "Redeem berhasil • +{tokenGained} token" |
| ScanSuccess | "Scan berhasil • {volumeMl} Ml" |

Saat `toastMessage` tidak null, render `NotificationShiftingPanel` di atas content.

### Format Waktu untuk HydrationLogged

Dari `timestampMs` (Long epoch millis), hasilkan jam lokal format `HH.mm` (pakai titik, bukan titik dua, konsisten dengan UI). Dikerjakan dengan `DateTimeFormatter` pattern `"HH.mm"` di zona sistem, di-apply ke `Instant.ofEpochMilli(ms)`.

### NotificationShiftingPanel (Overlay)

Komponen visual untuk menampilkan pesan. Spesifikasi:

- `Surface` full width dengan padding 16dp di sekeliling.
- Warna background primaryContainer, corner radius 16dp, border 1.25dp warna cyan brand.
- Row di dalam berisi: Icon `Notifications` cyan, spacer 12dp, lalu Text pesan.
- `LaunchedEffect(message)`: delay 3 detik lalu panggil `onDismiss` — panel otomatis hilang setelah 3s.
- Posisi: overlay di atas content, biasanya align `TopCenter` atau `BottomCenter` dalam sebuah Box.

---

## 2. Guidance Panel

Sub-header persistent di home tiap modul. Berisi tiga baris teks untuk menuntun user.

### GuidanceMessage — 5 Varian (Locked)

Didefinisikan sebagai sealed class di `core/guidance/GuidanceMessage.kt`. Setiap varian memegang tiga string: `greeting`, `question`, `action`.

| Varian | Greeting | Question | Action |
|---|---|---|---|
| MapsHome | Halo, Healthy! | Sudah tahu sumber air terdekat? | Tap EXPLORE untuk cari. |
| HydrationHomeEmpty | Halo, Healthy! | Belum minum hari ini? | Yuk mulai minum sekarang. |
| HydrationHomeProgress | Halo, Healthy! | Sudah minum cukup? | Lanjutkan sampai target. |
| PointsReady | Halo, Healthy! | Poinmu sudah cukup? | Redeem sekarang. |
| PointsLow | Halo, Healthy! | Masih kurang poin? | Minum lagi untuk dapat poin. |

Untuk lomba **5 varian ini cukup**. Jangan tambah varian baru.

### GuidancePanel Composable

Komponen visual:

- `Card` full width, corner radius 16dp, border 1.25dp cyan brand.
- Column di dalam, padding 16dp:
  - Teks greeting, tipografi labelLarge.
  - Teks question, tipografi titleMedium.
  - Teks action, tipografi bodyMedium, warna cyan brand.

### Pemilihan Varian di ViewModel

Tiap ViewModel punya computed property `guidance` yang memilih varian berdasarkan state saat ini.

Contoh Hydration: kalau `totalMlToday == 0` pilih `HydrationHomeEmpty`, selainnya `HydrationHomeProgress`.

Contoh Points: kalau `canRedeemToday` true pilih `PointsReady`, selainnya `PointsLow`.

Maps hanya punya satu varian `MapsHome` — pakai itu konstan di home.

---

## 3. Push Notification — STUB

Lomba 8 jam **skip implementasi**, tapi struktur file tetap ada supaya arsitektur konsisten dan mudah di-backfill setelah lomba.

### File Stub

`core/notification/NotificationScheduler.kt` — object dengan fungsi `scheduleAll()` yang kosong. Tambahkan komentar TODO: "post-lomba pakai WorkManager PeriodicWorkRequest untuk Maps 06:00, Hydration siang/sore/malam, Points 20:00".

### Jadwal Referensi (Untuk Post-Lomba)

| Modul | Waktu | Copy |
|---|---|---|
| Maps | 06:00 | Hari ini kamu butuh air. Temukan sumber terdekat sekarang. |
| Hydration (siang) | 12:00 | Sudah waktunya minum. Jangan lewatkan jadwalmu. |
| Hydration (sore) | 15:00 | Setengah hari terlewati — cek progress hidrasimu. |
| Hydration (malam) | 19:00 | Target belum tercapai? Masih ada waktu. |
| Points | 20:00 | Poinmu sudah cukup. Redeem poin ke token sekarang. |

---

## Strategi Mock

- **In-app bus**: real, SharedFlow jalan penuh.
- **Push scheduler**: stub, tanpa WorkManager.
- **Guidance**: real, 5 varian statik yang dipilih berdasarkan state.

---

## Skip

- WorkManager scheduling
- Notification channel + importance config (Android 8+)
- Deep link dari notification ke layar tertentu
- Penambahan varian guidance di atas 5
- Lokalisasi / i18n guidance copy

**Prioritas absolut**: `NotificationBus.emit()` dari ViewModel terobservasi oleh screen, panel overlay muncul 3 detik, lalu hilang. Push dan WorkManager sepenuhnya stub.
