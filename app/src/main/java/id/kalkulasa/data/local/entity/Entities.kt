package id.kalkulasa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import id.kalkulasa.domain.model.SyncStatus

/**
 * Every table stores a UUID primary key plus sync columns so the database is
 * ready for cloud sync without a painful migration later. Deletion is a soft
 * delete through `deletedAt`. Money is stored as Long (rupiah, no decimals).
 */

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: String,
    val namaUsaha: String,
    val jenisUsaha: String,
    val modalAwal: Long,
    val dibuatPada: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "category", indices = [Index("jenis")])
data class CategoryEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val jenis: String, // PRODUCT or EXPENSE
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(
    tableName = "product",
    indices = [Index("nama"), Index("barcode"), Index("kategoriId"), Index("diarsipkan")],
)
data class ProductEntity(
    @PrimaryKey val id: String,
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
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(
    tableName = "stock_movement",
    indices = [Index("produkId"), Index("waktu"), Index("tipe")],
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val produkId: String,
    val tipe: String, // MASUK, KELUAR, PENYESUAIAN, RETUR
    val jumlah: Double, // always positive; direction is derived from tipe
    val hargaSatuan: Long,
    val referensiId: String?,
    val catatan: String?,
    val waktu: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "purchase", indices = [Index("waktu")])
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val pemasok: String?,
    val total: Long,
    val waktu: Long,
    val catatan: String?,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "purchase_item", indices = [Index("purchaseId"), Index("produkId")])
data class PurchaseItemEntity(
    @PrimaryKey val id: String,
    val purchaseId: String,
    val produkId: String,
    val jumlah: Double,
    val hargaBeli: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "sale", indices = [Index("waktu"), Index("status")])
data class SaleEntity(
    @PrimaryKey val id: String,
    val subtotal: Long,
    val diskon: Long,
    val total: Long,
    val metodeBayar: String, // TUNAI, TRANSFER, QRIS
    val status: String, // SELESAI, BATAL
    val waktu: Long,
    val catatan: String?,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "sale_item", indices = [Index("saleId"), Index("produkId")])
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val produkId: String,
    val namaProduk: String,
    val jumlah: Double,
    val hargaJual: Long,
    val hppSaatJual: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "expense", indices = [Index("waktu"), Index("kategoriId")])
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val kategoriId: String?, // category with jenis = EXPENSE, nullable for "Lainnya"
    val kategoriNama: String,
    val nominal: Long,
    val catatan: String?,
    val waktu: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "capital_entry", indices = [Index("waktu"), Index("tipe")])
data class CapitalEntryEntity(
    @PrimaryKey val id: String,
    val tipe: String, // MODAL_AWAL, TAMBAH_MODAL, PRIVE
    val nominal: Long,
    val catatan: String?,
    val waktu: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "receivable", indices = [Index("namaPelanggan"), Index("jatuhTempo"), Index("saleId")])
data class ReceivableEntity(
    @PrimaryKey val id: String,
    val namaPelanggan: String,
    val saleId: String? = null,
    val nominal: Long,
    val terbayar: Long,
    val jatuhTempo: Long? = null,
    val catatan: String? = null,
    val waktu: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val deviceId: String,
    val syncStatus: String = SyncStatus.PENDING,
)

@Entity(tableName = "change_log", indices = [Index("createdAt"), Index("syncStatus")])
data class ChangeLogEntity(
    @PrimaryKey val id: String,
    val entitas: String,
    val entitasId: String,
    val aksi: String, // INSERT, UPDATE, DELETE
    val createdAt: Long,
    val syncStatus: String = SyncStatus.PENDING,
)


