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
import id.kalkulasa.data.local.entity.PayablePaymentEntity
import id.kalkulasa.data.local.entity.SupplierEntity
import id.kalkulasa.data.local.entity.RecurringExpenseEntity
import id.kalkulasa.data.local.entity.ChecklistItemEntity
import id.kalkulasa.data.local.entity.ChecklistLogEntity
import id.kalkulasa.data.local.entity.ProfitShareEntity
import id.kalkulasa.data.local.entity.StockTransferEntity
import id.kalkulasa.data.local.entity.StockTransferItemEntity
import id.kalkulasa.data.local.entity.VoucherUsageEntity
import id.kalkulasa.data.local.entity.PreorderEntity
import id.kalkulasa.data.local.entity.PreorderItemEntity
import id.kalkulasa.data.local.entity.BundleEntity
import id.kalkulasa.data.local.entity.BundleItemEntity
import id.kalkulasa.data.local.entity.VoucherEntity
import id.kalkulasa.data.local.entity.DepositEntity
import id.kalkulasa.data.local.entity.BudgetEntity
import id.kalkulasa.data.local.entity.CashAuditEntity
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

data class CashComponents(
    val modal: Long,
    val prive: Long,
    val salesCash: Long,
    val receivablePaid: Long,
    val purchases: Long,
    val payablePaid: Long,
    val kasbon: Long,
    val expenses: Long,
)

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

    @Query("SELECT * FROM product WHERE id IN (:ids) AND deletedAt IS NULL")
    suspend fun getByIds(ids: List<String>): List<ProductEntity>

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

    @Query("SELECT COALESCE(SUM(stok * hargaBeliRata2), 0) FROM product WHERE deletedAt IS NULL AND diarsipkan = 0")
    suspend fun inventoryValue(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Update
    suspend fun updateAll(products: List<ProductEntity>)

    @Query("UPDATE product SET diarsipkan = :archived, updatedAt = :now WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, now: Long)

    @Query("UPDATE product SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean, now: Long)

    @Query("UPDATE product SET isFavorite = :favorite, updatedAt = :now WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean, now: Long)
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

    @Query("SELECT * FROM stock_movement WHERE deletedAt IS NULL AND expiry IS NOT NULL AND expiry > 0 AND expiry <= :to ORDER BY expiry LIMIT :limit")
    suspend fun expiringBefore(to: Long, limit: Int): List<StockMovementEntity>

    @Query("SELECT * FROM stock_movement WHERE deletedAt IS NULL AND produkId = :productId AND tipe = 'MASUK' ORDER BY waktu DESC")
    suspend fun priceHistory(productId: String): List<StockMovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movements: List<StockMovementEntity>)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SaleItemEntity>)

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

    @Query("""SELECT si.namaProduk AS namaProduk, si.jumlah AS qty,
               si.jumlah * si.hargaJual AS gross, si.jumlah * si.hppSaatJual AS cogs,
               s.diskon AS diskon,
               (SELECT SUM(si2.jumlah * si2.hargaJual) FROM sale_item si2
                WHERE si2.saleId = s.id AND si2.deletedAt IS NULL) AS saleGross
               FROM sale_item si JOIN sale s ON s.id = si.saleId
               WHERE si.deletedAt IS NULL AND s.deletedAt IS NULL AND s.status = 'SELESAI'
               AND s.waktu BETWEEN :from AND :to""")
    suspend fun profitRowsBetween(from: Long, to: Long): List<ProductProfitRow>

    @Query("UPDATE sale SET status = 'BATAL', updatedAt = :now WHERE id = :id")
    suspend fun markCancelled(id: String, now: Long)
}

data class ProductSalesProjection(val namaProduk: String, val qty: Double)

data class ProductProfitProjection(val namaProduk: String, val qty: Double, val profit: Long)

data class ProductProfitRow(
    val namaProduk: String,
    val qty: Double,
    val gross: Double,
    val cogs: Double,
    val diskon: Long,
    val saleGross: Double,
)

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

    @Query("""
        SELECT
          (SELECT COALESCE(SUM(nominal), 0) FROM capital_entry WHERE deletedAt IS NULL AND tipe IN ('MODAL_AWAL','TAMBAH_MODAL')) AS modal,
          (SELECT COALESCE(SUM(nominal), 0) FROM capital_entry WHERE deletedAt IS NULL AND tipe = 'PRIVE') AS prive,
          (SELECT COALESCE(SUM(total), 0) FROM sale WHERE deletedAt IS NULL AND status = 'SELESAI' AND metodeBayar != 'HUTANG') AS salesCash,
          (SELECT COALESCE(SUM(terbayar), 0) FROM receivable WHERE deletedAt IS NULL) AS receivablePaid,
          (SELECT COALESCE(SUM(total), 0) FROM purchase WHERE deletedAt IS NULL) AS purchases,
          (SELECT COALESCE(SUM(terbayar), 0) FROM payable WHERE deletedAt IS NULL) AS payablePaid,
          (SELECT COALESCE(SUM(nominal), 0) FROM kasbon WHERE deletedAt IS NULL) AS kasbon,
          (SELECT COALESCE(SUM(nominal), 0) FROM expense WHERE deletedAt IS NULL) AS expenses
    """)
    suspend fun cashComponents(): CashComponents

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM capital_entry WHERE deletedAt IS NULL AND tipe IN ('MODAL_AWAL','TAMBAH_MODAL') AND waktu BETWEEN :from AND :to")
    suspend fun sumCapitalBetween(from: Long, to: Long): Long

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM capital_entry WHERE deletedAt IS NULL AND tipe = 'PRIVE' AND waktu BETWEEN :from AND :to")
    suspend fun sumPrivateDrawsBetween(from: Long, to: Long): Long

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

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM kasbon WHERE deletedAt IS NULL AND terpakai = 0")
    suspend fun sumUnsettled(): Long

    @Query("UPDATE kasbon SET terpakai = 1, updatedAt = :now WHERE karyawanId = :id AND terpakai = 0")
    suspend fun markUsed(id: String, now: Long)

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM kasbon WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumBetween(from: Long, to: Long): Long

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM kasbon WHERE deletedAt IS NULL")
    suspend fun sumAll(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kasbon: KasbonEntity)
}

@Dao
interface PayrollDao {
    @Query("SELECT COALESCE(SUM(dibayar), 0) FROM payroll WHERE karyawanId = :karyawanId AND periodeAwal = :periodeAwal AND deletedAt IS NULL")
    suspend fun sumPaidFor(karyawanId: String, periodeAwal: Long): Long

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

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM receivable WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumPaidBetween(from: Long, to: Long): Long

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
interface PayablePaymentDao {
    @Query("SELECT * FROM payable_payment WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<PayablePaymentEntity>>

    @Query("SELECT * FROM payable_payment WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PayablePaymentEntity>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM payable_payment WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumBetween(from: Long, to: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PayablePaymentEntity)
}

@Dao
interface PreorderDao {
    @Query("SELECT * FROM preorder WHERE deletedAt IS NULL ORDER BY waktu DESC")
    fun observeAll(): Flow<List<PreorderEntity>>

    @Query("SELECT * FROM preorder WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): PreorderEntity?

    @Query("SELECT * FROM preorder_item WHERE preorderId = :id AND deletedAt IS NULL")
    suspend fun getItems(id: String): List<PreorderItemEntity>

    @Query("SELECT * FROM preorder WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PreorderEntity>

    @Query("SELECT * FROM preorder_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<PreorderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preorder: PreorderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PreorderItemEntity>)
}

@Dao
interface BundleDao {
    @Query("SELECT * FROM bundle WHERE deletedAt IS NULL AND aktif = 1 ORDER BY nama")
    fun observeActive(): Flow<List<BundleEntity>>

    @Query("SELECT * FROM bundle WHERE deletedAt IS NULL")
    suspend fun getAll(): List<BundleEntity>

    @Query("SELECT * FROM bundle_item WHERE bundleId = :id AND deletedAt IS NULL")
    suspend fun getItems(id: String): List<BundleItemEntity>

    @Query("SELECT * FROM bundle_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<BundleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bundle: BundleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<BundleItemEntity>)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_item WHERE aktif = 1 ORDER BY nama")
    fun observeItems(): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_log WHERE tanggal = :tanggal")
    suspend fun logsFor(tanggal: Long): List<ChecklistLogEntity>

    @Query("SELECT * FROM checklist_item")
    suspend fun getAll(): List<ChecklistItemEntity>

    @Query("SELECT * FROM checklist_log")
    suspend fun getAllLogs(): List<ChecklistLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ChecklistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: ChecklistLogEntity)
}

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expense WHERE deletedAt IS NULL AND aktif = 1 ORDER BY hariPerBulan")
    fun observeActive(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expense WHERE deletedAt IS NULL")
    suspend fun getAll(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: RecurringExpenseEntity)
}

@Dao
interface ProfitShareDao {
    @Query("SELECT * FROM profit_share WHERE deletedAt IS NULL AND aktif = 1")
    fun observeActive(): Flow<List<ProfitShareEntity>>

    @Query("SELECT * FROM profit_share WHERE deletedAt IS NULL")
    suspend fun getAll(): List<ProfitShareEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ProfitShareEntity)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM supplier WHERE deletedAt IS NULL ORDER BY nama")
    fun observeAll(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM supplier WHERE deletedAt IS NULL")
    suspend fun getAll(): List<SupplierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(supplier: SupplierEntity)
}

@Dao
interface StockTransferDao {
    @Query("SELECT * FROM stock_transfer WHERE deletedAt IS NULL ORDER BY waktu DESC")
    fun observeAll(): Flow<List<StockTransferEntity>>

    @Query("SELECT * FROM stock_transfer WHERE deletedAt IS NULL")
    suspend fun getAll(): List<StockTransferEntity>

    @Query("SELECT * FROM stock_transfer_item WHERE transferId = :id AND deletedAt IS NULL")
    suspend fun getItems(id: String): List<StockTransferItemEntity>

    @Query("SELECT * FROM stock_transfer_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<StockTransferItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transfer: StockTransferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<StockTransferItemEntity>)
}

@Dao
interface VoucherUsageDao {
    @Query("SELECT * FROM voucher_usage WHERE voucherId = :id AND deletedAt IS NULL")
    suspend fun getForVoucher(id: String): List<VoucherUsageEntity>

    @Query("SELECT * FROM voucher_usage WHERE deletedAt IS NULL")
    suspend fun getAll(): List<VoucherUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: VoucherUsageEntity)
}

@Dao
interface VoucherDao {
    @Query("SELECT * FROM voucher WHERE deletedAt IS NULL ORDER BY berlakuSampai")
    fun observeAll(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM voucher WHERE kode = :kode AND deletedAt IS NULL AND aktif = 1 LIMIT 1")
    suspend fun findByCode(kode: String): VoucherEntity?

    @Query("SELECT * FROM voucher WHERE deletedAt IS NULL")
    suspend fun getAll(): List<VoucherEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(voucher: VoucherEntity)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposit WHERE deletedAt IS NULL ORDER BY waktu DESC")
    fun observeAll(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposit WHERE namaPelanggan = :nama AND deletedAt IS NULL")
    suspend fun getForCustomer(nama: String): List<DepositEntity>

    @Query("SELECT * FROM deposit WHERE deletedAt IS NULL")
    suspend fun getAll(): List<DepositEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(deposit: DepositEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget WHERE bulan = :bulan")
    fun observeForMonth(bulan: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budget")
    suspend fun getAll(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)
}

@Dao
interface CashAuditDao {
    @Query("SELECT * FROM cash_audit ORDER BY tanggal DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CashAuditEntity>>

    @Query("SELECT * FROM cash_audit")
    suspend fun getAll(): List<CashAuditEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audit: CashAuditEntity)
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
