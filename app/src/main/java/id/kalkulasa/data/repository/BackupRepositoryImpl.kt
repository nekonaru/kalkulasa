package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.BusinessProfileEntity
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.data.local.entity.CategoryEntity
import id.kalkulasa.data.local.entity.CustomerEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.PurchaseItemEntity
import id.kalkulasa.data.local.entity.ReceivableEntity
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.SaleItemEntity
import id.kalkulasa.data.local.entity.StockMovementEntity
import id.kalkulasa.domain.repository.BackupRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/** JSON backup of the whole local database. Versioned so future restores can migrate. */
class BackupRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
) : BackupRepository {

    override suspend fun exportJson(): String = db.withTransaction {
        val json = JSONObject()
        json.put("app", "kalkulasa")
        json.put("backupVersion", BACKUP_VERSION)
        json.put("exportedAt", System.currentTimeMillis())

        json.put("business_profile", JSONArray().apply {
            db.businessProfileDao().getProfile()?.let { put(it.toJson()) }
        })
        json.put("category", JSONArray().also { arr -> db.categoryDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("product", JSONArray().also { arr -> db.productDao().getAllActive().forEach { arr.put(it.toJson()) } })
        json.put("stock_movement", JSONArray().also { arr -> db.stockMovementDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("purchase", JSONArray().also { arr -> db.purchaseDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("purchase_item", JSONArray().also { arr -> db.purchaseDao().getAllItems().forEach { arr.put(it.toJson()) } })
        json.put("sale", JSONArray().also { arr -> db.saleDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("sale_item", JSONArray().also { arr -> db.saleDao().getAllItems().forEach { arr.put(it.toJson()) } })
        json.put("expense", JSONArray().also { arr -> db.expenseDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("capital_entry", JSONArray().also { arr -> db.capitalEntryDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("receivable", JSONArray().also { arr -> db.receivableDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("customer", JSONArray().also { arr -> db.customerDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("employee", JSONArray().also { arr -> db.employeeDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("attendance", JSONArray().also { arr -> db.attendanceDao().getBetween(0L, Long.MAX_VALUE).forEach { arr.put(it.toJson()) } })
        json.put("kasbon", JSONArray().also { arr -> db.kasbonDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("payroll", JSONArray().also { arr -> db.payrollDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("payable", JSONArray().also { arr -> db.payableDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("sales_target", JSONArray().also { arr -> db.salesTargetDao().getAll().forEach { arr.put(it.toJson()) } })
        json.put("payable_payment", JSONArray().also { arr -> db.payablePaymentDao().getAll().forEach { arr.put(it.toJson()) } })
        json.toString()
    }

    override suspend fun importJson(json: String): Result<Unit> = runCatching {
        val root = JSONObject(json)
        require(root.optString("app") == "kalkulasa") { "File backup bukan backup Kalkulasa" }
        val version = root.optInt("backupVersion", -1)
        require(version in 1..BACKUP_VERSION) { "Versi backup tidak didukung" }
        // A file that merely contains the app marker must never be allowed to wipe
        // the database. Require the core tables that every version of our exporter writes.
        val coreTables = listOf(
            "business_profile", "category", "product", "stock_movement",
            "purchase", "sale", "expense", "capital_entry",
        ) + if (version >= 2) listOf("receivable") else emptyList()
        require(coreTables.all { root.optJSONArray(it) != null }) { "Isi backup tidak valid" }

        // Parse everything before touching the database so a corrupt file changes nothing.
        val profile = root.optJSONArray("business_profile")?.optJSONObject(0)?.toBusinessProfile()
        val categories = root.optJSONArray("category")?.toList { it.toCategory() } ?: emptyList()
        val products = root.optJSONArray("product")?.toList { it.toProduct() } ?: emptyList()
        val movements = root.optJSONArray("stock_movement")?.toList { it.toStockMovement() } ?: emptyList()
        val purchases = root.optJSONArray("purchase")?.toList { it.toPurchase() } ?: emptyList()
        val purchaseItems = root.optJSONArray("purchase_item")?.toList { it.toPurchaseItem() } ?: emptyList()
        val sales = root.optJSONArray("sale")?.toList { it.toSale() } ?: emptyList()
        val saleItems = root.optJSONArray("sale_item")?.toList { it.toSaleItem() } ?: emptyList()
        val expenses = root.optJSONArray("expense")?.toList { it.toExpense() } ?: emptyList()
        val capitalEntries = root.optJSONArray("capital_entry")?.toList { it.toCapitalEntry() } ?: emptyList()
        val receivables = root.optJSONArray("receivable")?.toList { it.toReceivable() } ?: emptyList()
        val customers = root.optJSONArray("customer")?.toList { it.toCustomer() } ?: emptyList()
        val employees = root.optJSONArray("employee")?.toList { it.toEmployee() } ?: emptyList()
        val attendances = root.optJSONArray("attendance")?.toList { it.toAttendance() } ?: emptyList()
        val kasbons = root.optJSONArray("kasbon")?.toList { it.toKasbon() } ?: emptyList()
        val payrolls = root.optJSONArray("payroll")?.toList { it.toPayroll() } ?: emptyList()
        val payables = root.optJSONArray("payable")?.toList { it.toPayable() } ?: emptyList()
        val targets = root.optJSONArray("sales_target")?.toList { it.toSalesTarget() } ?: emptyList()
        val payments = root.optJSONArray("payable_payment")?.toList { it.toPayablePayment() } ?: emptyList()

        db.withTransaction {
            db.clearAllTables()
            profile?.let { db.businessProfileDao().upsert(it) }
            categories.forEach { db.categoryDao().upsert(it) }
            products.forEach { db.productDao().upsert(it) }
            movements.forEach { db.stockMovementDao().insert(it) }
            purchases.forEach { db.purchaseDao().insert(it) }
            purchaseItems.forEach { db.purchaseDao().insertItem(it) }
            sales.forEach { db.saleDao().insert(it) }
            saleItems.forEach { db.saleDao().insertItem(it) }
            expenses.forEach { db.expenseDao().insert(it) }
            capitalEntries.forEach { db.capitalEntryDao().insert(it) }
            receivables.forEach { db.receivableDao().upsert(it) }
            customers.forEach { db.customerDao().upsert(it) }
            employees.forEach { db.employeeDao().upsert(it) }
            attendances.forEach { db.attendanceDao().upsert(it) }
            kasbons.forEach { db.kasbonDao().upsert(it) }
            payrolls.forEach { db.payrollDao().upsert(it) }
            payables.forEach { db.payableDao().upsert(it) }
            targets.forEach { db.salesTargetDao().upsert(it) }
            payments.forEach { db.payablePaymentDao().insert(it) }
        }
    }

    private fun <T> JSONArray.toList(map: (JSONObject) -> T): List<T> =
        (0 until length()).map { map(getJSONObject(it)) }

    // JSON helpers
    private fun jPut(obj: JSONObject, name: String, value: Any?) {
        if (value == null) obj.put(name, JSONObject.NULL) else obj.put(name, value)
    }

    private fun JSONObject.optLongOrNull(name: String): Long? =
        if (isNull(name)) null else getLong(name)

    private fun JSONObject.optInt(name: String, default: Int): Int =
        if (has(name) && !isNull(name)) getInt(name) else default

    private fun BusinessProfileEntity.toJson() = JSONObject().apply {
        put("id", id); put("namaUsaha", namaUsaha); put("jenisUsaha", jenisUsaha)
        put("modalAwal", modalAwal); put("dibuatPada", dibuatPada); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun CategoryEntity.toJson() = JSONObject().apply {
        put("id", id); put("nama", nama); put("jenis", jenis); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun ProductEntity.toJson() = JSONObject().apply {
        put("id", id); put("nama", nama)
        jPut(this, "kategoriId", kategoriId)
        put("satuan", satuan); jPut(this, "barcode", barcode)
        put("hargaBeliRata2", hargaBeliRata2); put("hargaJual", hargaJual)
        put("stok", stok); put("stokMinimum", stokMinimum)
        jPut(this, "fotoUri", fotoUri); put("diarsipkan", diarsipkan); put("isPinned", isPinned)
        put("updatedAt", updatedAt); jPut(this, "deletedAt", deletedAt)
        put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun StockMovementEntity.toJson() = JSONObject().apply {
        put("id", id); put("produkId", produkId); put("tipe", tipe); put("jumlah", jumlah)
        put("hargaSatuan", hargaSatuan); jPut(this, "referensiId", referensiId)
        jPut(this, "catatan", catatan); put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun PurchaseEntity.toJson() = JSONObject().apply {
        put("id", id); jPut(this, "pemasok", pemasok); put("total", total); put("waktu", waktu)
        jPut(this, "catatan", catatan); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun PurchaseItemEntity.toJson() = JSONObject().apply {
        put("id", id); put("purchaseId", purchaseId); put("produkId", produkId)
        put("jumlah", jumlah); put("hargaBeli", hargaBeli); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.EmployeeEntity.toJson() = JSONObject().apply {
        put("id", id); put("nama", nama)
        jPut(this, "jabatan", jabatan); jPut(this, "telepon", telepon)
        put("upahHarian", upahHarian); put("aktif", aktif); put("dibuatPada", dibuatPada)
        put("updatedAt", updatedAt); jPut(this, "deletedAt", deletedAt)
        put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.AttendanceEntity.toJson() = JSONObject().apply {
        put("id", id); put("karyawanId", karyawanId); put("tanggal", tanggal)
        put("status", status); put("upahDibayar", upahDibayar)
        jPut(this, "catatan", catatan); put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.PayablePaymentEntity.toJson() = JSONObject().apply {
        put("id", id); put("payableId", payableId); put("pemasok", pemasok)
        put("nominal", nominal); jPut(this, "catatan", catatan)
        put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.SalesTargetEntity.toJson() = JSONObject().apply {
        put("jenis", jenis); put("nominal", nominal); put("updatedAt", updatedAt)
        put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.PayableEntity.toJson() = JSONObject().apply {
        put("id", id); put("pemasok", pemasok); put("nominal", nominal); put("terbayar", terbayar)
        jPut(this, "jatuhTempo", jatuhTempo); jPut(this, "catatan", catatan)
        put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.KasbonEntity.toJson() = JSONObject().apply {
        put("id", id); put("karyawanId", karyawanId); put("nominal", nominal)
        put("terpakai", terpakai); jPut(this, "catatan", catatan); put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun id.kalkulasa.data.local.entity.PayrollEntity.toJson() = JSONObject().apply {
        put("id", id); put("karyawanId", karyawanId); put("namaKaryawan", namaKaryawan)
        put("periodeAwal", periodeAwal); put("periodeAkhir", periodeAkhir)
        put("totalUpah", totalUpah); put("totalKasbon", totalKasbon); put("dibayar", dibayar)
        put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun SaleEntity.toJson() = JSONObject().apply {
        put("id", id); put("subtotal", subtotal); put("diskon", diskon); put("total", total)
        put("metodeBayar", metodeBayar); put("status", status); put("waktu", waktu)
        jPut(this, "catatan", catatan)
        jPut(this, "customerId", customerId)
        put("updatedAt", updatedAt); put("poinDitukar", poinDitukar)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun CustomerEntity.toJson() = JSONObject().apply {
        put("id", id); put("nama", nama); put("poin", poin); put("kunjungan", kunjungan)
        put("totalBelanja", totalBelanja); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun SaleItemEntity.toJson() = JSONObject().apply {
        put("id", id); put("saleId", saleId); put("produkId", produkId); put("namaProduk", namaProduk)
        put("jumlah", jumlah); put("hargaJual", hargaJual); put("hppSaatJual", hppSaatJual)
        put("updatedAt", updatedAt); jPut(this, "deletedAt", deletedAt)
        put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun ExpenseEntity.toJson() = JSONObject().apply {
        put("id", id); jPut(this, "kategoriId", kategoriId); put("kategoriNama", kategoriNama)
        put("nominal", nominal); jPut(this, "catatan", catatan); put("waktu", waktu)
        put("updatedAt", updatedAt); jPut(this, "deletedAt", deletedAt)
        put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun CapitalEntryEntity.toJson() = JSONObject().apply {
        put("id", id); put("tipe", tipe); put("nominal", nominal)
        jPut(this, "catatan", catatan); put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun ReceivableEntity.toJson() = JSONObject().apply {
        put("id", id); put("namaPelanggan", namaPelanggan)
        jPut(this, "saleId", saleId)
        put("nominal", nominal); put("terbayar", terbayar)
        jPut(this, "jatuhTempo", jatuhTempo)
        jPut(this, "catatan", catatan); put("waktu", waktu); put("updatedAt", updatedAt)
        jPut(this, "deletedAt", deletedAt); put("deviceId", deviceId); put("syncStatus", syncStatus)
    }

    private fun JSONObject.str(name: String): String? = if (isNull(name)) null else getString(name)
    private fun JSONObject.strOr(name: String): String? = str(name)

    private fun JSONObject.toBusinessProfile() = BusinessProfileEntity(
        id = getString("id"), namaUsaha = getString("namaUsaha"), jenisUsaha = getString("jenisUsaha"),
        modalAwal = getLong("modalAwal"), dibuatPada = getLong("dibuatPada"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toCategory() = CategoryEntity(
        id = getString("id"), nama = getString("nama"), jenis = getString("jenis"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toProduct() = ProductEntity(
        id = getString("id"), nama = getString("nama"), kategoriId = str("kategoriId"),
        satuan = getString("satuan"), barcode = str("barcode"),
        hargaBeliRata2 = getLong("hargaBeliRata2"), hargaJual = getLong("hargaJual"),
        stok = getDouble("stok"), stokMinimum = getDouble("stokMinimum"),
        fotoUri = str("fotoUri"), diarsipkan = getBoolean("diarsipkan"),
        isPinned = optBoolean("isPinned", false),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toStockMovement() = StockMovementEntity(
        id = getString("id"), produkId = getString("produkId"), tipe = getString("tipe"),
        jumlah = getDouble("jumlah"), hargaSatuan = getLong("hargaSatuan"),
        referensiId = str("referensiId"), catatan = str("catatan"), waktu = getLong("waktu"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toPurchase() = PurchaseEntity(
        id = getString("id"), pemasok = str("pemasok"), total = getLong("total"),
        waktu = getLong("waktu"), catatan = str("catatan"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toPurchaseItem() = PurchaseItemEntity(
        id = getString("id"), purchaseId = getString("purchaseId"), produkId = getString("produkId"),
        jumlah = getDouble("jumlah"), hargaBeli = getLong("hargaBeli"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toSale() = SaleEntity(
        id = getString("id"), subtotal = getLong("subtotal"), diskon = getLong("diskon"),
        total = getLong("total"), metodeBayar = getString("metodeBayar"),
        status = getString("status"), waktu = getLong("waktu"), catatan = str("catatan"),
        customerId = str("customerId"), poinDitukar = optInt("poinDitukar", 0),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toEmployee() = id.kalkulasa.data.local.entity.EmployeeEntity(
        id = getString("id"), nama = getString("nama"),
        jabatan = str("jabatan"), telepon = str("telepon"),
        upahHarian = getLong("upahHarian"), aktif = getBoolean("aktif"),
        dibuatPada = getLong("dibuatPada"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toAttendance() = id.kalkulasa.data.local.entity.AttendanceEntity(
        id = getString("id"), karyawanId = getString("karyawanId"), tanggal = getLong("tanggal"),
        status = getString("status"), upahDibayar = getLong("upahDibayar"),
        catatan = str("catatan"), waktu = getLong("waktu"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toPayablePayment() = id.kalkulasa.data.local.entity.PayablePaymentEntity(
        id = getString("id"), payableId = getString("payableId"), pemasok = getString("pemasok"),
        nominal = getLong("nominal"), catatan = str("catatan"), waktu = getLong("waktu"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toSalesTarget() = id.kalkulasa.data.local.entity.SalesTargetEntity(
        jenis = getString("jenis"), nominal = getLong("nominal"), updatedAt = getLong("updatedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toPayable() = id.kalkulasa.data.local.entity.PayableEntity(
        id = getString("id"), pemasok = getString("pemasok"), nominal = getLong("nominal"),
        terbayar = getLong("terbayar"), jatuhTempo = optLongOrNull("jatuhTempo"), catatan = str("catatan"),
        waktu = getLong("waktu"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toKasbon() = id.kalkulasa.data.local.entity.KasbonEntity(
        id = getString("id"), karyawanId = getString("karyawanId"), nominal = getLong("nominal"),
        catatan = str("catatan"), waktu = getLong("waktu"), terpakai = optBoolean("terpakai", false),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toPayroll() = id.kalkulasa.data.local.entity.PayrollEntity(
        id = getString("id"), karyawanId = getString("karyawanId"), namaKaryawan = getString("namaKaryawan"),
        periodeAwal = getLong("periodeAwal"), periodeAkhir = getLong("periodeAkhir"),
        totalUpah = getLong("totalUpah"), totalKasbon = getLong("totalKasbon"), dibayar = getLong("dibayar"),
        waktu = getLong("waktu"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toCustomer() = CustomerEntity(
        id = getString("id"), nama = getString("nama"), poin = getLong("poin"),
        kunjungan = getInt("kunjungan"), totalBelanja = getLong("totalBelanja"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toSaleItem() = SaleItemEntity(
        id = getString("id"), saleId = getString("saleId"), produkId = getString("produkId"),
        namaProduk = getString("namaProduk"), jumlah = getDouble("jumlah"),
        hargaJual = getLong("hargaJual"), hppSaatJual = getLong("hppSaatJual"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toExpense() = ExpenseEntity(
        id = getString("id"), kategoriId = str("kategoriId"), kategoriNama = getString("kategoriNama"),
        nominal = getLong("nominal"), catatan = str("catatan"), waktu = getLong("waktu"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toCapitalEntry() = CapitalEntryEntity(
        id = getString("id"), tipe = getString("tipe"), nominal = getLong("nominal"),
        catatan = str("catatan"), waktu = getLong("waktu"), updatedAt = getLong("updatedAt"),
        deletedAt = optLongOrNull("deletedAt"), deviceId = getString("deviceId"),
        syncStatus = optString("syncStatus", "PENDING"),
    )

    private fun JSONObject.toReceivable() = ReceivableEntity(
        id = getString("id"), namaPelanggan = getString("namaPelanggan"),
        saleId = str("saleId"),
        nominal = getLong("nominal"), terbayar = getLong("terbayar"),
        jatuhTempo = optLongOrNull("jatuhTempo"),
        catatan = str("catatan"), waktu = getLong("waktu"),
        updatedAt = getLong("updatedAt"), deletedAt = optLongOrNull("deletedAt"),
        deviceId = getString("deviceId"), syncStatus = optString("syncStatus", "PENDING"),
    )

    private companion object {
        const val BACKUP_VERSION = 9
    }
}
