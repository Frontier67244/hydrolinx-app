# Module: Onboarding (P4)

> **Prioritas 4.** Target waktu: 30 menit (termasuk fallback). Boleh di-skip total kalau waktu mepet.

## Tujuan Fitur

Intro ringan saat first launch: sambutan singkat lalu masuk ke halaman utama ber-bottom-nav. Karena lomba hanya 8 jam, modul ini sengaja dibuat minimal.

---

## Opsi Implementasi — Pilih Satu

### Opsi A — Splash Satu Halaman (Rekomendasi)

Satu layar statis. Elemen:

- Background: warna cyan brand dengan alpha rendah (sekitar 10%).
- Icon tetes air di tengah atas, ukuran sekitar 96dp, warna cyan brand.
- Teks judul **HydroLink** dengan tipografi display.
- Tagline "Hidrasi komunitas dalam genggaman" tipografi body.
- Tombol full width **GET STARTED** di bawah, tekan untuk pindah ke halaman utama.

Target waktu: 15 menit. Tidak perlu animasi, tidak perlu pager, tidak perlu ilustrasi kustom.

### Opsi B — Skip Total

Langsung arahkan `startDestination` ke route halaman utama. Tidak ada file `OnboardingScreen.kt` sama sekali. Ini pilihan kalau Maps/Hydration/Points sudah makan lebih dari 6 jam.

---

## Navigation Wiring

### AppRoute (Sealed Class)

Didefinisikan di `core/navigation/AppRoute.kt`. Lima route:

| Route | String |
|---|---|
| Onboarding | "onboarding" |
| MainHome | "main_home" (container bottom nav) |
| Maps | "maps" |
| Hydration | "hydration" |
| Points | "points" |

### HydrolinkNavHost (Outer NavHost)

Di `core/navigation/HydrolinkNavHost.kt`. Memegang dua destination utama:

1. **Onboarding**: render layar OnboardingScreen. Callback `onFinish` navigate ke MainHome dan pop Onboarding dari back stack (inclusive) supaya back button tidak balik ke splash.
2. **MainHome**: render `MainHomeScreen()` yang berisi bottom nav.

`startDestination`:
- Opsi A: mulai dari Onboarding.
- Opsi B: mulai dari MainHome langsung.

### MainHomeScreen (Inner NavHost + Bottom Nav)

Scaffold dengan `bottomBar` berisi `NavigationBar`. Tiga tab:

| Tab | Route | Icon | Label |
|---|---|---|---|
| Maps | maps | LocationOn | Maps |
| Hydration | hydration | LocalDrink | Hydrate |
| Points | points | Stars | Points |

Di body Scaffold pasang `NavHost` inner dengan controller terpisah. `startDestination` default Maps. Tiga composable destination: `MapsScreen`, `HydrationScreen`, `PointsScreen`.

Catatan: track tab aktif dengan `currentBackStackEntryAsState` lalu bandingkan route untuk menentukan state `selected` di NavigationBarItem.

---

## Strategi Mock

- **Login / Google Sign-In**: skip total. Seluruh app berjalan sebagai guest.
- **First launch detection**: tidak perlu. Setiap kali app dibuka dari cold start masuk Onboarding (Opsi A), atau langsung MainHome (Opsi B). Tidak perlu DataStore flag.
- **Analytics / tracking**: skip.

---

## Skip

- Pager 3–6 halaman value proposition
- Swipe gesture antar halaman
- Login flow apapun (Google, email, phone)
- Animasi transisi kustom
- DataStore untuk persist "sudah onboarding"

**Rekomendasi akhir**: Opsi A satu halaman statis. Selesai dalam 15 menit, cukup secara estetika untuk demo lomba.
