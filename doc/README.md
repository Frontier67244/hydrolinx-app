# HydroLink — AI Agent Entry Point

> **Baca file ini dulu sebelum melakukan apapun.**
> Dokumentasi ini berisi **spesifikasi perilaku saja** — tidak ada kode. Dibuat sesuai aturan lomba: peserta tidak boleh membawa kode dalam bentuk apapun (file maupun teks di dokumentasi).

---

## Konteks Singkat

- **Apa:** Aplikasi Android untuk komunitas hidrasi — akses sumber air + tracking minum + reward poin.
- **Bahasa & UI:** Kotlin dengan Jetpack Compose.
- **Waktu:** 8 jam live coding, satu developer aktif, dibantu AI agent (Claude Code CLI Opus + Codex Desktop GPT).
- **Status folder:** project kosong. Seluruh kode dibangun **dari nol** berdasarkan dokumentasi ini.
- **Referensi kode eksternal:** **tidak ada**. Dokumen ini self-contained sesuai aturan lomba.

---

## Prioritas Modul (WAJIB DIIKUTI)

| Prioritas | Modul | Alokasi Waktu |
|---|---|---|
| **P1** | Maps | sekitar 2 jam |
| **P2** | Hydration | sekitar 2 jam |
| **P3** | Points | sekitar 1,5 jam |
| P4 | Onboarding | sekitar 30 menit |
| P5 | Notifications | sekitar 30 menit |

Sisanya = foundation (sekitar 1 jam) + polish/demo (sekitar 30 menit).

Jika waktu mepet, **fitur P1–P3 wajib jalan**. P4 dan P5 boleh disederhanakan atau di-skip.

---

## Urutan Baca Dokumentasi

AI agent dan developer harus baca dalam urutan ini agar paham konteks:

1. **README.md** (file ini) — entry point
2. **SYSTEM_OVERVIEW.md** — tujuan aplikasi, fitur utama, scope lomba
3. **ARCHITECTURE.md** — struktur folder, layering, pola arsitektur
4. **DATA_MODEL.md** — skema 4 tabel database (dalam bentuk tabel, bukan kode)
5. **CODING_CONVENTIONS.md** — aturan wajib (penamaan, pola state, larangan)
6. **EXECUTION_PLAN.md** — jadwal 8 jam per fase
7. **modules/\<nama\>.md** — baca hanya modul yang sedang dikerjakan
8. **ai_prompts/\*.txt** — template prompt reusable untuk AI agent

---

## Aturan Keras (Non-Negotiable)

Aturan berikut dipegang teguh selama pembangunan aplikasi:

1. **Tanpa library Dependency Injection.** Tidak pakai Hilt, Koin, atau sejenisnya. Instansiasi manual dari kelas Application.
2. **Tanpa network layer.** Aplikasi sepenuhnya offline. Tidak ada Retrofit, OkHttp, atau HTTP client.
3. **Tanpa XML layout.** Seratus persen Jetpack Compose.
4. **Tanpa file bernama generik.** Hindari nama seperti `Utils.kt`, `Helpers.kt`, `Common.kt`, `Manager.kt`. Nama harus deskriptif.
5. **Akses database wajib satu pintu** lewat interface repository. Jangan pernah akses DAO atau Database langsung dari ViewModel.
6. **Pola state wajib seragam** di setiap modul fitur: folder `ui/`, `state/`, `viewmodel/`, dan opsional `logic/`.
7. **Ukuran minum final** hanya tiga varian: 100 Ml, 250 Ml, dan 350 Ml. Tidak ada ukuran lain.
8. **Copy UI baku:** EXPLORE, Ml, poin, token, MINUM, REDEEM, SCAN. Lihat SYSTEM_OVERVIEW untuk detail.

---

## Efisiensi Token untuk AI Agent

Saat meminta AI agent membangun sesuatu:

- Selalu rujuk dokumentasi via path (contoh: *"baca modules/maps.md"*), jangan paste isinya.
- Batasi satu prompt ke **satu file Kotlin** saja, bukan satu fitur besar.
- Selalu sebutkan aturan keras di atas agar AI tidak menambah library terlarang.
- Jika AI mulai meng-generate sesuatu yang melanggar aturan, hentikan dan arahkan ulang ke aturan yang dilanggar.

---

## Struktur Folder Dokumentasi Ini

```
hydrolink-app/
├── README.md                  ← kamu di sini
├── SYSTEM_OVERVIEW.md
├── ARCHITECTURE.md
├── DATA_MODEL.md
├── CODING_CONVENTIONS.md
├── EXECUTION_PLAN.md
├── modules/
│   ├── maps.md                (P1 — paling detail)
│   ├── hydration.md           (P2)
│   ├── points.md              (P3)
│   ├── onboarding.md          (P4)
│   └── notifications.md       (P5)
└── ai_prompts/
    ├── generate_module.txt
    ├── generate_ui.txt
    ├── generate_viewmodel.txt
    └── debug_fix.txt
```

---

## Pre-Flight Checklist Sebelum Mulai Lomba

- [ ] Android Studio terinstall dan bisa bikin project Compose kosong
- [ ] Google Maps API Key sudah siap di file properties lokal
- [ ] Emulator atau device Android fisik siap (minimum SDK 26)
- [ ] Baca `EXECUTION_PLAN.md` end-to-end sebelum timer mulai
- [ ] Pastikan koneksi internet untuk download dependensi di awal

Jika semua centang terpenuhi, lanjut ke **Phase 0** di `EXECUTION_PLAN.md`.
