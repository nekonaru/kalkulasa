# Changelog

## v5.3.0

Tambahan kasir: voucher khusus pelanggan dan diskon per item.

### Fitur baru

- **Voucher per pelanggan.** Voucher dengan cakupan PELANGGAN hanya berlaku saat nama pelanggan yang diisi cocok dengan pemilik voucher; voucher GLOBAL tetap berlaku untuk semua transaksi.
- **Diskon per item.** Setiap baris keranjang bisa diberi diskon rupiah tersendiri. Diskon per item mengurangi total baris sebelum diskon transaksi dihitung, dan tersimpan per item penjualan.

## v5.2.0

Tambahan keuangan akhir: bayar hutang batch dan laporan PPN.

### Fitur baru

- **Bayar hutang batch.** Pilih beberapa tagihan pemasok sekaligus, isi porsi pembayaran masing-masing, lalu bayar dalam satu transaksi.
- **Laporan PPN.** Hitung PPN keluaran 11% dari total yang belum termasuk pajak.

### Peningkatan

- **Aturan pajak dan batch teruji.** `PpnBatchUseCase` memuat perhitungan PPN dan validasi porsi pembayaran sebagai Kotlin murni dengan tes unit.

## v5.1.0

Tambahan karyawan: peran, PIN per karyawan, shift, dan lembur.

### Fitur baru

- **Peran karyawan.** Setiap karyawan memiliki peran Kasir atau Manajer sebagai dasar pembedaan akses.
- **PIN per karyawan.** Atur PIN enam digit untuk karyawan; verifikasi berjalan di dispatcher CPU agar tidak mem-blok UI.
- **Shift kerja.** Buat shift dengan jam mulai dan selesai dalam menit.
- **Lembur.** Catat menit lembur dan upah lembur dihitung proporsional dari upah harian delapan jam.

### Peningkatan

- **Aturan shift teruji.** `ShiftUseCase` memuat perhitungan menit lembur dan upah lembur sebagai Kotlin murni dengan tes unit.
- **Backup format v19** mencakup PIN karyawan, shift, lembur, dan peran. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v18 ke v19** tanpa menghapus data lama.

## v5.0.0

Rilis penutup UMKM Pro Suite: backup otomatis harian dan jual cepat dari produk favorit.

### Fitur baru

- **Auto-backup harian.** Sekali sehari aplikasi menyimpan backup JSON ke folder aplikasi secara otomatis saat Pengaturan dibuka. Tidak menggantikan backup manual, melainkan lapisan pengaman tambahan.
- **Jual cepat.** Produk favorit muncul sebagai pintasan di kasir; satu ketuk langsung menambahkannya ke keranjang.

### Catatan

Widget layar utama, dashboard customizable, audit aksesibilitas penuh, pencarian FTS, PIN per karyawan, shift, lembur, dan beberapa fitur lanjutan lainnya tetap menjadi utang teknis dan belum masuk rilis ini.

## v4.9.0

Tambahan CRM: broadcast WhatsApp, segmentasi pelanggan, dan kode kartu member.

### Fitur baru

- **Segmentasi pelanggan.** Filter berdasarkan tag dan minimal total belanja untuk memilih penerima pesan.
- **Broadcast WhatsApp.** Pilih beberapa pelanggan sekaligus dan kirim satu pesan yang dipersonalisasi dengan nama masing-masing.
- **Kode kartu member.** ID member unik berprefix KLK-MEMBER untuk setiap pelanggan, ditampilkan bersama tier.

### Peningkatan

- **Aturan segmentasi teruji.** `SegmentasiUseCase` memuat pencocokan tag dan minimal belanja sebagai Kotlin murni dengan tes unit.

## v4.8.0

Tambahan keuangan: laporan arus kas periode dan umur hutang usaha.

### Fitur baru

- **Arus kas periode.** Ringkasan kas masuk dari penjualan, piutang, dan modal, serta kas keluar dari pembelian, pengeluaran, prive, hutang, dan kasbon, untuk hari ini, 7 hari, 30 hari, atau bulan ini.
- **Umur hutang usaha.** Hutang pemasok belum lunas dikelompokkan menjadi belum jatuh tempo, 1-30 hari, 31-60 hari, dan lebih dari 60 hari.

### Peningkatan

- **Aturan arus kas teruji.** `ArusKasUseCase` merangkum seluruh komponen kas sebagai Kotlin murni dengan tes unit.

## v4.7.0

Tambahan gudang: peringatan kedaluwarsa dan riwayat harga beli.

### Fitur baru

- **Peringatan kedaluwarsa.** Daftar batch dengan tanggal expiry dalam tujuh hari berikutnya beserta jumlah stoknya.
- **Riwayat harga beli.** Lihat perubahan harga satuan dari catatan stok masuk untuk setiap produk.

## v4.6.0

Tambahan kasir: pembayaran terpisah, pemakaian deposit, dan struk 58 mm.

### Fitur baru

- **Pembayaran terpisah.** Satu transaksi bisa memakai metode kedua dengan nominal tersendiri, misalnya tunai plus transfer.
- **Bayar pakai deposit.** Saldo deposit pelanggan bisa dipakai sebagai bagian dari pembayaran dan otomatis dipotong dari saldo.
- **Struk 58 mm.** Format teks lebar 32 karakter untuk printer thermal, dengan perataan nama toko dan rincian total.

### Peningkatan

- **Aturan struk teruji.** `Struk58UseCase` memuat lebar baris, perataan tengah, dan format rincian sebagai Kotlin murni dengan tes unit.

## v4.5.0

Fase 6 dan terakhir dari Kalkulasa UMKM Pro Suite: checklist harian buka dan tutup toko.

### Fitur baru

- **Checklist harian.** Buat tugas buka dan tutup toko, lalu centang statusnya setiap hari. Status tersimpan per tanggal sehingga progres harian tidak tercampur.

### Peningkatan

- **Backup format v18** mencakup item dan log checklist. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v17 ke v18** tanpa menghapus data lama.

### Catatan

Widget layar utama, auto-backup terjadwal, dashboard customizable, audit aksesibilitas penuh, dan pencarian FTS tidak masuk rilis ini dan tetap menjadi utang teknis.

## v4.4.0

Fase 5 dari Kalkulasa UMKM Pro Suite: CRM dengan skor RFM dan pengingat ulang tahun.

### Fitur baru

- **Skor RFM pelanggan.** Skor 1 sampai 5 dari jumlah kunjungan dan total belanja, diurutkan dari yang paling bernilai.
- **Pengingat ulang tahun.** Hitung hari menuju ulang tahun pelanggan dari tanggal lahir yang tersimpan.

### Peningkatan

- **Aturan CRM teruji.** `CrmUseCase` memuat perhitungan RFM dan hari ulang tahun sebagai Kotlin murni dengan tes unit.
- **Backup format v17** mencakup tanggal lahir pelanggan. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v16 ke v17** tanpa menghapus data lama.

## v4.3.0

Fase 4 dari Kalkulasa UMKM Pro Suite: ranking performa karyawan dan slip gaji digital.

### Fitur baru

- **Ranking karyawan.** Urutan karyawan bulan ini berdasarkan upah yang dibayarkan, jumlah hari hadir, dan skor performa 0 sampai 100.
- **Slip gaji digital.** Bagikan ringkasan upah, kasbon, lembur, dan jumlah yang dibayar lewat WhatsApp atau aplikasi lain.

### Peningkatan

- **Aturan karyawan pro teruji.** `KaryawanProUseCase` memuat perhitungan skor dan format slip gaji sebagai Kotlin murni dengan tes unit.

## v4.2.0

Fase 3 dari Kalkulasa UMKM Pro Suite: pengeluaran berulang dan bagi hasil.

### Fitur baru

- **Pengeluaran berulang.** Daftar biaya rutin dengan kategori, nominal, dan hari jatuh tempo tiap bulan. Satu ketuk menjalankannya sebagai pengeluaran nyata.
- **Bagi hasil.** Atur penerima dan persentase pembagian laba. Total bagian tidak pernah melebihi laba bersih.

### Peningkatan

- **Aturan keuangan pro teruji.** `KeuanganProUseCase` memuat penyesuaian tanggal bulan dan pembagian laba sebagai Kotlin murni dengan tes unit.
- **Backup format v16** mencakup pengeluaran berulang dan bagi hasil. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v15 ke v16** tanpa menghapus data lama.

## v4.1.0

Fase 2 dari Kalkulasa UMKM Pro Suite: pemasok dengan lead time, transfer stok, dan barcode internal.

### Fitur baru

- **Pemasok dan lead time.** Simpan nama pemasok, telepon, dan waktu tunggu pengiriman dalam hari. Lead time menjadi dasar saran restok yang lebih realistis.
- **Transfer stok.** Pindahkan produk antar lokasi atau rak dengan catatan pergerakan dan rincian item.
- **Barcode internal.** Hasilkan kode unik berprefix KLK untuk produk yang belum memiliki barcode, siap dibagikan atau dicetak.

### Peningkatan

- **Aturan gudang teruji.** `GudangUseCase` memuat saran restok berbasis lead time, format barcode, dan deteksi kedaluwarsa sebagai Kotlin murni dengan tes unit.
- **Backup format v15** mencakup pemasok, transfer, dan item transfer. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v14 ke v15** tanpa menghapus data lama.

## v4.0.0

Fase 1 dari Kalkulasa UMKM Pro Suite: preorder dan paket bundling di kasir.

### Fitur baru

- **Preorder.** Catat pesanan pelanggan sebelum stok tersedia, lalu tandai menjadi terjual atau dibatalkan dari satu layar. Total preorder dihitung otomatis dari produk dan quantity.
- **Paket bundling.** Buat paket beberapa produk dengan harga khusus. Harga paket dibatasi agar tidak melebihi total harga normal produk.
- **Aturan kasir pro teruji.** `PreorderBundlingUseCase` memuat validasi harga paket, pembayaran terpisah, dan diskon per baris sebagai Kotlin murni dengan tes unit.

### Peningkatan

- **Struktur kasir disiapkan** untuk pembayaran terpisah, pemakaian deposit, dan diskon per baris melalui kolom baru pada transaksi dan item penjualan.
- **Backup format v14** mencakup preorder, item preorder, paket, item paket, dan riwayat pemakaian voucher. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v13 ke v14** tanpa menghapus data lama.

## v3.5.0

Rilis optimalisasi untuk membuat aplikasi lebih ringan, cepat, dan lancar tanpa fitur baru.

### Peningkatan performa

- **Beranda tidak lagi menjalankan 58 query saat dibuka.** Duplikasi pemicu muat dihapus dan laporan bulan dihitung sekali, sehingga query turun drastis.
- **Saldo kas jadi satu query.** Delapan agregat terpisah digabung menjadi satu kueri dengan subquery skalar di SQLite.
- **Nilai persediaan tidak lagi memuat seluruh tabel produk ke memori.** Agregat `SUM` dilakukan langsung di SQL.
- **Laporan Keuangan lebih ringan.** Query paralel, tidak ada lagi pemanggilan `netCapital` yang duplikat, dan persediaan memakai hasil agregat yang sudah ada.
- **Kasir lebih responsif.** Satu observer produk dan satu observer pelanggan, pencarian keranjang memakai peta id, dan diskon member memakai daftar pelanggan yang sama.
- **Penjualan dan opname massal dibatch.** Puluhan hingga ratusan statement per transaksi dikurangi menjadi beberapa operasi massal.
- **Verifikasi PIN tidak lagi mem-blok main thread.** PBKDF2 dijalankan di dispatcher CPU.
- **Laporan dan riwayat tidak dimuat dua kali** saat layar pertama dibuka.
- **Pencarian global ada jeda 250 ms** sehingga tidak membaca data pada setiap ketukan.
- **Batas restore backup diturunkan ke 25 MB** dan file dengan ukuran tidak dikenal ditolak, mencegah kehabisan memori.

### Perbaikan

- Kunci `LazyColumn` ditambahkan pada produk terlaris.
- Rekap absensi mengelompokkan data per karyawan sekali, bukan filter berulang.

## v3.4.0

Fase 5 dan terakhir dari Kalkulasa Suite UMKM: ekspor data master dan pencarian global.

### Fitur baru

- **Ekspor data master ke CSV.** Unduh daftar produk, pelanggan, dan hutang usaha dalam format CSV yang siap dibuka di spreadsheet.
- **Pencarian global.** Cari produk berdasarkan nama atau barcode, dan transaksi berdasarkan ID atau metode bayar dari satu kolom.

### Peningkatan

- **CSV escape yang benar.** Setiap sel dibungkus tanda kutip dan tanda kutip dalam nilai diduplikasi, sehingga koma tidak merusak kolom.
- **File ekspor dibagikan lewat FileProvider** dengan izin baca sementara, tanpa izin penyimpanan permanen.

## v3.3.0

Fase 4 dari Kalkulasa Suite UMKM: catatan pelanggan, tag, dan kartu member digital.

### Fitur baru

- **Catatan dan tag pelanggan.** Simpan tag dan catatan bebas per pelanggan dari halaman khusus CRM.
- **Kartu member digital.** Bagikan ringkasan nama, poin, dan jumlah kunjungan pelanggan lewat WhatsApp atau aplikasi lain.

### Peningkatan

- **Backup format v13** mencakup catatan dan tag pelanggan. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v12 ke v13** tanpa menghapus data lama.

## v3.2.0

Fase 3 dari Kalkulasa Suite UMKM: budget bulanan dan rekonsiliasi kas harian.

### Fitur baru

- **Budget bulanan per kategori.** Atur anggaran untuk setiap kategori pengeluaran dan lihat progres pemakaiannya terhadap anggaran bulan berjalan.
- **Rekonsiliasi kas harian.** Catat jumlah kas fisik, bandingkan dengan saldo sistem, dan simpan selisihnya sebagai riwayat audit.

### Peningkatan

- **Aturan keuangan lanjutan teruji.** `KeuanganLanjutanUseCase` berisi logika persentase budget dan status selisih kas tanpa dependency Android.
- **Backup format v12** mencakup budget dan riwayat rekonsiliasi. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v11 ke v12** tanpa menghapus data lama.

## v3.1.0

Fase 2 dari Kalkulasa Suite UMKM: lokasi penyimpanan, konversi satuan, batch, dan tanggal kedaluwarsa.

### Fitur baru

- **Lokasi dan rak.** Setiap produk bisa diberi lokasi penyimpanan dari form stok.
- **Konversi satuan.** Produk bisa punya satuan konversi dan jumlah per konversi, misalnya 1 dus berisi 12 pcs.
- **Nomor batch.** Setiap stok masuk bisa dicatat dengan nomor batch untuk pelacakan.
- **Tanggal kedaluwarsa.** Stok masuk bisa menyimpan tanggal expiry dalam format yyyy-MM-dd dan ikut tercatat di riwayat pergerakan.

### Peningkatan

- **Backup format v11** mencakup lokasi, konversi satuan, batch, dan expiry. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v10 ke v11** tanpa menghapus data lama.

## v3.0.0

Fase 1 dari Kalkulasa Suite UMKM: kasir promosi, member, voucher, deposit, harga grosir, dan PPN.

### Fitur baru

- **Tier member otomatis.** Reguler, Silver, Gold, dan Platinum dari total belanja seumur hidup, dengan diskon 5%, 10%, dan 15% yang diterapkan otomatis saat nama pelanggan diisi.
- **Voucher dan kupon.** Kode, tipe nominal atau persen, minimal belanja, kuota, dan masa berlaku. Kode divalidasi dan kuota dipotong dalam transaksi penjualan yang sama.
- **Deposit pelanggan.** Catat uang muka, lihat saldo per pelanggan, dan pakai untuk pembayaran. Saldo dihitung dari seluruh deposit dikurangi pemakaian.
- **Harga grosir.** Produk bisa punya harga grosir dan minimal quantity. Harga efektif dipakai otomatis di keranjang saat quantity mencapai batas.
- **Produk favorit.** Tandai produk favorit dari form stok untuk pintasan penjualan.
- **PPN opsional.** Aktifkan PPN 11% dari Pengaturan kasir; nominal PPN dihitung setelah diskon dan tersimpan per transaksi.

### Peningkatan

- **Aturan promosi murni dan teruji.** `MemberUseCase`, `PromosiLanjutanUseCase`, dan `DepositUseCase` berisi logika tanpa Android, dengan tes unit untuk tier, voucher, grosir, PPN, dan deposit.
- **Backup format v10** mencakup voucher, deposit, dan kolom baru pada produk serta penjualan. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v9 ke v10** tanpa menghapus data lama.

## v2.8.0

Penutupan temuan audit yang tersisa, ditambah riwayat pembayaran dan laporan absensi.

### Fitur baru

- **Riwayat Pembayaran Hutang.** Setiap pembayaran pemasok tercatat otomatis; lihat daftar dan total pembayaran bulan ini dari halaman Hutang Usaha.
- **Laporan Absensi.** Rekap hari hadir dan total upah per karyawan untuk hari ini, 7 hari, 30 hari, atau bulan ini.

### Peningkatan

- **Laba per produk sekarang memperhitungkan diskon.** Diskon dialokasikan proporsional ke item dalam transaksi, sehingga "Produk paling menguntungkan" dan "Margin tertinggi" tidak lagi overstated.
- **Kasbon tidak dihitung dua kali.** Kasbon yang sudah dipotong saat penggajian ditandai terpakai dan tidak lagi tampil sebagai aset di Neraca.
- **Bayar gaji lebih aman.** Ada penanda in-flight dan penolakan di repository bila gaji periode tersebut sudah lunas, mencegah dobel pembayaran.
- **Skor kesehatan memakai satu definisi bulan berjalan**, sama seperti kartu omzet, Laporan, dan Wawasan.
- **Struk, pesan WhatsApp piutang, dan pesan saran restok mengikuti bahasa aplikasi.**
- **Pesan snackbar error tidak lagi menampilkan teks teknis** dari exception.
- **Impor CSV dibatasi maksimal 5 MB** untuk mencegah kehabisan memori.
- **Riwayat Aktivitas menampilkan label yang benar** untuk modal, absensi, kasbon, profil, target, dan pembayaran.
- **Umur piutang lebih jujur.** Piutang tanpa jatuh tempo selalu masuk "Belum jatuh tempo", dan keterlambatan satu jam sudah dihitung satu hari.

### Perbaikan

- **Wawasan pelanggan paling sering** kini dihitung dari transaksi bulan berjalan, bukan total seumur hidup.
- **Backup format v9** mencakup riwayat pembayaran dan status terpakai kasbon; backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v8 ke v9** tanpa menghapus data lama.

## v2.7.0

Konsistensi angka lintas modul, perbaikan poin dan backup, ditambah riwayat aktivitas, umur piutang, dan wawasan usaha.

### Fitur baru

- **Riwayat Aktivitas.** Linimasa 200 perubahan terakhir pada penjualan, produk, pelanggan, karyawan, pengeluaran, dan data lainnya.
- **Umur Piutang.** Ringkasan piutang belum lunas dalam empat kelompok: belum jatuh tempo, 1-30 hari, 31-60 hari, dan lebih dari 60 hari.
- **Wawasan Usaha.** Rata-rata nilai transaksi, jam dan hari paling ramai, pelanggan paling sering, serta produk dengan margin tertinggi untuk bulan berjalan.

### Peningkatan

- **Nilai persediaan kini satu definisi.** Laporan, Beranda, dan Neraca sama-sama mengecualikan produk terarsipkan.
- **Poin tidak hangus** ketika total diskon melebihi subtotal; jumlah poin yang benar-benar dipakai yang dikurangi.
- **Laporan Keuangan dan Riwayat Penjualan** kini menampilkan pesan error dan tombol Coba lagi saat query gagal.
- **Hapus PIN tetap menunggu verifikasi** sebelum dialog ditutup, dan hash PIN korup tidak lagi membuat aplikasi crash.
- **Backup v1 bisa dipulihkan kembali** setelah penyesuaian validasi tabel inti.
- **Opname massal hanya menampilkan produk aktif.**
- **Profit negatif berwarna merah** pada daftar produk paling menguntungkan.

### Perbaikan dokumentasi

- FAQ scan barcode dan bahasa tampilan kini sesuai dengan implementasi nyata.

## v2.6.0

Perbaikan ekspor dan keamanan hasil audit, ditambah scan barcode dan opname massal.

### Fitur baru

- **Scan barcode di kasir.** Ketik atau scan kode produk dari scanner keyboard, lalu tekan Enter untuk langsung menambahkan produk ke keranjang. Tidak memerlukan izin kamera.
- **Opname massal.** Hitung fisik semua stok sekaligus dalam satu layar, lalu simpan semua penyesuaian dalam satu transaksi.

### Peningkatan

- **Ekspor CSV dan PDF sekarang berhasil.** Path FileProvider diperbaiki sehingga file laporan benar-benar bisa dibagikan.
- **PIN dilindungi PBKDF2 dengan salt acak** dan iterasi 120.000. PIN lama otomatis di-upgrade saat berhasil diverifikasi.
- **Hapus PIN wajib memasukkan PIN saat ini**, sehingga kunci tidak bisa dimatikan dari layar yang sudah terbuka.
- **Laporan, Beranda, dan impor tidak lagi macet di loading** saat query gagal; ada pesan error dan tombol Coba lagi.
- **CI menjalankan lint**, dan workflow rilis menjalankan unit test sebelum mempublish APK.

### Perbaikan

- **Poin yang ditukar dikembalikan** saat transaksi dibatalkan. Sebelumnya pelanggan kehilangan poin tersebut permanen.
- **Backup format v8** menyimpan jumlah poin yang ditukar per transaksi; backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v7 ke v8** tanpa menghapus data lama.
- **Locale resource Indonesia diperbaiki** dari `in` menjadi `id` untuk per-app language di Android 13 ke atas.
- **Guard overflow** pada perhitungan total keranjang dengan jumlah sangat besar.
- **Kategori resource tidak lagi bentrok** dengan kode locale lama.

## v2.5.0

Diskon dan loyalti yang lebih lengkap, target penjualan, serta pengamanan backup hasil audit lapisan data.

### Fitur baru

- **Diskon persen di kasir.** Pilih diskon dalam Rupiah atau Persen; potongan dihitung dari subtotal dan tidak pernah melebihi total belanja.
- **Tukar poin loyalti.** Pelanggan bernama bisa menukar poin menjadi potongan harga (1 poin = Rp100). Poin dipotong dan poin baru dari transaksi dicatat dalam satu transaksi database.
- **Target penjualan.** Atur target harian dan bulanan dari Keuangan; Beranda menampilkan progres omzet terhadap target tersebut.

### Peningkatan

- **Backup lebih aman.** File dengan struktur salah, versi tidak dikenal, atau tanpa tabel inti kini ditolak sebelum database dihapus. Ekspor backup dijalankan dalam satu transaksi agar isinya konsisten.
- **Pembayaran tunai divalidasi.** Kasir menolak transaksi Tunai ketika uang diterima kurang dari total.
- **Pencarian transaksi terakhir lebih cepat** memakai query SQL berstatus SELESAI dengan batas satu baris.
- **Seluruh riwayat kasbon dan gaji tampil**, tidak dibatasi 100 baris.
- **Neraca menunjukkan kas negatif** apa adanya, sehingga overdraft tidak tersembunyi.

### Perbaikan

- **Diskon melebihi subtotal ditolak** di repository, bukan hanya disembunyikan di tampilan.
- **Backup format v7** mencakup target penjualan; backup versi lama tetap dapat dipulihkan.
- **Migrasi database otomatis v6 ke v7** tanpa menghapus data lama.
- **Aturan promosi dan penukaran poin** berada di use case murni dengan tes unit.

## v2.4.0

Laporan keuangan mendalam dan perbaikan lanjutan hasil audit UI, navigasi, dan konsistensi kas.

### Fitur baru

- **Laporan Keuangan Lengkap.** Tab Neraca menampilkan aset lancar (kas, piutang, persediaan, kasbon), liabilitas hutang usaha, dan ekuitas bersih.
- **Rincian Saldo Kas.** Rincian semua komponen kas masuk dan kas keluar, termasuk pembayaran hutang usaha dan kasbon yang sebelumnya tidak tercatat.
- **Kategori Biaya.** Pengeluaran operasional dikelompokkan per kategori dengan persentase untuk hari ini, 7 hari, 30 hari, atau bulan ini.

### Peningkatan

- **Saldo kas kini mencerminkan semua arus uang.** Pembayaran hutang usaha dan kasbon karyawan mengurangi kas; gaji dan pengeluarannya kini ditulis dalam satu transaksi database yang sama.
- **Tampilan Keuangan segar saat kembali** dari layar lain, sehingga saldo tidak basi.
- **Label dan pesan mengikuti bahasa aplikasi**, termasuk status batal, stok, kuantitas terjual, dan pesan snackbar.

### Perbaikan

- **Crash saat membuka detail pemasok "Tanpa pemasok"** sudah diatasi dengan penanda rute khusus.
- **Tombol Bayar gaji tidak lagi terkunci** setelah pembayaran sebagian; sisa gaji tetap bisa dibayar.
- **Pesan snackbar piutang** kini membedakan utang baru tercatat dari pembayaran tercatat.
- **Pesan error backup** menampilkan teks yang bisa dipahami, bukan kode teknis.
- **Laporan keuangan tidak tertukar periode** saat filter diganti cepat.
- **Rumus laba ditahan dan neraca** dipindahkan ke use case murni dan diberi tes unit.

## v2.3.0

Audit mendalam dan perbaikan anomali, ditambah modul Hutang Usaha yang tetap berjalan offline penuh.

### Fitur baru

- **Buku Hutang Usaha.** Catat tagihan pemasok dengan nominal, jatuh tempo, dan catatan. Total hutang belum lunas tampil di ringkasan Keuangan.
- **Bayar hutang.** Cicilan berapa pun; pembayaran melebihi sisa tagihan ditolak dan lunas otomatis saat terbayar penuh.
- **Filter dan pencarian.** Pisahkan hutang belum lunas dan lunas, cari pemasok, dan tandai tagihan yang jatuh temponya terlewat.
- **Konfirmasi ke pemasok.** Kirim pengingat pembayaran lewat WhatsApp atau aplikasi lain yang tersedia.

### Peningkatan

- **Backup format v6** mencakup hutang usaha dan data absensi yang sebelumnya tidak ikut tercadangkan. Backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v5 ke v6** tanpa menghapus data lama.

### Perbaikan

- **Impor CSV kini mempertahankan pecahan stok.** Sebelumnya stok 2,5 kg tercatat sebagai 2 kg tanpa pemberitahuan.
- **Nomor baris error CSV** kini sesuai nomor baris di editor teks walau ada baris kosong.
- **Batal penjualan hutang** tidak lagi mengurangi saldo kas dari uang yang sudah diterima pelanggan.
- **Harga beli 0 ditolak** saat stok masuk, sehingga HPP rata-rata tidak rusak.
- **Laporan tidak tertukar** lagi saat pengguna mengganti periode dengan cepat; ekspor memakai rentang yang sedang ditampilkan.
- **Ekspor PDF dan CSV tidak crash** lagi saat gagal menulis file atau tidak ada aplikasi penerima.
- **Pembayaran gaji yang gagal** kini dilaporkan, bukan dianggap berhasil. Status Lunas pada gaji juga tampil dengan benar.
- **Validasi lebih ketat** pada angka terlalu besar, jumlah negatif, diskon negatif, dan nama pelanggan kosong.
- **Pembulatan rupiah konsisten** antara keranjang kasir, penjualan, stok masuk, dan nilai persediaan.
- **Nama usaha tetap tersimpan** walau entri profil hilang setelah pemulihan backup.
- **Teks Tersimpan mengikuti bahasa** aplikasi, tidak lagi selalu Bahasa Indonesia.

## v2.2.0

Modul Karyawan dan Gaji, lanjutan roadmap Fase 3 yang tetap jalan offline penuh.

### Fitur baru

- **Data karyawan.** Tambah, ubah, nonaktifkan karyawan dengan nama, jabatan, telepon, dan upah harian.
- **Absensi harian.** Tandai Hadir, Izin, Sakit, atau Alfa per karyawan per tanggal; upah dihitung dari status Hadir dan disimpan sebagai snapshot agar perubahan upah tidak mengubah riwayat.
- **Kasbon.** Catat uang muka yang diambil karyawan; otomatis dipotong saat penggajian.
- **Penggajian.** Hitung upah per karyawan untuk bulan berjalan atau bulan sebelumnya, lalu bayar sekali ketuk. Pembayaran gaji otomatis tercatat sebagai pengeluaran kategori Gaji.
- **Riwayat gaji** tersimpan lengkap dengan nama karyawan dan periode.

### Peningkatan

- **Format backup v5** mencakup karyawan, absensi, kasbon, dan riwayat gaji; backup lama tetap bisa dipulihkan.
- **Migrasi database otomatis v4 ke v5** tanpa menghapus data lama.

## v2.1.0

Optimalisasi performa tanpa fitur baru.

### Peningkatan

- **Beranda lebih cepat.** Semua pembacaan data independen (omzet, laba, stok menipis, piutang, kesehatan usaha) kini berjalan paralel, bukan berurutan.
- **Grafik tren jauh lebih ringan.** Dulu 14 query SQL per refresh, sekarang 2 query agregat; hari tanpa transaksi tetap ditampilkan dengan nilai nol.
- **Ekspor laporan tidak lagi mem-blok UI.** Penulisan file PDF/CSV dipindah ke thread IO.
- **Formatter tanggal dan rupiah thread-safe**, menghindari hasil acak saat dipanggil bersamaan.

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
