package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.ReceivableEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.SaleItemEntity
import id.kalkulasa.domain.model.SaleStatus
import id.kalkulasa.data.local.entity.StockMovementEntity
import id.kalkulasa.domain.model.StockMovementType
import id.kalkulasa.data.mapper.toDomain
import id.kalkulasa.domain.model.DailyPoint
import id.kalkulasa.domain.model.PaymentMethod
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.ProductProfit
import id.kalkulasa.domain.model.ProductSales
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.model.SaleLine
import id.kalkulasa.domain.usecase.PoinLoyalitasUseCase
import id.kalkulasa.domain.usecase.PromosiUseCase
import id.kalkulasa.domain.repository.SaleRepository
import id.kalkulasa.core.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : SaleRepository {

    override fun observeRecentSales(limit: Int): Flow<List<Sale>> =
        db.saleDao().observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun recordSale(
        items: List<Pair<Product, Double>>,
        diskon: Long,
        metodeBayar: String,
        catatan: String?,
        namaPelanggan: String?,
        poinDitukar: Int,
        ppn: Long,
        voucherKode: String?,
    ): Result<Sale> {
        if (items.isEmpty()) return Result.failure(IllegalArgumentException("Keranjang kosong"))
        if (diskon < 0) return Result.failure(IllegalArgumentException("Diskon tidak boleh negatif"))
        if (diskon > items.sumOf { kotlin.math.round(it.first.hargaJual * it.second).toLong() }) {
            return Result.failure(IllegalArgumentException("Diskon melebihi subtotal"))
        }
        if (items.any { it.second <= 0.0 }) return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        return runCatching {
            db.withTransaction {
                val productDao = db.productDao()
                val productById = productDao.getByIds(items.map { it.first.id }).associateBy { it.id }
                var subtotal = 0L
                val resolved = items.map { (p, qty) ->
                    val fresh = productById[p.id]
                        ?: throw IllegalStateException("Produk ${p.nama} tidak ditemukan")
                    if (fresh.stok < qty) {
                        throw IllegalStateException("Stok ${fresh.nama} tidak cukup (sisa ${fresh.stok})")
                    }
                    subtotal += kotlin.math.round(fresh.hargaJual * qty).toLong()
                    fresh to qty
                }
                val total = (subtotal - diskon + ppn.coerceAtLeast(0L)).coerceAtLeast(0)
                val now = RepositorySupport.now()
                val saleId = RepositorySupport.newId()

                // Loyalty: named customer earns 1 point per Rp 1.000 spent and may redeem points.
                val customerName = namaPelanggan?.trim().orEmpty()
                if (poinDitukar > 0 && customerName.isEmpty()) {
                    throw IllegalArgumentException("Nama pelanggan wajib untuk tukar poin")
                }
                var linkedCustomerId: String? = null
                if (customerName.isNotEmpty()) {
                    val existing = db.customerDao().findByName(customerName)
                    if (poinDitukar > 0) {
                        PromosiUseCase.validasiPenukaran(existing?.poin ?: 0L, poinDitukar).getOrThrow()
                    }
                    val pointsEarned = PoinLoyalitasUseCase.pointsFor(total)
                    if (existing != null) {
                        linkedCustomerId = existing.id
                        db.customerDao().upsert(
                            existing.copy(
                                poin = existing.poin - poinDitukar + pointsEarned,
                                kunjungan = existing.kunjungan + 1,
                                totalBelanja = existing.totalBelanja + total,
                                updatedAt = now,
                            ),
                        )
                    } else {
                        val newCustomerId = RepositorySupport.newId()
                        linkedCustomerId = newCustomerId
                        db.customerDao().upsert(
                            id.kalkulasa.data.local.entity.CustomerEntity(
                                id = newCustomerId,
                                nama = customerName,
                                poin = pointsEarned,
                                kunjungan = 1,
                                totalBelanja = total,
                                updatedAt = now,
                                deviceId = deviceId,
                            ),
                        )
                    }
                    RepositorySupport.log(changeLogDao, "customer", linkedCustomerId, "UPDATE")
                }

                val saleItems = mutableListOf<SaleItemEntity>()
                val movements = mutableListOf<StockMovementEntity>()
                val updatedProducts = mutableListOf<ProductEntity>()
                resolved.forEach { (fresh, qty) ->
                    saleItems += SaleItemEntity(
                        id = RepositorySupport.newId(),
                        saleId = saleId,
                        produkId = fresh.id,
                        namaProduk = fresh.nama,
                        jumlah = qty,
                        hargaJual = fresh.hargaJual,
                        hppSaatJual = fresh.hargaBeliRata2,
                        updatedAt = now,
                        deviceId = deviceId,
                    )
                    updatedProducts += fresh.copy(stok = fresh.stok - qty, updatedAt = now)
                    movements += StockMovementEntity(
                        id = RepositorySupport.newId(),
                        produkId = fresh.id,
                        tipe = StockMovementType.KELUAR,
                        jumlah = qty,
                        hargaSatuan = fresh.hargaBeliRata2,
                        referensiId = saleId,
                        catatan = null,
                        waktu = now,
                        updatedAt = now,
                        deviceId = deviceId,
                    )
                }
                db.saleDao().insertItems(saleItems)
                productDao.updateAll(updatedProducts)
                db.stockMovementDao().insertAll(movements)
                val sale = SaleEntity(
                    id = saleId,
                    subtotal = subtotal,
                    diskon = diskon,
                    total = total,
                    metodeBayar = metodeBayar,
                    status = SaleStatus.SELESAI,
                    waktu = now,
                    catatan = catatan?.ifBlank { null },
                    customerId = linkedCustomerId,
                    poinDitukar = poinDitukar.coerceAtLeast(0),
                    ppn = ppn.coerceAtLeast(0L),
                    voucherKode = voucherKode?.ifBlank { null },
                    updatedAt = now,
                    deviceId = deviceId,
                )
                voucherKode?.let { code ->
                    val voucher = db.voucherDao().findByCode(code)
                        ?: throw IllegalStateException("Voucher tidak ditemukan")
                    require(voucher.terpakai < voucher.kuota) { "Kuota voucher habis" }
                    db.voucherDao().upsert(voucher.copy(terpakai = voucher.terpakai + 1, updatedAt = now))
                    RepositorySupport.log(changeLogDao, "voucher", voucher.id, "UPDATE")
                }
                db.saleDao().insert(sale)
                if (metodeBayar == PaymentMethod.HUTANG && total > 0L) {
                    db.receivableDao().upsert(
                        ReceivableEntity(
                            id = RepositorySupport.newId(),
                            namaPelanggan = namaPelanggan?.trim().orEmpty().ifBlank { "Pelanggan" },
                            saleId = saleId,
                            nominal = total,
                            terbayar = 0L,
                            jatuhTempo = null,
                            catatan = catatan?.ifBlank { null },
                            waktu = now,
                            updatedAt = now,
                            deviceId = deviceId,
                        ),
                    )
                    RepositorySupport.log(changeLogDao, "receivable", saleId, "INSERT")
                }
                RepositorySupport.log(changeLogDao, "sale", saleId, "INSERT")
                sale.toDomain()
            }
        }
    }

    override suspend fun cancelSale(saleId: String): Result<Unit> = runCatching {
        db.withTransaction {
            val saleDao = db.saleDao()
            val productDao = db.productDao()
            val sale = saleDao.getById(saleId)
                ?: throw IllegalStateException("Transaksi tidak ditemukan")
            if (sale.status == SaleStatus.BATAL) {
                throw IllegalStateException("Transaksi sudah dibatalkan")
            }
            val now0 = RepositorySupport.now()
            // Preserve cash already received on a credit sale. Collapsing nominal
            // to terbayar leaves zero debt while sumPaid() still counts the money.
            db.receivableDao().softDeleteUnpaidBySaleId(saleId, now0)
            db.receivableDao().settlePaidBySaleId(saleId, now0)
            // Reverse loyalty points earned by the named customer on this sale.
            sale.customerId?.let { customerId ->
                db.customerDao().getById(customerId)?.let { customer ->
                    val points = PoinLoyalitasUseCase.pointsFor(sale.total)
                    db.customerDao().upsert(
                        customer.copy(
                            poin = (customer.poin - points + sale.poinDitukar.coerceAtLeast(0)).coerceAtLeast(0L),
                            kunjungan = (customer.kunjungan - 1).coerceAtLeast(0),
                            totalBelanja = (customer.totalBelanja - sale.total).coerceAtLeast(0L),
                            updatedAt = now0,
                        ),
                    )
                    RepositorySupport.log(changeLogDao, "customer", customerId, "UPDATE")
                }
            }
            val items = saleDao.getItems(saleId)
            val now = RepositorySupport.now()
            items.forEach { item ->
                val product = productDao.getById(item.produkId)
                if (product != null) {
                    productDao.update(product.copy(stok = product.stok + item.jumlah, updatedAt = now))
                    db.stockMovementDao().insert(
                        StockMovementEntity(
                            id = RepositorySupport.newId(),
                            produkId = product.id,
                            tipe = StockMovementType.RETUR,
                            jumlah = item.jumlah,
                            hargaSatuan = item.hppSaatJual,
                            referensiId = saleId,
                            catatan = "Pembatalan transaksi",
                            waktu = now,
                            updatedAt = now,
                            deviceId = deviceId,
                        ),
                    )
                }
            }
            saleDao.markCancelled(saleId, now)
            RepositorySupport.log(changeLogDao, "sale", saleId, "UPDATE")
        }
    }

    override suspend fun lastCompletedSale(): Pair<Sale, List<SaleLine>>? {
        val sale = db.saleDao().lastCompleted() ?: return null
        val items = db.saleDao().getItems(sale.id).map {
            SaleLine(
                produkId = it.produkId,
                namaProduk = it.namaProduk,
                jumlah = it.jumlah,
                hargaJual = it.hargaJual,
                hppSaatJual = it.hppSaatJual,
            )
        }
        return sale.toDomain() to items
    }

    override suspend fun saleItems(saleId: String): List<SaleLine> =
        db.saleDao().getItems(saleId).map {
            SaleLine(
                produkId = it.produkId,
                namaProduk = it.namaProduk,
                jumlah = it.jumlah,
                hargaJual = it.hargaJual,
                hppSaatJual = it.hppSaatJual,
            )
        }

    override suspend fun salesBetween(from: Long, to: Long): List<Sale> =
        db.saleDao().getBetween(from, to).map { it.toDomain() }

    override suspend fun revenueBetween(from: Long, to: Long): Long =
        db.saleDao().sumRevenueBetween(from, to)

    override suspend fun cogsBetween(from: Long, to: Long): Long =
        db.saleDao().sumCogsBetween(from, to)

    override suspend fun countBetween(from: Long, to: Long): Int =
        db.saleDao().countBetween(from, to)

    override suspend fun mostProfitableBetween(from: Long, to: Long, limit: Int): List<ProductProfit> =
        db.saleDao().profitRowsBetween(from, to)
            .groupBy { it.namaProduk }
            .map { (nama, rows) ->
                val gross = rows.sumOf { it.gross }
                val cogs = rows.sumOf { it.cogs }
                // Allocate each sale's discount across its items proportionally.
                val diskon = rows.sumOf { row ->
                    if (row.saleGross <= 0.0) 0L
                    else kotlin.math.round(row.diskon.toDouble() * row.gross / row.saleGross).toLong()
                }
                ProductProfit(
                    nama,
                    rows.sumOf { it.qty },
                    kotlin.math.round(gross - cogs).toLong() - diskon,
                )
            }
            .sortedByDescending { it.profit }
            .take(limit)

    override suspend fun topProductsBetween(from: Long, to: Long, limit: Int): List<ProductSales> =
        db.saleDao().topProductsBetween(from, to, limit)
            .map { ProductSales(it.namaProduk, it.qty) }

    override suspend fun dailyTrend(days: Int): List<DailyPoint> {
        val from = DateFormatter.startOfDayDaysAgo(days - 1)
        val to = DateFormatter.endOfDay()
        // Two aggregate queries instead of 2 per day
        val revenueByDay = db.saleDao().dailyRevenue(from, to).associate { it.day to it.revenue }
        val cogsByDay = db.saleDao().dailyCogs(from, to).associate { it.day to it.cogs }
        val dayFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return (days - 1 downTo 0).map { daysAgo ->
            val start = DateFormatter.startOfDayDaysAgo(daysAgo)
            val key = dayFormat.format(java.util.Date(start))
            val revenue = revenueByDay[key] ?: 0L
            val cogs = cogsByDay[key] ?: 0L
            DailyPoint(DateFormatter.formatDayMonth(start), revenue, revenue - cogs)
        }
    }
}
