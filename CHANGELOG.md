# Changelog

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
