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
import id.kalkulasa.data.local.entity.CustomerEntity
import id.kalkulasa.data.local.entity.EmployeeEntity
import id.kalkulasa.data.local.entity.AttendanceEntity
import id.kalkulasa.data.local.entity.KasbonEntity
import id.kalkulasa.data.local.entity.PayrollEntity
import id.kalkulasa.data.local.entity.PayableEntity
import id.kalkulasa.data.local.entity.PayablePaymentEntity
import id.kalkulasa.data.local.entity.VoucherEntity
import id.kalkulasa.data.local.entity.DepositEntity
import id.kalkulasa.data.local.entity.SalesTargetEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.PurchaseItemEntity
import id.kalkulasa.data.local.entity.ReceivableEntity
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
        ReceivableEntity::class,
        CustomerEntity::class,
        EmployeeEntity::class,
        AttendanceEntity::class,
        KasbonEntity::class,
        PayrollEntity::class,
        PayableEntity::class,
        PayablePaymentEntity::class,
        VoucherEntity::class,
        DepositEntity::class,
        SalesTargetEntity::class,
    ],
    version = 10,
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
    abstract fun receivableDao(): id.kalkulasa.data.local.dao.ReceivableDao
    abstract fun customerDao(): id.kalkulasa.data.local.dao.CustomerDao
    abstract fun employeeDao(): id.kalkulasa.data.local.dao.EmployeeDao
    abstract fun attendanceDao(): id.kalkulasa.data.local.dao.AttendanceDao
    abstract fun kasbonDao(): id.kalkulasa.data.local.dao.KasbonDao
    abstract fun payrollDao(): id.kalkulasa.data.local.dao.PayrollDao
    abstract fun payableDao(): id.kalkulasa.data.local.dao.PayableDao
    abstract fun salesTargetDao(): id.kalkulasa.data.local.dao.SalesTargetDao
    abstract fun payablePaymentDao(): id.kalkulasa.data.local.dao.PayablePaymentDao
    abstract fun voucherDao(): id.kalkulasa.data.local.dao.VoucherDao
    abstract fun depositDao(): id.kalkulasa.data.local.dao.DepositDao

    companion object {
        const val NAME = "kalkulasa.db"

        /** Migration 9 -> 10: vouchers, deposits, wholesale price, favourites and PPN. */
        val MIGRATION_9_10 = androidx.room.migration.Migration(9, 10) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `voucher` (
                    `id` TEXT NOT NULL PRIMARY KEY, `kode` TEXT NOT NULL, `tipe` TEXT NOT NULL,
                    `nilai` INTEGER NOT NULL, `minimalBelanja` INTEGER NOT NULL,
                    `kuota` INTEGER NOT NULL, `terpakai` INTEGER NOT NULL,
                    `berlakuMulai` INTEGER NOT NULL, `berlakuSampai` INTEGER NOT NULL,
                    `aktif` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_voucher_kode` ON `voucher` (`kode`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `deposit` (
                    `id` TEXT NOT NULL PRIMARY KEY, `namaPelanggan` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL, `terpakai` INTEGER NOT NULL,
                    `waktu` INTEGER NOT NULL, `catatan` TEXT, `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER, `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_deposit_namaPelanggan` ON `deposit` (`namaPelanggan`)")
            db.execSQL("ALTER TABLE `product` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `product` ADD COLUMN `hargaGrosir` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `product` ADD COLUMN `qtyGrosir` REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `ppn` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `voucherKode` TEXT")
        }

        /** Migration 8 -> 9: supplier payment history and kasbon settlement flag. */
        val MIGRATION_8_9 = androidx.room.migration.Migration(8, 9) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `payable_payment` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `payableId` TEXT NOT NULL,
                    `pemasok` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL,
                    `catatan` TEXT,
                    `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payable_payment_waktu` ON `payable_payment` (`waktu`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payable_payment_payableId` ON `payable_payment` (`payableId`)")
            db.execSQL("ALTER TABLE `kasbon` ADD COLUMN `terpakai` INTEGER NOT NULL DEFAULT 0")
        }

        /** Migration 7 -> 8: remember redeemed points so cancellation can refund them. */
        val MIGRATION_7_8 = androidx.room.migration.Migration(7, 8) { db ->
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `poinDitukar` INTEGER NOT NULL DEFAULT 0")
        }

        /** Migration 6 -> 7: daily and monthly sales targets. */
        val MIGRATION_6_7 = androidx.room.migration.Migration(6, 7) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `sales_target` (
                    `jenis` TEXT NOT NULL PRIMARY KEY,
                    `nominal` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
        }

        /** Migration 5 -> 6: supplier payables (hutang usaha). */
        val MIGRATION_5_6 = androidx.room.migration.Migration(5, 6) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `payable` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `pemasok` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL,
                    `terbayar` INTEGER NOT NULL,
                    `jatuhTempo` INTEGER,
                    `catatan` TEXT,
                    `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payable_pemasok` ON `payable` (`pemasok`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payable_jatuhTempo` ON `payable` (`jatuhTempo`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payable_waktu` ON `payable` (`waktu`)")
        }

        /** Migration 4 -> 5: employees, attendance, kasbon and payroll tables. */
        val MIGRATION_4_5 = androidx.room.migration.Migration(4, 5) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `employee` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `nama` TEXT NOT NULL,
                    `jabatan` TEXT,
                    `telepon` TEXT,
                    `upahHarian` INTEGER NOT NULL,
                    `aktif` INTEGER NOT NULL,
                    `dibuatPada` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_employee_nama` ON `employee` (`nama`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `attendance` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `karyawanId` TEXT NOT NULL,
                    `tanggal` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    `upahDibayar` INTEGER NOT NULL,
                    `catatan` TEXT,
                    `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_karyawanId` ON `attendance` (`karyawanId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_tanggal` ON `attendance` (`tanggal`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `kasbon` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `karyawanId` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL,
                    `catatan` TEXT,
                    `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_kasbon_karyawanId` ON `kasbon` (`karyawanId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_kasbon_waktu` ON `kasbon` (`waktu`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `payroll` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `karyawanId` TEXT NOT NULL,
                    `namaKaryawan` TEXT NOT NULL,
                    `periodeAwal` INTEGER NOT NULL,
                    `periodeAkhir` INTEGER NOT NULL,
                    `totalUpah` INTEGER NOT NULL,
                    `totalKasbon` INTEGER NOT NULL,
                    `dibayar` INTEGER NOT NULL,
                    `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_karyawanId` ON `payroll` (`karyawanId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_waktu` ON `payroll` (`waktu`)")
        }

        /** Migration 3 -> 4: add product.isPinned for the cashier fast-lane. */
        val MIGRATION_3_4 = androidx.room.migration.Migration(3, 4) { db ->
            db.execSQL("ALTER TABLE `product` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
        }

        /** Migration 1 -> 2: add the receivable (piutang) table. */
        val MIGRATION_1_2 = androidx.room.migration.Migration(1, 2) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `receivable` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `namaPelanggan` TEXT NOT NULL,
                    `saleId` TEXT,
                    `nominal` INTEGER NOT NULL,
                    `terbayar` INTEGER NOT NULL,
                    `jatuhTempo` INTEGER,
                    `catatan` TEXT,
                    `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_receivable_namaPelanggan` ON `receivable` (`namaPelanggan`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_receivable_jatuhTempo` ON `receivable` (`jatuhTempo`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_receivable_saleId` ON `receivable` (`saleId`)")
        }

        /** Migration 2 -> 3: customer loyalty table + sale.customerId link. */
        val MIGRATION_2_3 = androidx.room.migration.Migration(2, 3) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `customer` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `nama` TEXT NOT NULL,
                    `poin` INTEGER NOT NULL,
                    `kunjungan` INTEGER NOT NULL,
                    `totalBelanja` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL,
                    `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customer_nama` ON `customer` (`nama`)")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `customerId` TEXT")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_customerId` ON `sale` (`customerId`)")
        }

        fun build(context: Context, deviceId: String, passphrase: ByteArray): KalkulasaDatabase =
            Room.databaseBuilder(context, KalkulasaDatabase::class.java, NAME)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
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
