package id.kalkulasa.domain.repository

import id.kalkulasa.domain.model.Attendance
import id.kalkulasa.domain.model.BusinessProfile
import id.kalkulasa.domain.model.CashBreakdown
import id.kalkulasa.domain.model.CategorySpend
import id.kalkulasa.domain.model.PurchaseRecord
import id.kalkulasa.domain.model.CapitalEntry
import id.kalkulasa.domain.model.Category
import id.kalkulasa.domain.model.CategoryType
import id.kalkulasa.domain.model.Customer
import id.kalkulasa.domain.model.DailyPoint
import id.kalkulasa.domain.model.Employee
import id.kalkulasa.domain.model.Expense
import id.kalkulasa.domain.model.Kasbon
import id.kalkulasa.domain.model.PayrollRecord
import id.kalkulasa.domain.model.PeriodReport
import id.kalkulasa.domain.model.Payable
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.ProductProfit
import id.kalkulasa.domain.model.ProductSales
import id.kalkulasa.domain.model.Receivable
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.model.SaleLine
import id.kalkulasa.domain.model.StockMovement
import id.kalkulasa.domain.model.SupplierSpend
import id.kalkulasa.domain.usecase.ImportProdukUseCase
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<BusinessProfile?>
    suspend fun getProfile(): BusinessProfile?
    suspend fun saveProfile(profile: BusinessProfile)
}

interface ProductRepository {
    fun observeActive(query: String = ""): Flow<List<Product>>
    fun observeAll(query: String = ""): Flow<List<Product>>
    fun observeLowStockCount(): Flow<Int>
    fun observeLowStock(): Flow<List<Product>>
    fun observeInventoryValue(): Flow<Long>
    suspend fun getById(id: String): Product?
    suspend fun getAll(): List<Product>
    suspend fun save(product: Product)
    suspend fun setArchived(id: String, archived: Boolean)
    /** Bulk-import products atomically. Returns count imported. */
    suspend fun importProducts(products: List<ImportProdukUseCase.ImportedProduct>): Int
    suspend fun setPinned(id: String, pinned: Boolean)
}

interface EmployeeRepository {
    fun observeAll(): Flow<List<Employee>>
    suspend fun save(employee: Employee)
    suspend fun setActive(id: String, aktif: Boolean)
    suspend fun getById(id: String): Employee?
}

interface AttendanceRepository {
    fun observeBetween(from: Long, to: Long): Flow<List<Attendance>>
    suspend fun mark(karyawanId: String, tanggal: Long, status: String, upahDibayar: Long, catatan: String?): Result<Unit>
    suspend fun getBetween(from: Long, to: Long): List<Attendance>
}

interface KasbonRepository {
    suspend fun add(karyawanId: String, nominal: Long, catatan: String?)
    suspend fun getForEmployee(karyawanId: String, from: Long, to: Long): List<Kasbon>
}

interface PayrollRepository {
    fun observeAll(): Flow<List<PayrollRecord>>
    suspend fun pay(
        karyawan: Employee,
        from: Long,
        to: Long,
        upah: Long,
        kasbon: Long,
        dibayar: Long,
        kategoriNama: String,
        catatan: String,
    ): Result<Unit>
}

interface SupplierRepository {
    suspend fun supplierSpend(): List<SupplierSpend>
    suspend fun purchasesFromSupplier(name: String, from: Long, to: Long): List<PurchaseRecord>
}

interface CategoryRepository {
    fun observeByType(jenis: String): Flow<List<Category>>
    fun observeAll(): Flow<List<Category>>
    fun observeProductCategories(): Flow<List<Category>> = observeByType(CategoryType.PRODUCT)
    suspend fun addCategory(name: String, jenis: String)
    suspend fun rename(id: String, name: String)
    suspend fun delete(id: String)
}

interface StockRepository {
    fun observeRecentMovements(limit: Int = 100): Flow<List<StockMovement>>
    suspend fun recordStockIn(productId: String, qty: Double, hargaBeli: Long, pemasok: String?, catatan: String?): Result<Unit>
    suspend fun recordAdjustment(productId: String, newQty: Double, reason: String): Result<Unit>
}

interface CustomerRepository {
    fun observeAll(): Flow<List<Customer>>
    fun observeAll(query: String): Flow<List<Customer>>
    suspend fun getById(id: String): Customer?
    suspend fun resetPoints(id: String)
}

interface ReceivableRepository {
    fun observeAll(): Flow<List<Receivable>>
    fun observeOutstanding(): Flow<Long>
    suspend fun addManual(namaPelanggan: String, nominal: Long, jatuhTempo: Long?, catatan: String?): Result<Unit>
    suspend fun recordPayment(id: String, amount: Long): Result<Unit>
}

interface PayableRepository {
    fun observeAll(): Flow<List<Payable>>
    fun observeOutstanding(): Flow<Long>
    suspend fun add(pemasok: String, nominal: Long, jatuhTempo: Long?, catatan: String?): Result<Unit>
    suspend fun recordPayment(id: String, amount: Long): Result<Unit>
}

interface SaleRepository {
    fun observeRecentSales(limit: Int = 50): Flow<List<Sale>>
    suspend fun recordSale(items: List<Pair<Product, Double>>, diskon: Long, metodeBayar: String, catatan: String?, namaPelanggan: String? = null): Result<Sale>
    suspend fun cancelSale(saleId: String): Result<Unit>
    suspend fun salesBetween(from: Long, to: Long): List<Sale>
    suspend fun revenueBetween(from: Long, to: Long): Long
    suspend fun cogsBetween(from: Long, to: Long): Long
    suspend fun countBetween(from: Long, to: Long): Int
    suspend fun topProductsBetween(from: Long, to: Long, limit: Int): List<ProductSales>
    suspend fun dailyTrend(days: Int): List<DailyPoint>
    suspend fun saleItems(saleId: String): List<SaleLine>
    suspend fun mostProfitableBetween(from: Long, to: Long, limit: Int): List<ProductProfit>
    suspend fun lastCompletedSale(): Pair<Sale, List<SaleLine>>?
}

interface FinanceRepository {
    fun observeRecentExpenses(limit: Int = 100): Flow<List<Expense>>
    fun observeRecentCapitalEntries(limit: Int = 100): Flow<List<CapitalEntry>>
    fun observeRecentPurchases(limit: Int = 100): Flow<List<PurchaseRecord>>
    suspend fun addCapital(tipe: String, nominal: Long, catatan: String?)
    suspend fun addExpense(kategoriId: String?, kategoriNama: String, nominal: Long, catatan: String?): Result<Unit>
    suspend fun cashBalance(): Long
    suspend fun cashBreakdown(): CashBreakdown
    suspend fun netCapital(): Long
    suspend fun expensesByCategoryBetween(from: Long, to: Long): List<CategorySpend>
    suspend fun expensesBetween(from: Long, to: Long): Long
    suspend fun purchasesBetween(from: Long, to: Long): Long
}

interface ReportRepository {
    suspend fun reportBetween(from: Long, to: Long): PeriodReport
    suspend fun exportCsv(from: Long, to: Long): String
    suspend fun exportPdfBytes(from: Long, to: Long): ByteArray
    suspend fun shareText(from: Long, to: Long): String
}

interface BackupRepository {
    /** Serialize the whole database to a JSON string. */
    suspend fun exportJson(): String

    /** Replace all local data with the content of the given JSON backup. */
    suspend fun importJson(json: String): Result<Unit>
}
