package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.VoucherEntity
import id.kalkulasa.domain.model.Voucher
import id.kalkulasa.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VoucherRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : VoucherRepository {

    override fun observeAll(): Flow<List<Voucher>> =
        db.voucherDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun findByCode(kode: String): Voucher? =
        db.voucherDao().findByCode(kode.trim().uppercase())?.toDomain()

    override suspend fun save(voucher: Voucher): Result<Unit> = runCatching {
        require(voucher.kode.isNotBlank()) { "Kode voucher wajib diisi" }
        db.voucherDao().upsert(
            VoucherEntity(
                id = voucher.id,
                kode = voucher.kode.trim().uppercase(),
                tipe = voucher.tipe,
                nilai = voucher.nilai.coerceAtLeast(0L),
                minimalBelanja = voucher.minimalBelanja.coerceAtLeast(0L),
                kuota = voucher.sisaKuota.coerceAtLeast(0),
                terpakai = 0,
                berlakuMulai = System.currentTimeMillis(),
                berlakuSampai = voucher.berlakuSampai,
                aktif = true,
                updatedAt = System.currentTimeMillis(),
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "voucher", voucher.id, "INSERT")
    }

    override suspend fun consume(id: String): Result<Unit> = runCatching {
        val row = db.voucherDao().getAll().firstOrNull { it.id == id }
            ?: throw IllegalStateException("Voucher tidak ditemukan")
        require(row.terpakai < row.kuota) { "Kuota voucher habis" }
        db.voucherDao().upsert(row.copy(terpakai = row.terpakai + 1, updatedAt = RepositorySupport.now()))
        RepositorySupport.log(db.changeLogDao(), "voucher", id, "UPDATE")
    }

    private fun VoucherEntity.toDomain() = Voucher(
        id, kode, tipe, nilai, minimalBelanja, (kuota - terpakai).coerceAtLeast(0), berlakuSampai,
        scope, namaPelanggan,
    )
}
