# DATA MODEL

> Skema empat tabel database lokal HydroLink. Direpresentasikan dalam bentuk tabel deskriptif. Saat AI agent diminta generate kode entity dan DAO, dia menerjemahkan tabel ini ke struktur Room.

---

## Tabel 1: water_locations

Tujuan: menyimpan daftar stasiun air komunitas. Read-only setelah seeding.

| Kolom | Tipe Konseptual | Wajib | Deskripsi |
|---|---|---|---|
| id | string | Ya (Primary Key) | Identifier unik stasiun. Format bebas, contoh `loc-001`, `loc-002`, dst. |
| name | string | Ya | Nama tampilan stasiun. Contoh: *"Pos Air Cikini"*, *"Stasiun Air Salemba"* |
| latitude | angka desimal | Ya | Koordinat lintang |
| longitude | angka desimal | Ya | Koordinat bujur |
| address | string | Ya | Alamat lengkap untuk ditampilkan di detail sheet dan disalin ke clipboard |
| isOpen | boolean | Ya | Status buka atau tutup. Untuk demo bisa di-set acak antar entri |
| hasCapacity | boolean | Ya | Apakah stasiun masih punya kapasitas air |

**Catatan operasional:**
- Diisi sekali saja saat aplikasi pertama kali jalan, oleh `DummyLocationSeeder`
- Sekitar 20 entri dummy, sebar di sekitar koordinat default user (Jakarta, sekitar -6.20, 106.81)
- Tidak ada update atau delete dari user. Tidak ada CRUD UI

---

## Tabel 2: hydration_sessions

Tujuan: log append-only setiap sesi minum user.

| Kolom | Tipe Konseptual | Wajib | Deskripsi |
|---|---|---|---|
| id | bilangan bulat | Ya (Primary Key, auto-generate) | Nomor urut sesi |
| amountMl | bilangan bulat | Ya | Jumlah Ml yang diminum dalam sesi ini. Hanya boleh 100, 250, atau 350 |
| pointsAwarded | bilangan bulat | Ya | Jumlah poin yang diberikan untuk sesi ini. Dihitung sebagai `amountMl / 100` |
| sessionTimestampEpochMillis | bilangan bulat 64-bit | Ya | Waktu sesi dalam milidetik epoch (System.currentTimeMillis) |

**Catatan operasional:**
- Hanya operasi insert. Tidak ada update atau delete (untuk lomba)
- Query yang dibutuhkan: ambil semua sesi yang timestamp-nya di hari ini (hari sekarang dimulai dari pukul 00:00 lokal)
- Diobservasi via Flow oleh ViewModel modul Hydration agar UI auto-update saat ada insert baru

---

## Tabel 3: point_wallet

Tujuan: menyimpan state wallet poin user. **Single row pattern** — selalu hanya satu baris dengan id 0.

| Kolom | Tipe Konseptual | Wajib | Deskripsi |
|---|---|---|---|
| id | bilangan bulat | Ya (Primary Key, default 0) | Selalu 0. Pola single-row |
| totalRedeem | bilangan bulat | Ya | Jumlah total redeem yang pernah dilakukan user (lifetime counter) |
| activePoints | bilangan bulat | Ya | Total Poin aktif hari ini. Cap 100 |
| bankPoints | bilangan bulat | Ya | Bank Poin (penampungan overflow). Cap 300 |
| tokenCount | bilangan bulat | Ya | Jumlah token siap pakai. Cap 1 (sesuai aturan 1 token per hari) |
| lastRedeemDateIso | string atau null | Tidak | Tanggal redeem terakhir dalam format ISO `YYYY-MM-DD`. Null jika belum pernah redeem. Dipakai untuk mengecek batas harian |

**Catatan operasional:**
- Diobservasi via Flow oleh ViewModel modul Points
- Operasi yang dipakai: upsert (replace seluruh row dengan nilai baru)
- Saat aplikasi pertama jalan, baris ini bisa belum ada. Repository harus handle kasus null dengan return wallet default kosong

---

## Tabel 4: user_profiles

Tujuan: menyimpan target hidrasi user. **Single row pattern**.

| Kolom | Tipe Konseptual | Wajib | Deskripsi |
|---|---|---|---|
| id | bilangan bulat | Ya (Primary Key, default 0) | Selalu 0 |
| targetMl | bilangan bulat | Ya | Target hidrasi harian dalam Ml. Rentang 1500 sampai 2500. Default 0 berarti belum di-set |
| displayName | string atau null | Tidak | Nama tampilan opsional. Untuk lomba bisa diabaikan |

**Catatan operasional:**
- Saat user pertama kali buka aplikasi dan belum set target, modul Hydration menampilkan layar *Started* dengan tombol LET'S GO
- Setelah user set target lewat slider dan tap SAVE, baris ini di-upsert dengan nilai target
- Modul Hydration mengecek nilai targetMl saat init untuk menentukan langkah awal (Started kalau 0, Main kalau sudah ada nilai)

---

## Method Repository yang Wajib Ada

`LocalRepository` adalah interface tunggal yang menjadi pintu akses ke seluruh tabel. Berikut method yang harus tersedia (dikelompokkan per fitur):

### Untuk Modul Maps

- Mengambil seluruh stasiun air dari database (suspend, return list)
- Memastikan seeding sudah dilakukan saat aplikasi pertama jalan (boleh di Application init)

### Untuk Modul Hydration

- Mengambil profil user (suspend, return profil atau null)
- Set target Ml user (suspend, upsert profile)
- Mengambil sesi minum hari ini (suspend, return list)
- Mengobservasi sesi minum hari ini secara reaktif (return Flow of list)
- Insert sesi minum baru (suspend)

### Untuk Modul Points

- Mengobservasi wallet poin secara reaktif (return Flow of wallet entity, default kosong jika belum ada)
- Menambah poin ke wallet (suspend, internal pakai aturan overflow ke Bank Poin)
- Apply hasil redeem (suspend, update activePoints, bankPoints, tokenCount, totalRedeem, dan lastRedeemDateIso sekaligus)
- Konsumsi satu token saat scan sukses (suspend, kurangi tokenCount)

---

## Aturan Konsistensi

Aturan berikut harus dijaga di level repository, **bukan** di ViewModel:

1. Saat menambah poin lewat operasi *add points*, repository wajib menerapkan aturan overflow:
   - Jika activePoints belum mencapai cap 100, masukkan dulu ke activePoints sampai penuh
   - Sisanya dialirkan ke bankPoints sampai cap 300
   - Sisa setelah bank penuh dianggap hilang (acceptable untuk lomba)

2. Saat apply redeem, tokenCount tidak boleh melebihi 1 (cap harian). Jika sudah 1, redeem ditolak di level ViewModel sebelum sampai repository.

3. Saat consume token, jika tokenCount sudah 0 maka operasi tidak melakukan apapun (no-op aman).

4. Tanggal di lastRedeemDateIso disimpan sebagai string ISO `YYYY-MM-DD`. Pengecekan batas harian dilakukan dengan membandingkan string ini dengan tanggal hari ini (komputasi tanggal dilakukan di ViewModel).

---

## Perbedaan Operasi Suspend vs Flow

Untuk menghindari kebingungan saat generate kode:

| Kebutuhan | Pakai |
|---|---|
| Ambil data sekali (init load, lookup) | Suspend function yang return value langsung |
| Observe perubahan data secara terus-menerus (UI yang harus auto-update) | Function yang return Flow |
| Mengubah data (insert, update, delete, upsert) | Suspend function tanpa return value (Unit) |

---

## Panduan Saat Generate Kode dari Tabel Ini

AI agent yang membaca dokumen ini untuk generate entity Room harus:

1. Buat satu file entity per tabel di folder `data/local/entity/`
2. Buat satu DAO per entity di folder `data/local/dao/`
3. Buat satu kelas Database tunggal di `data/local/db/HydrolinkDatabase.kt` yang mendaftarkan keempat entity dan keempat DAO
4. Untuk *single row pattern* (point_wallet dan user_profiles), DAO punya method untuk get singleton (return entity atau null) dan method untuk observe singleton (return Flow). Method upsert untuk update
5. Tipe Boolean disimpan sebagai INTEGER di SQLite (Room handle otomatis)
6. Tidak perlu type converter custom untuk lomba ini
