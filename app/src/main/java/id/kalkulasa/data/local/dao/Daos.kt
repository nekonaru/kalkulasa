package id.kalkulasa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.kalkulasa.data.local.entity.BusinessProfileEntity
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.data.local.entity.CategoryEntity
import id.kalkulasa.data.local.entity.ChangeLogEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.PurchaseItemEntity
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.SaleItemEntity
import id.kalkulasa.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)
}

@Dao
interface ProductDao {
    @Query(
        """SELECT * FROM product WHERE deletedAt IS NULL AND diarsipkan = 0
           AND (:query = '' OR nama LIKE '%' || :query || '%' OR barcode = :query)
           ORDER BY nama"""
    )
    fun observeActive(query: String): Flow<List<ProductEntity>>

    @Query(
        """SELECT * FROM product WHERE deletedAt IS NULL
           AND (:query = '' OR nama LIKE '%' || :query || '%' OR barcode = :query)
           ORDER BY diarsipkan, nama"""
    )
    fun observeAll(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM product WHERE deletedAt IS NULL")
    suspend fun getAllActive(): List<ProductEntity>

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
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movement WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<StockMovementEntity>>

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

    @Query("SELECT * FROM purchase WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to ORDER BY waktu DESC")
    suspend fun getBetween(from: Long, to: Long): List<PurchaseEntity>

    @Query("SELECT * FROM purchase WHERE deletedAt IS NULL")
    suspend fun getAll(): List<PurchaseEntity>

    @Query("SELECT * FROM purchase_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<PurchaseItemEntity>

    @Query("SELECT COALESCE(SUM(total), 0) FROM purchase WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumBetween(from: Long, to: Long): Long
}

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SaleItemEntity)

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to ORDER BY waktu DESC")
    suspend fun getBetween(from: Long, to: Long): List<SaleEntity>

    @Query("SELECT * FROM sale_item WHERE saleId = :saleId AND deletedAt IS NULL")
    suspend fun getItems(saleId: String): List<SaleItemEntity>

    @Query("SELECT * FROM sale_item WHERE deletedAt IS NULL")
    suspend fun getAllItems(): List<SaleItemEntity>

    @Query("SELECT * FROM sale WHERE deletedAt IS NULL")
    suspend fun getAll(): List<SaleEntity>

    @Query("SELECT COALESCE(SUM(total), 0) FROM sale WHERE deletedAt IS NULL AND status = 'SELESAI' AND waktu BETWEEN :from AND :to")
    suspend fun sumRevenueBetween(from: Long, to: Long): Long

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

    @Query("UPDATE sale SET status = 'BATAL', updatedAt = :now WHERE id = :id")
    suspend fun markCancelled(id: String, now: Long)
}

data class ProductSalesProjection(val namaProduk: String, val qty: Double)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense WHERE deletedAt IS NULL ORDER BY waktu DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense WHERE deletedAt IS NULL")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM expense WHERE deletedAt IS NULL AND waktu BETWEEN :from AND :to")
    suspend fun sumBetween(from: Long, to: Long): Long

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
interface ChangeLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ChangeLogEntity)

    @Query("SELECT * FROM change_log WHERE syncStatus = 'PENDING' ORDER BY createdAt")
    suspend fun getPending(): List<ChangeLogEntity>
}
