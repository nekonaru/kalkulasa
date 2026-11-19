<div align="center">
  <img src="assets/kalkulasa-mark.png" alt="Logo Kalkulasa" width="120" />
  <h1>Kalkulasa: Stok dan Kas untuk UMKM</h1>
  <p><b>Urusi stok dan kas harian warung kamu dengan kasir satu ketuk,<br/>
  modal per barang otomatis, dan laporan untung yang simpel.</b></p>

  <a href="https://github.com/nekonaru/kalkulasa/releases"><img src="https://img.shields.io/github/v/release/nekonaru/kalkulasa?style=flat-square&color=blue" alt="Release" /></a>
  <a href="https://github.com/nekonaru/kalkulasa/actions/workflows/ci.yml"><img src="https://github.com/nekonaru/kalkulasa/actions/workflows/ci.yml/badge.svg" alt="CI" /></a>
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.0" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License MIT" />
  <img src="https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square" alt="Status Active" />
</div>

<br />

**Bahasa Indonesia** | [English](README.en.md)

> [!NOTE]
> Kalkulasa bekerja sepenuhnya offline. Data kamu tidak pernah keluar dari ponsel kecuali kamu sendiri yang mengekspor backup.

---

## Daftar Isi

- [Tentang](#tentang)
- [Fitur](#fitur)
- [Kebutuhan](#kebutuhan)
- [Instalasi](#instalasi)
- [Cara Pakai](#cara-pakai)
- [Bahasa Tampilan](#bahasa-tampilan)
- [Struktur Proyek](#struktur-proyek)
- [Pengembangan](#pengembangan)
- [Pengujian](#pengujian)
- [Keamanan](#keamanan)
- [Continuous Integration](#continuous-integration)
- [FAQ](#faq)
- [Penulis](#penulis)
- [Disclaimer](#disclaimer)
- [Lisensi](#lisensi)

---

## Tentang

**Kalkulasa** adalah aplikasi Android gratis dan offline-first untuk UMKM dan warung: stok masuk, stok keluar, kasir satu ketuk, dan laporan untung dengan perhitungan modal per barang (HPP) otomatis. Data usaha kamu tersimpan di ponsel dalam database lokal yang terenkripsi. Tanpa akun, tanpa iklan, tanpa internet.

> _"Usaha kecil, semua urusan beres."_

---

## Fitur

### Kasir

| Fitur | Deskripsi |
|---------|-------------|
| **Kasir satu ketuk** | Ketuk produk untuk masukkan ke keranjang, atur jumlah, beri diskon dalam rupiah, pilih tunai / transfer / QRIS / Hutang sebagai label |
| **Produk pin di kasir** | Sematkan produk yang paling sering dibeli agar selalu muncul di atas |
| **Kembalian otomatis** | Saat bayar Tunai, isi uang diterima dan Kalkulasa hitung kembalian |
| **Riwayat penjualan** | Semua transaksi per periode, omzet per periode, bagikan struk dan batal transaksi dari satu layar |
| **Stok otomatis berkurang** | Menyimpan penjualan mengurangi stok dan mencatat modal per barang terjual |
| **Hutang pelanggan** | Jualan dengan metode Hutang, isi nama pelanggan, utang otomatis masuk buku Piutang |
| **Poin loyalti** | Isi nama pelanggan saat penjualan, dapat poin otomatis (1 poin tiap Rp 1.000) |
| **Struk digital** | Bagikan struk lewat WhatsApp setelah penjualan tersimpan |
| **Batalkan dengan restock** | Membatalkan transaksi mengembalikan stok dan riwayat pergerakan |

### Stok

| Fitur | Deskripsi |
|---------|-------------|
| **Manajemen produk** | Tambah, ubah, cari, arsipkan dan kembalikan produk dengan satuan, barcode dan stok minimum |
| **Stok masuk dengan harga** | Catat pembelian per produk; modal per barang dihitung ulang otomatis |
| **Penyesuaian stok** | Isi stok hasil hitung fisik dengan alasan (rusak, hilang, kedaluwarsa, opname) |
| **Riwayat pergerakan** | Semua stok masuk, keluar, penyesuaian dan retur tercatat |
| **Peringatan stok menipis** | Beranda dan Stok menandai produk yang stoknya di batas minimum atau kurang |
| **Manajemen kategori** | Tambah, ubah, dan hapus kategori produk dari tab Stok |
| **Impor produk dari CSV** | Impor ratusan produk dari file teks; mendukung pemisah koma/titik koma, angka Indonesia, dan judul kolom opsional |

### Keuangan

| Fitur | Deskripsi |
|---------|-------------|
| **Saldo kas** | Sekilas posisi kas, dihitung dari modal, penjualan, pembelian, pengeluaran dan prive |
| **Pencatatan modal** | Modal awal dan tambahan modal tercatat sebagai entri terpisah |
| **Prive** | Pengambilan uang pribadi pemilik dicatat terpisah agar laba usaha tetap bersih |
| **Pengeluaran operasional** | Pengeluaran per kategori (Listrik, Sewa, Gaji, Transportasi, Lainnya); penjualan dan pembelian stok tercatat otomatis |
| **Riwayat pembelian** | Setiap stok masuk tercatat di riwayat keuangan dengan label pemasoknya |
| **Pemasok** | Total belanja, jumlah transaksi dan tanggal pembelian terakhir per pemasok |

### Pelanggan dan poin

| Fitur | Deskripsi |
|---------|-------------|
| **Loyalti poin** | 1 poin untuk setiap Rp 1.000 yang dibelanjakan; poin terakumulasi per pelanggan sesuai nama yang diisi di kasir |
| **Buku pelanggan** | Daftar pelanggan dengan total poin, jumlah kunjungan dan total belanja |
| **Reset poin** | Reset poin per pelanggan kapan pun dibutuhkan |

### Saran Restok

| Fitur | Deskripsi |
|---------|-------------|
| **Saran restok otomatis** | Produk di bawah stok minimum ditandai dengan jumlah pesan ulang yang disarankan (target dua kali lipat stok minimum) |
| **Bagikan ke pemasok** | Kirim saran restok lewat WhatsApp dengan satu ketukan |

### Piutang

| Fitur | Deskripsi |
|---------|-------------|
| **Catat utang** | Dari penjualan (pilih Hutang) atau tambah manual dengan jatuh tempo opsional |
| **Bayar sebagian** | Cicilan berapa pun, lunas otomatis saat lunas; pembayaran berlebih ditolak |
| **Ingatkan via WhatsApp** | Kirim pesan pengingat sopan ke pelanggan dengan satu ketukan |

### Beranda

| Fitur | Deskripsi |
|---------|-------------|
| **Ringkasan hari ini** | Omzet, untung bersih, jumlah transaksi dan jumlah stok menipis |
| **Tren 7 hari** | Grafik batang omzet dengan penanda untung per hari |
| **Produk terlaris** | 3 produk terlaris 7 hari terakhir |
| **Skor Kesehatan Usaha** | Skor 0-100 dari margin, laba, persediaan, piutang dan kas, dengan tiga saran paling berdampak |

### Laporan

| Fitur | Deskripsi |
|---------|-------------|
| **Laba rugi sederhana** | Omzet, modal per barang terjual, laba kotor, pengeluaran operasional dan untung bersih, untuk hari ini, 7 hari, 30 hari atau bulan ini |
| **Nilai persediaan** | Nilai stok saat ini dengan modal per barang |
| **Produk paling menguntungkan** | Lima produk dengan laba terbesar periode ini, dengan jumlah terjual |
| **Ekspor PDF dan CSV** | Ringkasan PDF siap bagi atau CSV untuk dibuka di spreadsheet |
| **Bagikan ke WhatsApp** | Kirim ringkasan laporan via WhatsApp atau aplikasi lain |

### Data dan backup

| Fitur | Deskripsi |
|---------|-------------|
| **Database lokal terenkripsi** | Semua data tersimpan di database Room yang dienkripsi SQLCipher, kuncinya dipegang Android Keystore |
| **Backup JSON** | Ekspor backup lengkap ke folder Unduhan dan pulihkan kapan pun di ponsel yang sama atau baru |
| **Skema siap sinkron** | Semua tabel memakai id UUID dengan kolom `updatedAt`, `deletedAt`, `deviceId` dan `syncStatus` |

### Pengaturan

| Fitur | Deskripsi |
|---------|-------------|
| **Tema** | Terang, gelap atau ikut sistem |
| **Mode Warung / Pro** | Mode Warung untuk tampilan sederhana, Mode Pro untuk fitur lengkap |
| **Bahasa aplikasi** | Bahasa Indonesia (default) atau English, ganti di Pengaturan dan langsung berlaku |
| **Kunci PIN** | PIN 6 digit opsional untuk mengunci aplikasi |
| **Profil usaha** | Ubah nama dan jenis usaha kapan pun |

---

## Kebutuhan

| Dependensi | Versi Minimum | Catatan |
|------------|-----------------|-------|
| **JDK** | 17 (21 direkomendasikan) | Hanya untuk build dari source |
| **Android SDK** | API 35 | Hanya untuk build dari source |
| **Perangkat atau emulator Android** | Android 7.0 (API 24) | Untuk menjalankan aplikasi |

> [!TIP]
> Kalau kamu hanya ingin memakai aplikasinya, kamu tidak butuh JDK atau Android SDK. Unduh APK dari halaman [Releases](https://github.com/nekonaru/kalkulasa/releases).

### Belum pernah pakai terminal? Mulai dari sini

<details>
<summary><b>Cara membuka terminal di Windows</b></summary>

1. Tekan tombol **Windows**, ketik `PowerShell`, lalu tekan **Enter**
2. Jendela biru terbuka. Di situlah kamu mengetik perintah
3. Setiap perintah `git ...` atau `./gradlew ...` di panduan ini diketik di jendela itu, diakhiri **Enter**

</details>

---

## Instalasi

### Opsi A: Unduh APK (paling mudah)

1. Buka halaman [Releases](https://github.com/nekonaru/kalkulasa/releases)
2. Unduh `kalkulasa-v1.6.0.apk`
3. Buka di ponsel kamu dan izinkan instalasi dari sumber tidak dikenal jika diminta
4. Buka aplikasi dan isi profil usaha kamu

> [!IMPORTANT]
> Kalkulasa tidak meminta izin runtime apa pun. Izinkan "instal dari sumber tidak dikenal" hanya untuk memasang APK yang kamu unduh sendiri dari repo ini.

### Opsi B: Build dari source (untuk pengembangan / membaca atau mengubah kode)

#### 1. Ambil kode proyek

<details>
<summary><b>Saya sudah punya Git</b></summary>

```bash
git clone https://github.com/nekonaru/kalkulasa.git
cd kalkulasa
```

</details>

<details>
<summary><b>Saya belum punya Git</b></summary>

1. Buka halaman repo di GitHub
2. Klik tombol hijau **`Code`**, pilih **`Download ZIP`**
3. Extract ZIP ke folder pilihan kamu
4. Di terminal, masuk ke folder hasil extract, misalnya:

```bash
cd Downloads/kalkulasa-main
```

</details>

#### 2. Build APK

```bash
./gradlew :app:assembleDebug
```

Hasilnya ada di `app/build/outputs/apk/debug/app-debug.apk`.

#### 3. Install ke perangkat atau emulator

```bash
./gradlew :app:installDebug
```

---

## Cara Pakai

### Peluncuran pertama

1. Buka Kalkulasa dan isi nama usaha, jenis usaha dan modal awal
2. Buka tab **Stok**, tambahkan produk kamu (nama, harga beli, harga jual, stok, stok minimum)
3. Catat **Stok Masuk** setiap kali kamu belanja stok
4. Jual dari tab **Kasir**; stok otomatis berkurang dan laba tercatat otomatis
5. Buka **Beranda** dan **Laporan** untuk melihat kondisi usaha kamu

### Pemakaian sehari-hari

- Jualan: **Kasir**, ketuk produk, ketuk **Simpan penjualan**
- Restok: **Stok**, tab **Stok Masuk**, pilih produk, isi jumlah dan harga beli
- Cek untung: **Beranda** untuk hari ini, **Laporan** untuk periode tertentu
- Backup data tiap bulan: **Pengaturan**, **Backup data**

---

## Bahasa Tampilan

Teks aplikasi tersedia dalam **Bahasa Indonesia** (default) dan **English**. Aplikasi selalu terbuka dalam Bahasa Indonesia, apa pun bahasa sistem ponsel kamu. Untuk beralih ke English, buka **Pengaturan** dan pilih **English** di bagian **Bahasa**. Pilihanmu tersimpan dan langsung berlaku.

---

## Struktur Proyek

```
kalkulasa/
├── app/
│   ├── schemas/                  # riwayat skema Room (dibuat otomatis, di-commit)
│   └── src/main/
│       ├── java/id/kalkulasa/
│       │   ├── core/             # formatter (Rupiah, tanggal), preferensi DataStore
│       │   ├── data/             # entity Room, DAO, database, repository
│       │   ├── domain/           # model Kotlin murni, interface repository, use case
│       │   ├── di/               # modul Hilt
│       │   └── presentation/     # layar per fitur, tema, navigasi
│       └── res/                  # strings (default Indonesia, English), ikon, tema
├── assets/                       # mark aplikasi dan aset Play Store
├── .github/workflows/            # ci.yml (build dan test), release.yml (APK bertanda tangan)
├── LICENSE
├── README.en.md                  # documentation (English)
└── README.md                     # dokumentasi (Bahasa Indonesia)
```

---

## Pengembangan

### Menambah string yang tampil ke pengguna

1. Tambahkan key ke `app/src/main/res/values/strings.xml` (Bahasa Indonesia) dan `app/src/main/res/values-en/strings.xml` (English)
2. Pakai dengan `stringResource(R.string.key_kamu)` atau dari ViewModel lewat context

### Menambah layar baru

1. Buat `presentation/<fitur>/<Fitur>Screen.kt` dan `<Fitur>ViewModel.kt`
2. Daftarkan route di `presentation/navigation/Routes.kt` dan `KalkulasaNavHost.kt`
3. Simpan logika bisnis di domain layer (use case di `domain/usecase`), jangan di composable

> [!WARNING]
> `app/schemas/` dibuat otomatis oleh Room pada setiap build. Commit isinya, tapi jangan pernah edit manual.

---

## Pengujian

Jalankan pemeriksaan sebelum push:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

| Pemeriksaan | Kebutuhan |
|-------|-------------|
| `./gradlew :app:testDebugUnitTest` | Harus lolos seluruh suite |
| `./gradlew :app:assembleDebug` | Harus build tanpa error |
| `./gradlew :app:assembleRelease` | Release harus lolos lint dan R8 |

Lint diberlakukan oleh `lintVitalRelease` bawaan Android Gradle Plugin pada build release.

---

## Keamanan

Kalkulasa menyimpan semuanya secara lokal di database terenkripsi SQLCipher; kuncinya dibuat di perangkat dan digembok oleh Android Keystore. Aplikasi tidak membuat koneksi jaringan dan tidak meminta izin apa pun. Backup yang kamu ekspor adalah JSON biasa; simpan di tempat yang privat. Lihat [`SECURITY.md`](SECURITY.md) untuk cara melaporkan kerentanan.

> [!NOTE]
> PIN lokal hanya mengunci UI. Siapa pun dengan akses penuh ke ponsel yang tidak terkunci bisa membaca data yang ditampilkan aplikasi.

---

## Continuous Integration

| Workflow | Tujuan |
|----------|---------|
| `ci.yml` | Unit test dan build APK debug di setiap push dan pull request |
| `release.yml` | APK release bertanda tangan dan GitHub Release pada tag `v*` |

Workflow release menandatangani APK dengan keystore yang tersimpan di secret repository dan membuat catatan rilis dari `CHANGELOG.md` dan `CHANGELOG.en.md`.

---

## FAQ

<details>
<summary><b>Di mana data saya tersimpan?</b></summary>

Di ponsel kamu, di database lokal yang terenkripsi. Tidak ada yang diunggah ke mana pun. Ekspor backup JSON setiap bulan dari **Pengaturan** supaya aman dari ponsel hilang atau rusak.

</details>

<details>
<summary><b>Apakah Bahasa Inggris didukung?</b></summary>

Ya. Teks aplikasi mengikuti bahasa ponsel: Bahasa Indonesia secara default, English kalau bahasa ponsel di-set English.

</details>

<details>
<summary><b>Apakah butuh internet?</b></summary>

Tidak. Semua fitur berjalan tanpa internet. Internet hanya dipakai kalau kamu memilih membagikan laporan ke WhatsApp.

</details>

<details>
<summary><b>Apakah bisa scan barcode?</b></summary>

Produk punya kolom barcode, tapi fitur scan belum diimplementasikan. Itu masuk roadmap.

</details>

---

## Penulis

<div align="center">

| [![Nicolas Dwi Dharma](https://github.com/nekonaru.png?size=100)](https://github.com/nekonaru) |
|:---:|
| **Nicolas Dwi Dharma** |
| [github.com/nekonaru](https://github.com/nekonaru) |

</div>

---

## Disclaimer

Kalkulasa adalah alat pencatatan. Untung bersih dan laporan adalah estimasi dari catatan kamu sendiri; bukan nasihat pajak, akuntansi atau hukum. Konsultasikan ke profesional untuk keperluan resmi.

---

## Lisensi

Didistribusikan di bawah **MIT License**. Lihat [`LICENSE`](LICENSE) untuk informasi selengkapnya.
