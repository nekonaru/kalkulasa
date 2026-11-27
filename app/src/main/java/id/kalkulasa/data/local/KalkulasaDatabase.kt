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
import id.kalkulasa.data.local.entity.PreorderEntity
import id.kalkulasa.data.local.entity.PreorderItemEntity
import id.kalkulasa.data.local.entity.BundleEntity
import id.kalkulasa.data.local.entity.BundleItemEntity
import id.kalkulasa.data.local.entity.VoucherUsageEntity
import id.kalkulasa.data.local.entity.SupplierEntity
import id.kalkulasa.data.local.entity.RecurringExpenseEntity
import id.kalkulasa.data.local.entity.ChecklistItemEntity
import id.kalkulasa.data.local.entity.ChecklistLogEntity
import id.kalkulasa.data.local.entity.ProfitShareEntity
import id.kalkulasa.data.local.entity.StockTransferEntity
import id.kalkulasa.data.local.entity.StockTransferItemEntity
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
        PreorderEntity::class,
        PreorderItemEntity::class,
        BundleEntity::class,
        BundleItemEntity::class,
        VoucherUsageEntity::class,
        SupplierEntity::class,
        RecurringExpenseEntity::class,
        ChecklistItemEntity::class,
        ChecklistLogEntity::class,
        ProfitShareEntity::class,
        StockTransferEntity::class,
        StockTransferItemEntity::class,
        BudgetEntity::class,
        CashAuditEntity::class,
        SalesTargetEntity::class,
    ],
    version = 18,
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
    abstract fun preorderDao(): id.kalkulasa.data.local.dao.PreorderDao
    abstract fun bundleDao(): id.kalkulasa.data.local.dao.BundleDao
    abstract fun voucherUsageDao(): id.kalkulasa.data.local.dao.VoucherUsageDao
    abstract fun supplierDao(): id.kalkulasa.data.local.dao.SupplierDao
    abstract fun stockTransferDao(): id.kalkulasa.data.local.dao.StockTransferDao
    abstract fun recurringExpenseDao(): id.kalkulasa.data.local.dao.RecurringExpenseDao
    abstract fun profitShareDao(): id.kalkulasa.data.local.dao.ProfitShareDao
    abstract fun checklistDao(): id.kalkulasa.data.local.dao.ChecklistDao
    abstract fun budgetDao(): id.kalkulasa.data.local.dao.BudgetDao
    abstract fun cashAuditDao(): id.kalkulasa.data.local.dao.CashAuditDao

    companion object {
        const val NAME = "kalkulasa.db"

        /** Migration 17 -> 18: daily checklist. */
        val MIGRATION_17_18 = androidx.room.migration.Migration(17, 18) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `checklist_item` (
                    `id` TEXT NOT NULL PRIMARY KEY, `nama` TEXT NOT NULL, `fase` TEXT NOT NULL,
                    `aktif` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_checklist_item_fase` ON `checklist_item` (`fase`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `checklist_log` (
                    `id` TEXT NOT NULL PRIMARY KEY, `itemId` TEXT NOT NULL, `tanggal` INTEGER NOT NULL,
                    `selesai` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_checklist_log_tanggal` ON `checklist_log` (`tanggal`)")
        }

        /** Migration 16 -> 17: customer birthday. */
        val MIGRATION_16_17 = androidx.room.migration.Migration(16, 17) { db ->
            db.execSQL("ALTER TABLE `customer` ADD COLUMN `tanggalLahir` INTEGER")
        }

        /** Migration 15 -> 16: recurring expenses and profit sharing. */
        val MIGRATION_15_16 = androidx.room.migration.Migration(15, 16) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `recurring_expense` (
                    `id` TEXT NOT NULL PRIMARY KEY, `kategoriNama` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL, `hariPerBulan` INTEGER NOT NULL,
                    `aktif` INTEGER NOT NULL, `terakhirDijalankan` TEXT,
                    `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_expense_aktif` ON `recurring_expense` (`aktif`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `profit_share` (
                    `id` TEXT NOT NULL PRIMARY KEY, `namaPenerima` TEXT NOT NULL,
                    `persentase` INTEGER NOT NULL, `aktif` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_profit_share_aktif` ON `profit_share` (`aktif`)")
        }

        /** Migration 14 -> 15: suppliers, lead time and stock transfers. */
        val MIGRATION_14_15 = androidx.room.migration.Migration(14, 15) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `supplier` (
                    `id` TEXT NOT NULL PRIMARY KEY, `nama` TEXT NOT NULL, `telepon` TEXT,
                    `leadTimeDays` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER, `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_supplier_nama` ON `supplier` (`nama`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `stock_transfer` (
                    `id` TEXT NOT NULL PRIMARY KEY, `dariLokasi` TEXT NOT NULL, `keLokasi` TEXT NOT NULL,
                    `catatan` TEXT, `waktu` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER, `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transfer_waktu` ON `stock_transfer` (`waktu`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `stock_transfer_item` (
                    `id` TEXT NOT NULL PRIMARY KEY, `transferId` TEXT NOT NULL, `produkId` TEXT NOT NULL,
                    `jumlah` REAL NOT NULL, `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER, `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transfer_item_transferId` ON `stock_transfer_item` (`transferId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movement_expiry` ON `stock_movement` (`expiry`)")
        }

        /** Migration 13 -> 14: preorders, bundles, voucher usage and split payment. */
        val MIGRATION_13_14 = androidx.room.migration.Migration(13, 14) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `preorder` (
                    `id` TEXT NOT NULL PRIMARY KEY, `namaPelanggan` TEXT, `status` TEXT NOT NULL,
                    `total` INTEGER NOT NULL, `catatan` TEXT, `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_preorder_waktu` ON `preorder` (`waktu`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_preorder_status` ON `preorder` (`status`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `preorder_item` (
                    `id` TEXT NOT NULL PRIMARY KEY, `preorderId` TEXT NOT NULL, `produkId` TEXT NOT NULL,
                    `namaProduk` TEXT NOT NULL, `jumlah` REAL NOT NULL, `hargaJual` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_preorder_item_preorderId` ON `preorder_item` (`preorderId`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `bundle` (
                    `id` TEXT NOT NULL PRIMARY KEY, `nama` TEXT NOT NULL, `hargaPaket` INTEGER NOT NULL,
                    `aktif` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `bundle_item` (
                    `id` TEXT NOT NULL PRIMARY KEY, `bundleId` TEXT NOT NULL, `produkId` TEXT NOT NULL,
                    `jumlah` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bundle_item_bundleId` ON `bundle_item` (`bundleId`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `voucher_usage` (
                    `id` TEXT NOT NULL PRIMARY KEY, `voucherId` TEXT NOT NULL, `voucherKode` TEXT NOT NULL,
                    `namaPelanggan` TEXT NOT NULL, `saleId` TEXT NOT NULL, `waktu` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_voucher_usage_voucherId` ON `voucher_usage` (`voucherId`)")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `metodeBayar2` TEXT")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `nominalBayar2` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `depositTerpakai` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `sale` ADD COLUMN `cashAccountId` TEXT")
            db.execSQL("ALTER TABLE `sale_item` ADD COLUMN `diskonItem` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `voucher` ADD COLUMN `scope` TEXT NOT NULL DEFAULT 'GLOBAL'")
            db.execSQL("ALTER TABLE `voucher` ADD COLUMN `namaPelanggan` TEXT")
        }

        /** Migration 12 -> 13: customer notes and tags. */
        val MIGRATION_12_13 = androidx.room.migration.Migration(12, 13) { db ->
            db.execSQL("ALTER TABLE `customer` ADD COLUMN `catatan` TEXT")
            db.execSQL("ALTER TABLE `customer` ADD COLUMN `tag` TEXT")
        }

        /** Migration 11 -> 12: monthly budgets and daily cash reconciliation. */
        val MIGRATION_11_12 = androidx.room.migration.Migration(11, 12) { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `budget` (
                    `id` TEXT NOT NULL PRIMARY KEY, `kategoriNama` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL, `bulan` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_bulan` ON `budget` (`bulan`)")
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `cash_audit` (
                    `id` TEXT NOT NULL PRIMARY KEY, `tanggal` INTEGER NOT NULL,
                    `sistem` INTEGER NOT NULL, `fisik` INTEGER NOT NULL, `selisih` INTEGER NOT NULL,
                    `catatan` TEXT, `waktu` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL,
                    `deviceId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL
                )""".trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_audit_tanggal` ON `cash_audit` (`tanggal`)")
        }

        /** Migration 10 -> 11: locations, unit conversion, batch and expiry. */
        val MIGRATION_10_11 = androidx.room.migration.Migration(10, 11) { db ->
            db.execSQL("ALTER TABLE `product` ADD COLUMN `lokasi` TEXT")
            db.execSQL("ALTER TABLE `product` ADD COLUMN `satuanKonversi` TEXT")
            db.execSQL("ALTER TABLE `product` ADD COLUMN `qtyKonversi` REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `stock_movement` ADD COLUMN `batch` TEXT")
            db.execSQL("ALTER TABLE `stock_movement` ADD COLUMN `expiry` INTEGER")
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)
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
