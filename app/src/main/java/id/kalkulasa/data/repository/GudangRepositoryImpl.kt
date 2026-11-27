package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.StockTransferEntity
import id.kalkulasa.data.local.entity.StockTransferItemEntity
import id.kalkulasa.data.local.entity.SupplierEntity
import id.kalkulasa.domain.model.StockTransfer
import id.kalkulasa.domain.model.Supplier
import id.kalkulasa.domain.repository.SupplierRepositoryPro
import id.kalkulasa.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SupplierRepositoryProImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : SupplierRepositoryPro {

    override fun observeAll(): Flow<List<Supplier>> =
        db.supplierDao().observeAll().map { rows -> rows.map { Supplier(it.id, it.nama, it.telepon, it.leadTimeDays) } }

    override suspend fun save(nama: String, telepon: String?, leadTimeDays: Int): Result<Unit> = runCatching {
        require(nama.isNotBlank()) { "Nama pemasok wajib diisi" }
        db.supplierDao().upsert(
            SupplierEntity(
                id = RepositorySupport.newId(), nama = nama.trim(),
                telepon = telepon?.ifBlank { null }, leadTimeDays = leadTimeDays.coerceAtLeast(0),
                updatedAt = RepositorySupport.now(), deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "supplier", nama, "INSERT")
    }
}

class TransferRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : TransferRepository {

    override fun observeAll(): Flow<List<StockTransfer>> =
        db.stockTransferDao().observeAll().map { rows ->
            rows.map { StockTransfer(it.id, it.dariLokasi, it.keLokasi, it.waktu) }
        }

    override suspend fun save(
        dari: String,
        ke: String,
        catatan: String?,
        items: List<Pair<String, Double>>,
    ): Result<Unit> = runCatching {
        require(dari.isNotBlank() && ke.isNotBlank()) { "Lokasi wajib diisi" }
        require(items.isNotEmpty()) { "Item transfer kosong" }
        db.withTransaction {
            val id = RepositorySupport.newId()
            val now = RepositorySupport.now()
            db.stockTransferDao().upsert(
                StockTransferEntity(
                    id = id, dariLokasi = dari.trim(), keLokasi = ke.trim(),
                    catatan = catatan?.ifBlank { null }, waktu = now,
                    updatedAt = now, deviceId = deviceId,
                ),
            )
            db.stockTransferDao().insertItems(items.map {
                StockTransferItemEntity(
                    id = RepositorySupport.newId(), transferId = id,
                    produkId = it.first, jumlah = it.second,
                    updatedAt = now, deviceId = deviceId,
                )
            })
            RepositorySupport.log(db.changeLogDao(), "stock_transfer", id, "INSERT")
        }
    }
}
