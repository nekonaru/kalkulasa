package id.kalkulasa.domain.model

/** Pure Kotlin domain models. No Android or Room dependencies. */

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

data class ProductSales(val namaProduk: String, val qty: Double)

data class ProductProfit(val namaProduk: String, val qty: Double, val profit: Long)

sealed class FinanceEntry(val waktu: Long) {
    data class SaleEntry(val sale: Sale) : FinanceEntry(sale.waktu)
    data class PurchaseEntry(val total: Long, val pemasok: String?, val waktuMillis: Long, val id: String) : FinanceEntry(waktuMillis)
    data class ExpenseEntry(val expense: Expense) : FinanceEntry(expense.waktu)
    data class Capital(val entry: CapitalEntry) : FinanceEntry(entry.waktu)
}
