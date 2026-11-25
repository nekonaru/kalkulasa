package id.kalkulasa.domain.model

/** Pure Kotlin domain models. No Android or Room dependencies. */

data class Employee(
    val id: String,
    val nama: String,
    val jabatan: String?,
    val telepon: String?,
    val upahHarian: Long,
    val aktif: Boolean,
)

data class Attendance(
    val id: String,
    val karyawanId: String,
    val tanggal: Long,
    val status: String, // HADIR, IZIN, SAKIT, ALPHA
    val upahDibayar: Long,
    val catatan: String?,
)

data class Kasbon(
    val id: String,
    val karyawanId: String,
    val nominal: Long,
    val catatan: String?,
    val waktu: Long,
)

data class PayrollRecord(
    val id: String,
    val karyawanId: String,
    val namaKaryawan: String,
    val periodeAwal: Long,
    val periodeAkhir: Long,
    val totalUpah: Long,
    val totalKasbon: Long,
    val dibayar: Long,
    val waktu: Long,
)

data class GajiKaryawan(
    val karyawan: Employee,
    val hadir: Int,
    val izin: Int,
    val sakit: Int,
    val alpha: Int,
    val totalUpah: Long,
    val totalKasbon: Long,
    val dibayar: Long,
    val sudahDibayar: Boolean = false,
)

data class BusinessProfile(
    val id: String,
    val namaUsaha: String,
    val jenisUsaha: String,
    val modalAwal: Long,
    val dibuatPada: Long,
)

data class Category(val id: String, val nama: String, val jenis: String)

data class Product(
    val id: String,
    val nama: String,
    val kategoriId: String?,
    val satuan: String,
    val barcode: String?,
    val hargaBeliRata2: Long,
    val hargaJual: Long,
    val stok: Double,
    val stokMinimum: Double,
    val fotoUri: String?,
    val diarsipkan: Boolean,
    val isPinned: Boolean = false,
) {
    val isLowStock: Boolean get() = stok <= stokMinimum
}

data class StockMovement(
    val id: String,
    val produkId: String,
    val produkNama: String,
    val tipe: String,
    val jumlah: Double,
    val hargaSatuan: Long,
    val referensiId: String?,
    val catatan: String?,
    val waktu: Long,
)

data class CartItem(val product: Product, val qty: Double)

data class Sale(
    val id: String,
    val subtotal: Long,
    val diskon: Long,
    val total: Long,
    val metodeBayar: String,
    val status: String,
    val waktu: Long,
)

data class SaleLine(
    val produkId: String,
    val namaProduk: String,
    val jumlah: Double,
    val hargaJual: Long,
    val hppSaatJual: Long,
)

data class Expense(
    val id: String,
    val kategoriNama: String,
    val nominal: Long,
    val catatan: String?,
    val waktu: Long,
)

data class SupplierSpend(
    val pemasok: String,
    val totalSpent: Long,
    val purchaseCount: Int,
    val lastPurchaseAt: Long,
)

data class PurchaseRecord(
    val id: String,
    val pemasok: String?,
    val total: Long,
    val catatan: String?,
    val waktu: Long,
)

data class Customer(
    val id: String,
    val nama: String,
    val poin: Long,
    val kunjungan: Int,
    val totalBelanja: Long,
)

data class Receivable(
    val id: String,
    val namaPelanggan: String,
    val saleId: String?,
    val nominal: Long,
    val terbayar: Long,
    val jatuhTempo: Long?,
    val catatan: String?,
    val waktu: Long,
) {
    val sisa: Long get() = (nominal - terbayar).coerceAtLeast(0L)
    val isLunas: Boolean get() = terbayar >= nominal
}

data class Payable(
    val id: String,
    val pemasok: String,
    val nominal: Long,
    val terbayar: Long,
    val jatuhTempo: Long?,
    val catatan: String?,
    val waktu: Long,
) {
    val sisa: Long get() = (nominal - terbayar).coerceAtLeast(0L)
    val isLunas: Boolean get() = terbayar >= nominal
    val isOverdue: Boolean get() = jatuhTempo != null && jatuhTempo < System.currentTimeMillis() && !isLunas
}

data class CapitalEntry(
    val id: String,
    val tipe: String,
    val nominal: Long,
    val catatan: String?,
    val waktu: Long,
)

data class PeriodReport(
    val omzet: Long,
    val hpp: Long,
    val pengeluaran: Long,
    val labaKotor: Long,
    val labaBersih: Long,
    val marginPersen: Double,
    val jumlahTransaksi: Int,
    val nilaiPersediaan: Long,
) {
    companion object {
        fun empty() = PeriodReport(0, 0, 0, 0, 0, 0.0, 0, 0)
    }
}

data class DailyPoint(val label: String, val omzet: Long, val laba: Long)

data class CategorySpend(val nama: String, val total: Long)

data class CashBreakdown(
    val modal: Long,
    val prive: Long,
    val penjualanTunai: Long,
    val penerimaanPiutang: Long,
    val pembelianStok: Long,
    val pembayaranHutang: Long,
    val kasbon: Long,
    val pengeluaran: Long,
) {
    val totalMasuk: Long get() = modal + penjualanTunai + penerimaanPiutang
    val totalKeluar: Long get() = prive + pembelianStok + pembayaranHutang + kasbon + pengeluaran
    val saldo: Long get() = totalMasuk - totalKeluar
}

data class Neraca(
    val kas: Long,
    val piutang: Long,
    val persediaan: Long,
    val kasbon: Long,
    val hutangUsaha: Long,
    val modalBersih: Long,
    val labaDitahan: Long,
) {
    val aset: Long get() = kas + piutang + persediaan + kasbon
    val liabilitas: Long get() = hutangUsaha
    /** Net worth is derived from observed assets and liabilities. */
    val ekuitas: Long get() = aset - liabilitas
}

data class ProductSales(val namaProduk: String, val qty: Double)

data class ProductProfit(val namaProduk: String, val qty: Double, val profit: Long)

sealed class FinanceEntry(val waktu: Long) {
    data class SaleEntry(val sale: Sale) : FinanceEntry(sale.waktu)
    data class PurchaseEntry(val total: Long, val pemasok: String?, val waktuMillis: Long, val id: String) : FinanceEntry(waktuMillis)
    data class ExpenseEntry(val expense: Expense) : FinanceEntry(expense.waktu)
    data class Capital(val entry: CapitalEntry) : FinanceEntry(entry.waktu)
}
