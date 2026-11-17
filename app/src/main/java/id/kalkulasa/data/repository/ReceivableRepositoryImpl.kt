package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.ReceivableEntity
import id.kalkulasa.domain.model.Receivable
import id.kalkulasa.domain.repository.ReceivableRepository
import id.kalkulasa.domain.usecase.UtangUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReceivableRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : ReceivableRepository {

    override fun observeAll(): Flow<List<Receivable>> =
        db.receivableDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOutstanding(): Flow<Long> =
        db.receivableDao().observeOutstanding()

    override suspend fun addManual(namaPelanggan: String, nominal: Long, jatuhTempo: Long?, catatan: String?) {
        val now = RepositorySupport.now()
        val id = RepositorySupport.newId()
        db.receivableDao().upsert(
            ReceivableEntity(
                id = id,
                namaPelanggan = namaPelanggan.trim(),
                saleId = null,
                nominal = nominal,
                terbayar = 0L,
                jatuhTempo = jatuhTempo,
                catatan = catatan?.ifBlank { null },
                waktu = now,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(changeLogDao, "receivable", id, "INSERT")
    }

    override suspend fun recordPayment(id: String, amount: Long): Result<Unit> = runCatching {
        db.withTransaction {
            val receivable = db.receivableDao().getById(id)
                ?: throw IllegalStateException("Piutang tidak ditemukan")
            val newPaid = UtangUseCase.validatePayment(receivable.nominal, receivable.terbayar, amount)
                .getOrThrow()
            val now = RepositorySupport.now()
            db.receivableDao().upsert(receivable.copy(terbayar = newPaid, updatedAt = now))
            RepositorySupport.log(changeLogDao, "receivable", id, "UPDATE")
        }
    }

    private fun ReceivableEntity.toDomain() = Receivable(
        id = id,
        namaPelanggan = namaPelanggan,
        saleId = saleId,
        nominal = nominal,
        terbayar = terbayar,
        jatuhTempo = jatuhTempo,
        catatan = catatan,
        waktu = waktu,
    )
}
