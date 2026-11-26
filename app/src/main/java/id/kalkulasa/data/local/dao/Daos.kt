package id.kalkulasa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.kalkulasa.data.local.entity.AttendanceEntity
import id.kalkulasa.data.local.entity.BusinessProfileEntity
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.data.local.entity.CategoryEntity
import id.kalkulasa.data.local.entity.ChangeLogEntity
import id.kalkulasa.data.local.entity.CustomerEntity
import id.kalkulasa.data.local.entity.EmployeeEntity
import id.kalkulasa.data.local.entity.KasbonEntity
import id.kalkulasa.data.local.entity.PayrollEntity
import id.kalkulasa.data.local.entity.PayableEntity
import id.kalkulasa.data.local.entity.SalesTargetEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.PurchaseItemEntity
import id.kalkulasa.data.local.entity.ReceivableEntity
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.SaleItemEntity
import id.kalkulasa.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

/** Projection for the expense-by-category aggregate query. */
data class CategorySpendRow(val nama: String, val total: Long)

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE deletedAt IS NULL LIMIT 1")
    fun observeProfile(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profile WHERE deletedAt IS NULL LIMIT 1")
    suspend fun getProfile(): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: BusinessProfileEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE deletedAt IS NULL AND jenis = :jenis ORDER BY nama")
    fun observeByType(jenis: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE deletedAt IS NULL ORDER BY nama")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE deletedAt IS NULL ORDER BY nama")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    @Query("UPDATE category SET nama = :nama, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: String, nama: String, now: Long)

    @Query("UPDATE category SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)
}

@Dao
interface ProductDao {
    @Query(
        """SELECT * FROM product WHERE deletedAt IS NULL AND diarsipkan = 0
           AND (:query = '' OR nama LIKE '%' || :query || '%' OR barcode = :query)
           ORDER BY isPinned DESC, nama"""
    )
    fun observeActive(query: String): Flow<List<ProductEntity>>

    @Query(
        """SELECT * FROM product WHERE deletedAt IS NULL
           AND (:query = '' OR nama LIKE '%' || :query || '%' OR barcode = :query)
           ORDER BY isPinned DESC, diarsipkan, nama"""
    )
    fun observeAll(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM product WHERE barcode = :barcode AND deletedAt IS NULL AND diarsipkan = 0 LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM product WHERE deletedAt IS NULL")
    suspend fun getAllActive(): List<ProductEntity>

    @Query("SELECT * FROM product WHERE deletedAt IS NULL AND diarsipkan = 0")
    suspend fun getActiveForInventory(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM product WHERE deletedAt IS NULL AND diarsipkan = 0 AND stok <= stokMinimum")
    fun observeLowStockCount(): Flow<Int>

    @Query("SELECT * FROM product WHERE deletedAt IS NULL AND diarsipkan = 0 AND stok <= stokMinimum ORDER BY nama")
    fun observeLowStock(): Flow<List<ProductEntity>>

    @Query("SELECT COALESCE(SUM(stok * hargaBeliRata2), 0) FROM product WHERE deletedAt IS NULL AND diarsipkan = 0")
    fun observeInventoryValue(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE product SET diarsipkan = :archived, updatedAt = :now WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, now: Long)

    @Query("UPDATE product SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean, now: Long)
}

data class MovementWithProduct(
    val id: String,
    val produkId: String,
    val tipe: String,
    val jumlah: Double,
    val hargaSatuan: Long,
    val referensiId: String?,
    val catatan: String?,
    val waktu: Long,
    val produkNama: String,
)

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movement WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<StockMovementEntity>>

    @Query(
        """SELECT m.id, m.produkId, m.tipe, m.jumlah, m.hargaSatuan, m.referensiId, m.catatan, m.waktu,
                  p.nama AS produkNama
           FROM stock_movement m JOIN product p ON p.id = m.produkId
           WHERE m.deletedAt IS NULL ORDER BY m.waktu DESC LIMIT :limit"""
    )
    fun observeRecentWithProduct(limit: Int): Flow<List<MovementWithProduct>>

    @Query("SELECT * FROM stock_movement WHERE produkId = :productId AND deletedAt IS NULL ORDER BY waktu DESC")
    fun observeByProduct(productId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movement WHERE deletedAt IS NULL")
    suspend fun getAll(): List<StockMovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovementEntity)
}

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PurchaseItemEntity)

    @Query("SELECT * FROM purchase WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int): kotlinx.coroutines.flow.Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchase WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to ORDER BY waktu DESC")
    suspend fun getBetween(from: Long, to: Long): List<PurchaseEntity>

    @Query(
        """SELECT pemasok, SUM(total) AS totalSpent, COUNT(*) AS purchaseCount, MAX(waktu) AS lastPurchaseAt
           FROM purchase WHERE deletedAt IS NULL
           GROUP BY LOWER(TRIM(COALESCE(pemasok, '')))
           ORDER BY totalSpent DESC"""
    )
    suspend fun supplierSpend(): List<SupplierSpendProjection>

    @Query(
        """SELECT * FROM purchase WHERE deletedAt IS NULL
           AND LOWER(TRIM(COALESCE(pemasok, ''))) = LOWER(TRIM(:name))
           AND waktu BETWEEN :from AND :to
           ORDER BY waktu DESC"""
    )
    suspend fun purchasesFromSupplier(name: String, from: Long, to: Long): List<PurchaseEntity>

    @Query("SELECT * FROM purchase WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PurchaseEntity>

    @Query("SELECT * FROM purchase_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<PurchaseItemEntity>

    @Query("SELECT COALESCE(SUM(total), 0) FROM purchase WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumBetween(from: Long, to: Long): Long
}

data class DailyRevenueProjection(val day: String, val revenue: Long)
data class DailyCogsProjection(val day: String, val cogs: Long)

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SaleItemEntity)

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale WHERE id = :id")
    suspend fun getById(id: String): SaleEntity?

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to ORDER BY waktu DESC")
    suspend fun getBetween(from: Long, to: Long): List<SaleEntity>

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL AND status = 'SELESAI' ORDER BY waktu DESC LIMIT 1")
    suspend fun lastCompleted(): SaleEntity?

    @Query("SELECT * FROM sale_item WHERE saleId = :saleId AND deletedAt IS NULL")
    suspend fun getItems(saleId: String): List<SaleItemEntity>

    @Query("SELECT * FROM sale_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<SaleItemEntity>

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL")
    suspend fun getAll(): List<SaleEntity>

    @Query(
        """SELECT substr(datetime(waktu / 1000, 'unixepoch', 'localtime'), 1, 10) AS day,
                  COALESCE(SUM(total), 0) AS revenue
           FROM sale
           WHERE deletedAt IS NULL AND status = 'SELESAI' AND waktu BETWEEN :from AND :to
           GROUP BY day"""
    )
    suspend fun dailyRevenue(from: Long, to: Long): List<DailyRevenueProjection>

    @Query(
        """SELECT substr(datetime(s.waktu / 1000, 'unixepoch', 'localtime'), 1, 10) AS day,
                  COALESCE(SUM(si.jumlah * si.hppSaatJual), 0) AS cogs
           FROM sale_item si JOIN sale s ON s.id = si.saleId
           WHERE si.deletedAt IS NULL AND s.deletedAt IS NULL AND s.status = 'SELESAI'
           AND s.waktu BETWEEN :from AND :to
           GROUP BY day"""
    )
    suspend fun dailyCogs(from: Long, to: Long): List<DailyCogsProjection>

    @Query("SELECT COALESCE(SUM(total), 0) FROM sale WHERE deletedAt IS NULL AND status = 'SELESAI' AND waktu BETWEEN :from AND :to")
    suspend fun sumRevenueBetween(from: Long, to: Long): Long

    @Query("SELECT COALESCE(SUM(total), 0) FROM sale WHERE deletedAt IS NULL AND status = 'SELESAI' AND metodeBayar != :method AND waktu BETWEEN :from AND :to")
    suspend fun sumRevenueBetweenExcludingMethod(from: Long, to: Long, method: String): Long

    @Query("SELECT COUNT(*) FROM sale WHERE deletedAt IS NULL AND status = 'SELESAI' AND waktu BETWEEN :from AND :to")
    suspend fun countBetween(from: Long, to: Long): Int

    @Query("""SELECT COALESCE(SUM(jumlah * hppSaatJual), 0) FROM sale_item si
              JOIN sale s ON s.id = si.saleId
              WHERE si.deletedAt IS NULL AND s.deletedAt IS NULL AND s.status = 'SELESAI'
              AND s.waktu BETWEEN :from AND :to""")
    suspend fun sumCogsBetween(from: Long, to: Long): Long

    @Query("""SELECT si.namaProduk, SUM(si.jumlah) AS qty FROM sale_item si
              JOIN sale s ON s.id = si.saleId
              WHERE si.deletedAt IS NULL AND s.deletedAt IS NULL AND s.status = 'SELESAI'
              AND s.waktu BETWEEN :from AND :to
              GROUP BY si.namaProduk ORDER BY qty DESC LIMIT :limit""")
    suspend fun topProductsBetween(from: Long, to: Long, limit: Int): List<ProductSalesProjection>

    @Query("""SELECT si.namaProduk, SUM(si.jumlah) AS qty,
              SUM(si.jumlah * (si.hargaJual - si.hppSaatJual)) AS profit
              FROM sale_item si
              JOIN sale s ON s.id = si.saleId
              WHERE si.deletedAt IS NULL AND s.deletedAt IS NULL AND s.status = 'SELESAI'
              AND s.waktu BETWEEN :from AND :to
              GROUP BY si.namaProduk ORDER BY profit DESC LIMIT :limit""")
    suspend fun mostProfitableBetween(from: Long, to: Long, limit: Int): List<ProductProfitProjection>

    @Query("UPDATE sale SET status = 'BATAL', updatedAt = :now WHERE id = :id")
    suspend fun markCancelled(id: String, now: Long)
}

data class ProductSalesProjection(val namaProduk: String, val qty: Double)

data class ProductProfitProjection(val namaProduk: String, val qty: Double, val profit: Long)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense WHERE deletedAt IS NULL")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM expense WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumBetween(from: Long, to: Long): Long

    @Query("SELECT kategoriNama AS nama, COALESCE(SUM(nominal), 0) AS total FROM expense WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to GROUP BY kategoriNama ORDER BY total DESC")
    suspend fun sumByCategoryBetween(from: Long, to: Long): List<CategorySpendRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)
}

@Dao
interface CapitalEntryDao {
    @Query("SELECT * FROM capital_entry WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<CapitalEntryEntity>>

    @Query("SELECT * FROM capital_entry WHERE deletedAt IS NULL")
    suspend fun getAll(): List<CapitalEntryEntity>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM capital_entry WHERE deletedAt IS NULL AND tipe IN ('MODAL_AWAL', 'TAMBAH_MODAL')")
    suspend fun sumCapital(): Long

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM capital_entry WHERE deletedAt IS NULL AND tipe = 'PRIVE'")
    suspend fun sumPrivateDraws(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CapitalEntryEntity)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employee WHERE deletedAt IS NULL ORDER BY aktif DESC, nama")
    fun observeAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employee WHERE deletedAt IS NULL ORDER BY nama")
    suspend fun getAll(): List<EmployeeEntity>

    @Query("SELECT * FROM employee WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(employee: EmployeeEntity)

    @Query("UPDATE employee SET aktif = :aktif, updatedAt = :now WHERE id = :id")
    suspend fun setActive(id: String, aktif: Boolean, now: Long)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE deletedAt IS NULL AND tanggal BETWEEN :from AND :to ORDER BY waktu DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE deletedAt IS NULL AND tanggal BETWEEN :from AND :to")
    suspend fun getBetween(from: Long, to: Long): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE karyawanId = :employeeId AND tanggal = :tanggal AND deletedAt IS NULL LIMIT 1")
    suspend fun getForEmployeeAndDate(employeeId: String, tanggal: Long): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: AttendanceEntity)
}

@Dao
interface KasbonDao {
    @Query("SELECT * FROM kasbon WHERE deletedAt IS NULL AND karyawanId = :employeeId AND waktu BETWEEN :from AND :to ORDER BY waktu DESC")
    suspend fun getForEmployee(employeeId: String, from: Long, to: Long): List<KasbonEntity>

    @Query("SELECT * FROM kasbon WHERE deletedAt IS NULL ORDER BY waktu DESC")
    fun observeAll(): Flow<List<KasbonEntity>>

    @Query("SELECT * FROM kasbon WHERE deletedAt IS NULL")
    suspend fun getAll(): List<KasbonEntity>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM kasbon WHERE deletedAt IS NULL")
    suspend fun sumAll(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kasbon: KasbonEntity)
}

@Dao
interface PayrollDao {
    @Query("SELECT * FROM payroll WHERE deletedAt IS NULL ORDER BY waktu DESC")
    fun observeAll(): Flow<List<PayrollEntity>>

    @Query("SELECT * FROM payroll WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PayrollEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(payroll: PayrollEntity)
}

@Dao
interface CustomerDao {
    @Query(
        """SELECT * FROM customer WHERE deletedAt IS NULL
           AND (:query = '' OR nama LIKE '%' || :query || '%')
           ORDER BY totalBelanja DESC"""
    )
    fun observeAll(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customer WHERE deletedAt IS NULL ORDER BY totalBelanja DESC")
    suspend fun getAll(): List<CustomerEntity>

    @Query("""SELECT * FROM customer WHERE deletedAt IS NULL AND LOWER(nama) = LOWER(:name)
              ORDER BY totalBelanja DESC LIMIT 1""")
    suspend fun findByName(name: String): CustomerEntity?

    @Query("SELECT * FROM customer WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: CustomerEntity)
}

data class SupplierSpendProjection(val pemasok: String?, val totalSpent: Long, val purchaseCount: Int, val lastPurchaseAt: Long)

@Dao
interface ReceivableDao {
    @Query("SELECT * FROM receivable WHERE deletedAt IS NULL ORDER BY waktu DESC")
    fun observeAll(): Flow<List<ReceivableEntity>>

    @Query("SELECT COALESCE(SUM(nominal - terbayar), 0) FROM receivable WHERE deletedAt IS NULL AND nominal > terbayar")
    fun observeOutstanding(): Flow<Long>

    @Query("SELECT COALESCE(SUM(terbayar), 0) FROM receivable WHERE deletedAt IS NULL")
    suspend fun sumPaid(): Long

    @Query("SELECT * FROM receivable WHERE deletedAt IS NULL")
    suspend fun getAll(): List<ReceivableEntity>

    @Query("SELECT * FROM receivable WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): ReceivableEntity?

    @Query("UPDATE receivable SET deletedAt = :now, updatedAt = :now WHERE saleId = :saleId AND deletedAt IS NULL")
    suspend fun softDeleteBySaleId(saleId: String, now: Long)

    @Query("UPDATE receivable SET deletedAt = :now, updatedAt = :now WHERE saleId = :saleId AND deletedAt IS NULL AND terbayar = 0")
    suspend fun softDeleteUnpaidBySaleId(saleId: String, now: Long)

    /**
     * Keep money already received when a credit sale is cancelled. Collapsing
     * nominal to terbayar leaves zero outstanding while sumPaid() still counts
     * the cash, so cancelling never silently removes received money.
     */
    @Query("UPDATE receivable SET nominal = terbayar, jatuhTempo = NULL, updatedAt = :now WHERE saleId = :saleId AND deletedAt IS NULL AND terbayar > 0")
    suspend fun settlePaidBySaleId(saleId: String, now: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(receivable: ReceivableEntity)
}

@Dao
interface PayableDao {
    @Query("SELECT * FROM payable WHERE deletedAt IS NULL ORDER BY (jatuhTempo IS NULL), jatuhTempo, waktu DESC")
    fun observeAll(): Flow<List<PayableEntity>>

    @Query("SELECT COALESCE(SUM(nominal - terbayar), 0) FROM payable WHERE deletedAt IS NULL AND nominal > terbayar")
    fun observeOutstanding(): Flow<Long>

    @Query("SELECT COALESCE(SUM(terbayar), 0) FROM payable WHERE deletedAt IS NULL")
    suspend fun sumPaid(): Long

    @Query("SELECT * FROM payable WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PayableEntity>

    @Query("SELECT * FROM payable WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): PayableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(payable: PayableEntity)
}

@Dao
interface SalesTargetDao {
    @Query("SELECT * FROM sales_target")
    fun observeAll(): Flow<List<SalesTargetEntity>>

    @Query("SELECT * FROM sales_target")
    suspend fun getAll(): List<SalesTargetEntity>

    @Query("SELECT * FROM sales_target WHERE jenis = :jenis")
    suspend fun get(jenis: String): SalesTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(target: SalesTargetEntity)
}

@Dao
interface ChangeLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ChangeLogEntity)

    @Query("SELECT * FROM change_log WHERE syncStatus = 'PENDING' ORDER BY createdAt")
    suspend fun getPending(): List<ChangeLogEntity>

    @Query("SELECT * FROM change_log ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ChangeLogEntity>>
}
