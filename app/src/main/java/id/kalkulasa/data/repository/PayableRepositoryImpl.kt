package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.PayableEntity
import id.kalkulasa.data.local.entity.PayablePaymentEntity
import id.kalkulasa.domain.model.Payable
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.usecase.UtangUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PayableRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : PayableRepository {

    override fun observeAll(): Flow<List<Payable>> =
        db.payableDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOutstanding(): Flow<Long> = db.payableDao().observeOutstanding()

    override suspend fun add(
        pemasok: String,
        nominal: Long,
        jatuhTempo: Long?,
        catatan: String?,
    ): Result<Unit> = runCatching {
        if (pemasok.isBlank()) throw IllegalArgumentException("Nama pemasok wajib diisi")
        if (nominal <= 0) throw IllegalArgumentException("Nominal harus lebih dari 0")
        db.withTransaction {
            val now = RepositorySupport.now()
            val id = RepositorySupport.newId()
            db.payableDao().upsert(
                PayableEntity(
                    id = id,
                    pemasok = pemasok.trim(),
                    nominal = nominal,
                    terbayar = 0L,
                    jatuhTempo = jatuhTempo,
                    catatan = catatan?.ifBlank { null },
                    waktu = now,
                    updatedAt = now,
                    deviceId = deviceId,
                ),
            )
            RepositorySupport.log(db.changeLogDao(), "payable", id, "INSERT")
        }
    }

    override suspend fun recordPayment(id: String, amount: Long): Result<Unit> = runCatching {
        db.withTransaction {
            val payable = db.payableDao().getById(id)
                ?: throw IllegalStateException("Hutang tidak ditemukan")
            val newPaid = UtangUseCase.validatePayment(payable.nominal, payable.terbayar, amount)
                .getOrThrow()
            val now = RepositorySupport.now()
            db.payableDao().upsert(payable.copy(terbayar = newPaid, updatedAt = now))
            val paymentId = RepositorySupport.newId()
            db.payablePaymentDao().insert(
                PayablePaymentEntity(
                    id = paymentId,
                    payableId = id,
                    pemasok = payable.pemasok,
                    nominal = amount,
                    catatan = null,
                    waktu = now,
                    updatedAt = now,
                    deviceId = deviceId,
                ),
            )
            RepositorySupport.log(db.changeLogDao(), "payable", id, "UPDATE")
            RepositorySupport.log(db.changeLogDao(), "payable_payment", paymentId, "INSERT")
        }
    }

    private fun PayableEntity.toDomain() = Payable(
        id = id,
        pemasok = pemasok,
        nominal = nominal,
        terbayar = terbayar,
        jatuhTempo = jatuhTempo,
        catatan = catatan,
        waktu = waktu,
    )
}
