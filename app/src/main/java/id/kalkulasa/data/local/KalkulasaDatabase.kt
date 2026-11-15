package id.kalkulasa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import id.kalkulasa.data.local.dao.BusinessProfileDao
import id.kalkulasa.data.local.dao.CapitalEntryDao
import id.kalkulasa.data.local.dao.CategoryDao
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.dao.ExpenseDao
import id.kalkulasa.data.local.dao.ProductDao
import id.kalkulasa.data.local.dao.PurchaseDao
import id.kalkulasa.data.local.dao.SaleDao
import id.kalkulasa.data.local.dao.StockMovementDao
import id.kalkulasa.data.local.entity.BusinessProfileEntity
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.data.local.entity.CategoryEntity
import id.kalkulasa.domain.model.CategoryType
import id.kalkulasa.data.local.entity.ChangeLogEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.PurchaseItemEntity
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.SaleItemEntity
import id.kalkulasa.data.local.entity.StockMovementEntity
import java.util.UUID

@Database(
    entities = [
        BusinessProfileEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        StockMovementEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ExpenseEntity::class,
        CapitalEntryEntity::class,
        ChangeLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KalkulasaDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun capitalEntryDao(): CapitalEntryDao
    abstract fun changeLogDao(): ChangeLogDao

    companion object {
        const val NAME = "kalkulasa.db"

        fun build(context: Context, deviceId: String, passphrase: ByteArray): KalkulasaDatabase =
            Room.databaseBuilder(context, KalkulasaDatabase::class.java, NAME)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .addCallback(SeedCallback(deviceId))
                .build()
    }

    /** Seed default expense categories on first creation. */
    private class SeedCallback(private val deviceId: String) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val now = System.currentTimeMillis()
            val defaults = listOf("Listrik", "Sewa", "Gaji", "Transportasi", "Lainnya")
            defaults.forEach { name ->
                db.execSQL(
                    "INSERT INTO category (id, nama, jenis, updatedAt, deletedAt, deviceId, syncStatus) " +
                        "VALUES (?, ?, ?, ?, NULL, ?, 'PENDING')",
                    arrayOf(UUID.randomUUID().toString(), name, CategoryType.EXPENSE, now, deviceId),
                )
            }
        }
    }
}
