package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.SaleItemEntity
import id.kalkulasa.domain.model.SaleStatus
import id.kalkulasa.data.local.entity.StockMovementEntity
import id.kalkulasa.domain.model.StockMovementType
import id.kalkulasa.data.mapper.toDomain
import id.kalkulasa.domain.model.DailyPoint
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.ProductSales
import id.kalkulasa.domain.model.Sale
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
    ): Result<Sale> {
        if (items.isEmpty()) return Result.failure(IllegalArgumentException("Keranjang kosong"))
        return runCatching {
            db.withTransaction {
                val productDao = db.productDao()
                var subtotal = 0L
                val resolved = items.map { (p, qty) ->
                    val fresh = productDao.getById(p.id)
                        ?: throw IllegalStateException("Produk ${p.nama} tidak ditemukan")
                    if (fresh.stok < qty) {
                        throw IllegalStateException("Stok ${fresh.nama} tidak cukup (sisa ${fresh.stok})")
                    }
                    subtotal += (fresh.hargaJual * qty).toLong()
                    fresh to qty
                }
                val total = (subtotal - diskon).coerceAtLeast(0)
                val now = RepositorySupport.now()
                val saleId = RepositorySupport.newId()

                resolved.forEach { (fresh, qty) ->
                    db.saleDao().insertItem(
                        SaleItemEntity(
                            id = RepositorySupport.newId(),
                            saleId = saleId,
                            produkId = fresh.id,
                            namaProduk = fresh.nama,
                            jumlah = qty,
                            hargaJual = fresh.hargaJual,
                            hppSaatJual = fresh.hargaBeliRata2,
                            updatedAt = now,
                            deviceId = deviceId,
                        ),
                    )
                    productDao.update(fresh.copy(stok = fresh.stok - qty, updatedAt = now))
                    db.stockMovementDao().insert(
                        StockMovementEntity(
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
                        ),
                    )
                }
                val sale = SaleEntity(
                    id = saleId,
                    subtotal = subtotal,
                    diskon = diskon,
                    total = total,
                    metodeBayar = metodeBayar,
                    status = SaleStatus.SELESAI,
                    waktu = now,
                    catatan = catatan?.ifBlank { null },
                    updatedAt = now,
                    deviceId = deviceId,
                )
                db.saleDao().insert(sale)
                RepositorySupport.log(changeLogDao, "sale", saleId, "INSERT")
                sale.toDomain()
            }
        }
    }

    override suspend fun cancelSale(saleId: String): Result<Unit> = runCatching {
        db.withTransaction {
            val saleDao = db.saleDao()
            val productDao = db.productDao()
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

    override suspend fun salesBetween(from: Long, to: Long): List<Sale> =
        db.saleDao().getBetween(from, to).map { it.toDomain() }

    override suspend fun revenueBetween(from: Long, to: Long): Long =
        db.saleDao().sumRevenueBetween(from, to)

    override suspend fun cogsBetween(from: Long, to: Long): Long =
        db.saleDao().sumCogsBetween(from, to)

    override suspend fun countBetween(from: Long, to: Long): Int =
        db.saleDao().countBetween(from, to)

    override suspend fun topProductsBetween(from: Long, to: Long, limit: Int): List<ProductSales> =
        db.saleDao().topProductsBetween(from, to, limit)
            .map { ProductSales(it.namaProduk, it.qty) }

    override suspend fun dailyTrend(days: Int): List<DailyPoint> {
        val result = mutableListOf<DailyPoint>()
        for (daysAgo in days - 1 downTo 0) {
            val from = DateFormatter.startOfDayDaysAgo(daysAgo)
            val to = from + 24L * 60 * 60 * 1000 - 1
            val revenue = db.saleDao().sumRevenueBetween(from, to)
            val cogs = db.saleDao().sumCogsBetween(from, to)
            result += DailyPoint(DateFormatter.formatDayMonth(from), revenue, revenue - cogs)
        }
        return result
    }
}
