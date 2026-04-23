# SYSTEM OVERVIEW

## Apa itu HydroLink?

Aplikasi Android komunitas yang menyatukan tiga pilar:

1. **Akses sumber air** — peta lokasi stasiun air komunitas terdekat
2. **Tracking hidrasi** — log minum harian dengan target personal
3. **Reward** — poin dari minum tepat waktu, ditukar jadi token, token dipakai scan QR di stasiun

Ketiga modul saling terhubung: user pergi minum → dapat poin → poin jadi token → token dipakai untuk akses air di stasiun.

---

## Lima Modul Utama

### 1. Maps (Prioritas 1)

Tujuan: membantu user menemukan stasiun air komunitas terdekat dari posisinya.

Yang dilakukan modul ini:
- Menampilkan peta dengan posisi user (titik biru) di tengah
- Tombol EXPLORE memicu pencarian dengan radius mengembang bertahap
- Saat ditemukan stasiun di dalam radius, muncul pin di peta
- Pin bisa di-tap untuk popup ringkas (nama, status buka/tutup, kapasitas, jarak, estimasi waktu per moda transportasi)
- Detail sheet menampilkan alamat lengkap, tombol salin alamat, dan tombol route yang membuka aplikasi Google Maps eksternal
- Data stasiun air berasal dari sekitar dua puluh entri dummy yang di-seed saat aplikasi pertama kali jalan

### 2. Hydration (Prioritas 2)

Tujuan: membantu user mencatat minum harian dan mencapai target hidrasi.

Yang dilakukan modul ini:
- User pertama kali mengatur target minum harian melalui slider rentang 1500 sampai 2500 mililiter (kelipatan 100)
- Setelah target di-set, user masuk ke layar utama dengan progress ring lingkaran
- User log sesi minum dengan memilih ukuran 100, 250, atau 350 Ml lewat bottom sheet
- Setiap sesi tercatat dengan timestamp dan otomatis menambah poin (1 poin per 100 Ml)
- History panel menampilkan list sesi minum hari ini
- Notify panel menampilkan feedback poin per sesi

### 3. Points (Prioritas 3)

Tujuan: memberi reward atas konsistensi minum dan menjadi jembatan ke stasiun air.

Yang dilakukan modul ini:
- Wallet menampilkan empat angka: Total Redeem, Total Poin (kapasitas 100 per hari), Token (kapasitas 1 per hari), Bank Poin (kapasitas 300)
- Poin masuk otomatis dari modul Hydration. Jika Total Poin sudah penuh, sisanya overflow ke Bank Poin
- Tombol REDEEM menukar 100 poin jadi 1 token, dibatasi 1 kali per hari. Prioritas pengambilan: Total Poin dulu, fallback ke Bank Poin
- Tombol SCAN membuka layar QR (untuk lomba boleh placeholder kamera) dengan pilihan volume 250, 500, atau 1000 Ml
- Setiap scan sukses mengurangi token sebanyak 1

### 4. Onboarding (Prioritas 4)

Tujuan: memperkenalkan aplikasi saat first launch.

Yang dilakukan modul ini:
- Untuk lomba 8 jam, cukup satu halaman splash dengan logo, tagline, dan tombol *Get Started*
- Saat user tap tombol, navigasi pindah ke layar utama dengan bottom navigation
- Versi ideal punya enam halaman (identity, login, value Maps, value Hydration, value Points, start) — boleh di-skip kalau waktu mepet

### 5. Notifications (Prioritas 5)

Tujuan: memberi feedback in-app saat user melakukan aksi penting.

Yang dilakukan modul ini:
- **In-app event bus**: panel overlay yang muncul setelah user log minum, redeem token, atau scan sukses. Format pesan: jenis aksi, nilai, dan waktu (contoh: *"+2 poin • Tepat waktu (09.30)"*)
- **Guidance panel**: header kontekstual yang muncul di layar Maps, Hydration, atau Points untuk mengarahkan user ke aksi berikutnya. Contoh: *"Belum minum hari ini? Yuk mulai minum sekarang."*
- **Push notification terjadwal**: untuk lomba ini hanya stub. Tidak perlu implementasi WorkManager. Jadwal referensi: Maps pukul 06:00, Hydration di slot siang/sore/malam, Points sekitar pukul 20:00

---

## Alur User Sederhana (Happy Path Demo)

Skenario yang akan diperagakan saat demo:

1. **Launch aplikasi** → splash sebentar → masuk ke layar utama dengan bottom navigation tiga tab (Maps, Hydration, Points)
2. **Tab Maps**: user grant izin lokasi → peta render → tap EXPLORE → lingkaran radius mengembang dari 200m, naik bertahap → pin stasiun muncul → tap pin → detail sheet → tap ROUTE → aplikasi Google Maps eksternal terbuka dengan navigasi
3. **Tab Hydration**: tap LET'S GO → set slider ke 2000 Ml → SAVE → masuk layar utama dengan ring progress 0% → tap tombol minum di tengah → bottom sheet muncul → pilih 250 Ml → MINUM → ring update jadi sekitar 12% → notify panel muncul *"+2 poin • (jam saat ini)"*
4. **Ulangi minum** sampai Total Poin mencapai 100
5. **Tab Points**: wallet menunjukkan Total Poin 100/100 → tap REDEEM → token bertambah jadi 1, Total Poin jadi 0, Total Redeem bertambah → tap SCAN → bottom sheet pilih volume 500 → tap tombol SCAN → token berkurang jadi 0 → notify panel *"Scan berhasil • 500 Ml siap diambil"*

Jika empat langkah di atas berjalan mulus, lomba dianggap sukses.

---

## Scope Lomba 8 Jam

### Yang Wajib Real (Berfungsi Penuh)

- Navigasi antar tiga tab utama
- Persistence database lokal dengan empat tabel (lihat DATA_MODEL.md)
- Maps: state machine explore, ekspansi radius, pin/detail, intent ke Google Maps
- Hydration: set target, log sesi, progress ring
- Points: redeem flow, wallet state, batas harian dipaksakan
- In-app notification dengan event bus
- Guidance panel minimal di satu layar

### Yang Boleh Mock atau Stub

- Onboarding multi-page → cukup splash satu halaman
- Login Google → langsung anggap user guest
- Push notification → stub class kosong, tidak benar-benar terjadwal
- QR scanner kamera → tombol *Simulate Scan* yang langsung memproses
- Stasiun air → seluruhnya dummy yang di-hardcode (sekitar 20 entri)
- User profile → satu baris default, tidak ada login flow

### Yang Skip Total

- Backend, server, API
- Authentication asli
- Dark mode
- Animasi kompleks yang tidak kritikal untuk demo
- Unit test dan UI test

---

## Istilah Baku (Copy UI Final)

Konsistensi istilah ini wajib di seluruh aplikasi:

| Istilah | Pemakaian |
|---|---|
| EXPLORE | Tombol aksi utama di Maps. Tulis huruf besar semua. Bukan "SEARCH" atau "CARI" |
| Ml | Satuan volume air. Penulisan: huruf M besar, l kecil. Bukan "ml" atau "ML" |
| poin | Reward harian dari minum. Penulisan huruf kecil. Bukan "Poin" atau "POIN" |
| token | Hasil tukar 100 poin. Penulisan huruf kecil. Bukan "Token" atau "TOKEN" |
| MINUM | Tombol konfirmasi log hidrasi. Huruf besar semua |
| REDEEM | Tombol tukar poin jadi token. Huruf besar semua |
| SCAN | Tombol scan QR di stasiun. Huruf besar semua |

---

## Aturan Bisnis Final (Locked)

Aturan ini diambil dari spesifikasi master dan tidak bisa diubah:

- Ukuran minum per sesi: hanya 100, 250, atau 350 Ml
- Konversi: 100 poin sama dengan 1 token
- Batas redeem: maksimum 1 kali per hari per user
- Batas penggunaan token: 1 token untuk 1 kali scan QR per hari
- Cap Total Poin: 100 per hari (sisa overflow ke Bank Poin)
- Cap Bank Poin: 300 (kalau Bank penuh, sisanya hilang)
- Target hidrasi: rentang 1500 sampai 2500 Ml dengan step 100
- Default target: 2000 Ml
- 1 poin diberikan per 100 Ml yang diminum

---

## Persona User

User tipikal aplikasi ini adalah anggota komunitas aktif (mahasiswa atau pekerja urban) yang ingin akses air mudah, kebiasaan hidrasi terbangun pelan-pelan, dan reward yang terlihat sebagai motivasi. Pain point utama: lupa minum, dan bingung dengan sistem yang ribet.

Karena itu desain dibuat **clean, sparse, dengan whitespace banyak** dan tidak banyak step. Tap tiga kali maksimum untuk aksi inti.
