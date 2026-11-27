package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.BundleEntity
import id.kalkulasa.data.local.entity.BundleItemEntity
import id.kalkulasa.data.local.entity.PreorderEntity
import id.kalkulasa.data.local.entity.PreorderItemEntity
import id.kalkulasa.domain.model.Bundle
import id.kalkulasa.domain.model.Preorder
import id.kalkulasa.domain.model.PreorderLine
import id.kalkulasa.domain.repository.BundleRepository
import id.kalkulasa.domain.repository.PreorderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreorderRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : PreorderRepository {

    override fun observeAll(): Flow<List<Preorder>> =
        db.preorderDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getWithItems(id: String): Pair<Preorder, List<PreorderLine>>? {
        val row = db.preorderDao().getById(id) ?: return null
        val items = db.preorderDao().getItems(id).map {
            PreorderLine(it.produkId, it.namaProduk, it.jumlah, it.hargaJual)
        }
        return row.toDomain() to items
    }

    override suspend fun save(nama: String?, catatan: String?, items: List<PreorderLine>): Result<Unit> = runCatching {
        require(items.isNotEmpty()) { "Item preorder kosong" }
        db.withTransaction {
            val id = RepositorySupport.newId()
            val now = RepositorySupport.now()
            val total = items.sumOf { kotlin.math.round(it.hargaJual * it.jumlah).toLong() }
            db.preorderDao().upsert(
                PreorderEntity(
                    id = id,
                    namaPelanggan = nama?.ifBlank { null },
                    status = "DIPESAN",
                    total = total,
                    catatan = catatan?.ifBlank { null },
                    waktu = now,
                    updatedAt = now,
                    deviceId = deviceId,
                ),
            )
            db.preorderDao().insertItems(items.map {
                PreorderItemEntity(
                    id = RepositorySupport.newId(),
                    preorderId = id,
                    produkId = it.produkId,
                    namaProduk = it.namaProduk,
                    jumlah = it.jumlah,
                    hargaJual = it.hargaJual,
                    updatedAt = now,
                    deviceId = deviceId,
                )
            })
            RepositorySupport.log(db.changeLogDao(), "preorder", id, "INSERT")
        }
    }

    override suspend fun mark(id: String, status: String): Result<Unit> = runCatching {
        val row = db.preorderDao().getById(id) ?: throw IllegalStateException("Preorder tidak ditemukan")
        db.preorderDao().upsert(row.copy(status = status, updatedAt = RepositorySupport.now()))
        RepositorySupport.log(db.changeLogDao(), "preorder", id, "UPDATE")
    }

    private fun PreorderEntity.toDomain() = Preorder(
        id, namaPelanggan, status, total, catatan, waktu,
    )
}

class BundleRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : BundleRepository {

    override fun observeActive(): Flow<List<Bundle>> =
        db.bundleDao().observeActive().map { rows ->
            rows.map { Bundle(it.id, it.nama, it.hargaPaket, it.aktif) }
        }

    override suspend fun save(nama: String, hargaPaket: Long, items: List<Pair<String, Double>>): Result<Unit> = runCatching {
        require(nama.isNotBlank()) { "Nama paket wajib diisi" }
        require(items.isNotEmpty()) { "Item paket kosong" }
        db.withTransaction {
            val id = RepositorySupport.newId()
            val now = RepositorySupport.now()
            db.bundleDao().upsert(
                BundleEntity(
                    id = id, nama = nama.trim(), hargaPaket = hargaPaket.coerceAtLeast(0L),
                    aktif = true, updatedAt = now, deviceId = deviceId,
                ),
            )
            db.bundleDao().insertItems(items.map {
                BundleItemEntity(
                    id = RepositorySupport.newId(), bundleId = id, produkId = it.first,
                    jumlah = it.second, updatedAt = now, deviceId = deviceId,
                )
            })
            RepositorySupport.log(db.changeLogDao(), "bundle", id, "INSERT")
        }
    }

    override suspend fun itemsFor(id: String): List<Pair<String, Double>> =
        db.bundleDao().getItems(id).map { it.produkId to it.jumlah }
}
