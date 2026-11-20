# Changelog

## v2.0.0

Fitur manajemen yang dimantapkan dan tampilan yang lebih rapi.

### Fitur baru

- **Atur pin produk dari Stok.** Tambah atau ubah produk kini punya opsi "Sematkan di kasir"; produk pin muncul paling atas di Kasir tanpa perlu membuka Kasir.
- **Status pin tidak hilang saat produk diedit.**

### Peningkatan

- **Format backup v4** menyimpan status pin produk; backup versi lama tetap bisa dipulihkan.
- **.kotlin** (metadata compiler) tidak lagi ikut tersimpan di git.

## v1.9.0

Audit lanjutan pada fitur inti, fokus pada konsistensi data dan tampilan.

### Peningkatan

- **Pembatalan transaksi lebih aman.** Membatalkan dua kali tidak lagi mengembalikan stok dua kali; transaksi yang sudah dibatalkan ditolak.
- **Poin loyalti ikut dikoreksi saat transaksi dibatalkan.** Poin, jumlah kunjungan, dan total belanja pelanggan dikurangi sesuai transaksi yang dibatalkan.
- **Grafik tren di Beranda** kini menampilkan label tanggal di bawah setiap batang, supaya mudah dibaca.
- **Laba bersih di Laporan** berwarna hijau saat untung dan merah saat rugi.
- **Riwayat penjualan** ikut diperbarui saat kamu kembali dari Kasir.

## v1.8.0

Audit lanjutan, perbaikan alur data, dan detail pembelian per pemasok.

### Fitur baru

- **Detail belanja per pemasok.** Ketuk pemasok di layar Pemasok untuk melihat seluruh transaksi pembelian dari pemasok itu dalam 30 hari, 90 hari atau 12 bulan terakhir, lengkap dengan total per periode.
- **Tombol Uang pas** di kolom uang diterima saat pembayaran Tunai: satu ketuk mengisi nominal sesuai total.

### Peningkatan

- **Laporan selalu segar** saat kamu kembali dari Kasir, sehingga angka omzet dan laba langsung diperbarui.
- **Ulangi transaksi terakhir kini memeriksa stok**: produk yang stoknya sudah tidak cukup dilewati, bukan masuk keranjang lalu gagal saat disimpan.

## v1.7.0

Fitur ulangi transaksi untuk mempercepat kasir.

### Fitur baru

- **Ulangi transaksi terakhir.** Satu ketuk di Kasir mengisi keranjang dengan isi dari transaksi terakhir yang selesai; produk diarsipkan atau stok kosong otomatis dilewati.

## v1.6.0

Desain lebih rapi dan produk terlaris cepat muncul di kasir.

### Fitur baru

- **Produk terlaris disematkan ke atas kasir.** Ketuk pin pada kartu produk; produk pin muncul paling atas, produk lain menyusul. Cocok untuk barang yang paling sering dibeli.
- **Sapaan sesuai waktu di Beranda.** Selamat pagi, siang, sore atau malam muncul sesuai jam perangkat.

### Peningkatan

- **Status pin produk** tersimpan permanen; tidak berubah saat pindah perangkat (ikut backup).
- **Header Beranda** membaca nama usaha kamu dengan sapaan waktu.

## v1.5.0

Poles desain, modul Pemasok, dan riwayat keuangan lebih lengkap.

### Fitur baru

- **Modul Pemasok.** Layar baru di Keuangan menampilkan daftar pemasok dari riwayat pembelian (kode pembelian diinput bebas, misal "Distributor Jawa"), total belanja per pemasok, jumlah transaksi, dan tanggal transaksi terakhir.
- **Baris pembelian stok di riwayat keuangan.** Setiap kali stok masuk, tercatat di Riwayat keuangan sebagai pengeluaran dengan label pemasoknya.
- **Kartu statistik lebih lapang, ikon dalam chip kecil, dan empty state lebih ramah.**

### Peningkatan

- **Komponen desain disederhanakan dan dipadukan** (rounded corner 20dp, tonal icons).

## v1.4.0

Riwayat penjualan lengkap dan impor produk dari CSV, plus penghapusan kode deprecated.

### Fitur baru

- **Riwayat penjualan.** Layar baru berisi seluruh transaksi per periode (7, 30, 90 hari), dengan omzet per periode, tombol bagikan struk, dan batal transaksi dengan pengembalian stok. Akses dari Kasir > Lihat semua transaksi.
- **Impor produk dari CSV.** Siapkan file teks dengan kolom nama, satuan, harga beli, harga jual, stok, stok minimum, barcode (opsional) dan kategori (opsional). Impor massal seratus baris dalam hitungan detik. Dapatkan contoh format langsung dari aplikasi.
- **Parser CSV canggih.** Mendukung pemisah koma atau titik koma, sel dengan tanda kutip yang berisi pembatas, baris judul kolom opsional, penanganan BOM, dan format angka Indonesia (titik seribu, koma desimal).

### Peningkatan

- **Locale deprecated diganti** dengan Locale.forLanguageTag("id-ID") di seluruh kode; tidak ada lagi warning deprecation.
- **Ikon WhatsApp modern** di layar Piutang dan Peringatan stok.

## v1.3.0

Fitur lanjutan kasir dan laporan, plus perbaikan performa dan polesan UI/UX.

### Fitur baru

- **Kalkulator kembalian (Tunai).** Saat memilih metode bayar Tunai, isi uang yang diterima dan Kalkulasa langsung menghitung kembaliannya.
- **Manajemen kategori produk.** Tambah, ubah nama, dan hapus kategori langsung dari tab Produk di Stok.
- **Produk paling menguntungkan.** Laporan menampilkan lima produk dengan laba terbesar untuk periode yang dipilih, lengkap dengan jumlah terjual dan untung.

### Peningkatan

- **Splash screen branded** dengan logo Kalkulasa.
- **Kompatibilitas dan kebersihan kode**: pengganti Locale deprecated, ikon WhatsApp modern.
- **Perbaikan peringatan di layar Piutang** (ikon auto-mirror).

## v1.2.0

Yang baru: Mode Warung untuk pemilik usaha pemula, program poin loyalti pelanggan, dan saran restok otomatis.

### Fitur baru

- **Mode Warung.** Tampilan super sederhana untuk pemilik usaha yang baru belajar, tombol besar dan hanya angka yang paling penting. Ganti ke Mode Pro kapan pun di Pengaturan > Tampilan.
- **Pelanggan dan poin loyalti.** Isi nama pelanggan saat penjualan (opsional), setiap rupiah 1.000 otomatis jadi 1 poin. Layar Pelanggan menunjukkan riwayat poin, kunjungan dan total belanja per pelanggan, dengan opsi reset poin.
- **Saran Restok.** Kalkulasa menandai produk yang stoknya di bawah batas minimum dan menghitung berapa buah yang harus dipesan ulang (target dua kali lipat stok minimum), bisa langsung dibagikan sebagai pesan WhatsApp ke pemasok.
- **Catatan nama pelanggan di semua penjualan.** Nama pelanggan tidak hanya untuk jualan Hutang; siapa pun yang diisi namanya otomatis terkumpul poinnya.

### Peningkatan

- **Ekspor CSV** tidak lagi menyertakan transaksi yang dibatalkan.
- **Migrasi database otomatis v2 ke v3**; data lama aman.
- **Backup format v3** mencakup data pelanggan dan poin; restore untuk backup v1 dan v2 tetap didukung.

## v1.1.0

Modul pelanggan dan piutang, struk digital yang bisa dibagikan, dan skor kesehatan usaha langsung di Beranda.

### Fitur baru

- **Modul Piutang.** Catat utang pelanggan dari penjualan (pilih Hutang di kasir) atau entri manual; bayar sebagian atau lunas dengan validasi pembayaran berlebih ditolak; buku utang dengan tab Belum lunas / Lunas; ingatkan pelanggan lewat WhatsApp dengan satu ketukan.
- **Penjualan Hutang di kasir.** Pilih metode bayar Hutang, isi nama pelanggan, penjualan tercatat dan utang otomatis muncul di buku Piutang. Membatalkan transaksi Hutang otomatis menghapus utangnya.
- **Struk digital.** Setelah penjualan tersimpan, bagikan struk lewat WhatsApp atau aplikasi lain dalam satu ketukan; struk juga bisa dibagikan per transaksi di daftar transaksi hari ini.
- **Skor Kesehatan Usaha.** Beranda menampilkan skor 0-100 dengan verdict Prima / Sehat / Cukup / Perlu perhatian, dirinci dari margin, laba, nilai persediaan, piutang dan saldo kas, beserta tiga saran paling berdampak.
- **Bahasa aplikasi bisa diganti.** Pengaturan > Bahasa: Bahasa Indonesia (default) atau English, langsung berlaku tanpa restart.

### Peningkatan

- **Bahasa Indonesia jadi default proyek** di aplikasi, README, metadata repo dan release notes; English tetap ada sebagai bahasa kedua.
- **Saldo kas lebih akurat**: penjualan Hutang tidak dihitung sebagai kas masuk; pembayaran piutang masuk saat diterima.
- **Riwayat stok lebih cepat** dimuat (query join tunggal, tanpa N+1).
- **Backup lebih aman**: versi backup didukung hingga skema terbaru, file divalidasi penuh sebelum data diganti, maksimum 100 MB.

### Perbaikan bug

- **Restore backup tidak lagi rawan crash**; proses parsing dan restore berjalan di IO thread dengan transaksi database atomik.
- **Crash SQLCipher saat pertama jalan** diperbaiki dengan memuat native library di Application.
- **Izin jaringan tidak lagi terdaftar** setelah WorkManager yang tidak terpakai dihapus; manifest sekarang nol izin runtime.

## v1.0.0

Rilis pertama Kalkulasa: pencatatan stok dan kas offline untuk UMKM, dengan kasir satu ketuk dan modal per barang otomatis.

### Fitur baru

- **Kasir satu ketuk.** Ketuk produk ke keranjang, beri diskon rupiah, pilih tunai / transfer / QRIS sebagai label pembayaran; penjualan mengurangi stok dengan modal per barang tercatat per item.
- **Manajemen produk dan stok.** Tambah, ubah, cari, arsipkan dan kembalikan produk; stok masuk dengan harga beli; penyesuaian stok dengan alasan; riwayat pergerakan lengkap.
- **Modal per barang (HPP) otomatis.** Stok masuk menghitung ulang rata-rata tertimbang; laba historis tidak berubah saat harga berubah.
- **Peringatan stok menipis.** Beranda dan Stok menandai produk yang stoknya di batas minimum atau kurang.
- **Modul keuangan.** Modal awal dan tambahan modal, prive tercatat terpisah, pengeluaran operasional per kategori, dan saldo kas yang hidup.
- **Beranda.** Omzet, untung bersih, jumlah transaksi, jumlah stok menipis, tren omzet 7 hari dan produk terlaris.
- **Laporan.** Laba rugi sederhana untuk hari ini, 7 hari, 30 hari atau bulan ini; nilai persediaan; ekspor PDF dan CSV; bagikan ringkasan via WhatsApp.
- **Database lokal terenkripsi.** Database SQLCipher dengan kunci yang digembok Android Keystore; tanpa jaringan, tanpa izin, tanpa akun.
- **Backup dan pulihkan.** Backup JSON lengkap ke folder Unduhan dan pulihkan di ponsel yang sama atau baru.
- **Kunci PIN dan tema.** PIN 6 digit opsional, plus tema terang, gelap dan ikut sistem.
- **Bahasa Indonesia dan English.** Bahasa Indonesia adalah default; English mengikuti bahasa perangkat.
