package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.PurchaseItemEntity
import id.kalkulasa.data.local.entity.StockMovementEntity
import id.kalkulasa.domain.model.StockMovementType
import id.kalkulasa.data.mapper.toDomain
import id.kalkulasa.domain.model.StockMovement
import id.kalkulasa.domain.repository.StockRepository
import id.kalkulasa.domain.usecase.HitungHppUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : StockRepository {

    override fun observeRecentMovements(limit: Int): Flow<List<StockMovement>> =
        db.stockMovementDao().observeRecentWithProduct(limit).map { movements ->
            movements.map { m -> m.toDomain() }
        }

    override suspend fun recordStockIn(
        productId: String,
        qty: Double,
        hargaBeli: Long,
        pemasok: String?,
        catatan: String?,
    ): Result<Unit> {
        if (qty <= 0.0) return Result.failure(IllegalArgumentException("qty"))
        if (hargaBeli <= 0) return Result.failure(IllegalArgumentException("Harga beli harus lebih dari 0"))
        return runCatching {
            db.withTransaction {
                val productDao = db.productDao()
                val product = productDao.getById(productId)
                    ?: throw IllegalStateException("Produk tidak ditemukan")
                val newSellOrKeep = product.hargaJual
                val newHpp = HitungHppUseCase.newAverageCost(product.stok, product.hargaBeliRata2, qty, hargaBeli)
                val purchaseId = RepositorySupport.newId()
                val now = RepositorySupport.now()
                val total = kotlin.math.round(qty * hargaBeli).toLong()
                db.purchaseDao().insert(
                    PurchaseEntity(
                        id = purchaseId,
                        pemasok = pemasok?.ifBlank { null },
                        total = total,
                        waktu = now,
                        catatan = catatan?.ifBlank { null },
                        updatedAt = now,
                        deviceId = deviceId,
                    ),
                )
                db.purchaseDao().insertItem(
                    PurchaseItemEntity(
                        id = RepositorySupport.newId(),
                        purchaseId = purchaseId,
                        produkId = productId,
                        jumlah = qty,
                        hargaBeli = hargaBeli,
                        updatedAt = now,
                        deviceId = deviceId,
                    ),
                )
                db.stockMovementDao().insert(
                    StockMovementEntity(
                        id = RepositorySupport.newId(),
                        produkId = productId,
                        tipe = StockMovementType.MASUK,
                        jumlah = qty,
                        hargaSatuan = hargaBeli,
                        referensiId = purchaseId,
                        catatan = catatan?.ifBlank { null },
                        waktu = now,
                        updatedAt = now,
                        deviceId = deviceId,
                    ),
                )
                productDao.update(
                    product.copy(
                        stok = product.stok + qty,
                        hargaBeliRata2 = newHpp,
                        hargaJual = newSellOrKeep,
                        updatedAt = now,
                    ),
                )
                RepositorySupport.log(changeLogDao, "purchase", purchaseId, "INSERT")
                RepositorySupport.log(changeLogDao, "product", productId, "UPDATE")
                RepositorySupport.log(changeLogDao, "stock_movement", purchaseId, "INSERT")
            }
        }
    }

    override suspend fun applyAdjustments(
        items: List<Pair<String, Double>>,
        reason: String,
    ): Result<Int> = runCatching {
        db.withTransaction {
            var changed = 0
            items.forEach { (productId, newQty) ->
                val product = db.productDao().getById(productId) ?: return@forEach
                if (newQty < 0.0 || newQty == product.stok) return@forEach
                val delta = newQty - product.stok
                val now = RepositorySupport.now()
                db.stockMovementDao().insert(
                    StockMovementEntity(
                        id = RepositorySupport.newId(),
                        produkId = productId,
                        tipe = StockMovementType.PENYESUAIAN,
                        jumlah = kotlin.math.abs(delta),
                        hargaSatuan = product.hargaBeliRata2,
                        referensiId = null,
                        catatan = reason,
                        waktu = now,
                        updatedAt = now,
                        deviceId = deviceId,
                    ),
                )
                db.productDao().update(product.copy(stok = newQty, updatedAt = now))
                RepositorySupport.log(changeLogDao, "product", productId, "UPDATE")
                changed++
            }
            changed
        }
    }

    override suspend fun recordAdjustment(
        productId: String,
        newQty: Double,
        reason: String,
    ): Result<Unit> {
        if (newQty < 0.0) return Result.failure(IllegalArgumentException("qty"))
        return runCatching {
            db.withTransaction {
                val productDao = db.productDao()
                val product = productDao.getById(productId)
                    ?: throw IllegalStateException("Produk tidak ditemukan")
                val delta = newQty - product.stok
                val now = RepositorySupport.now()
                db.stockMovementDao().insert(
                    StockMovementEntity(
                        id = RepositorySupport.newId(),
                        produkId = productId,
                        tipe = StockMovementType.PENYESUAIAN,
                        jumlah = kotlin.math.abs(delta),
                        hargaSatuan = product.hargaBeliRata2,
                        referensiId = null,
                        catatan = reason,
                        waktu = now,
                        updatedAt = now,
                        deviceId = deviceId,
                    ),
                )
                productDao.update(product.copy(stok = newQty, updatedAt = now))
                RepositorySupport.log(changeLogDao, "product", productId, "UPDATE")
            }
        }
    }
}
