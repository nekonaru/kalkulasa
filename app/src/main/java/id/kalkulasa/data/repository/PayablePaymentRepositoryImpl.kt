package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.domain.model.PayablePayment
import id.kalkulasa.domain.repository.PayablePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PayablePaymentRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
) : PayablePaymentRepository {

    override fun observeRecent(limit: Int): Flow<List<PayablePayment>> =
        db.payablePaymentDao().observeRecent(limit).map { rows ->
            rows.map {
                PayablePayment(
                    it.id, it.payableId, it.pemasok, it.nominal,
                    it.catatan, it.waktu,
                )
            }
        }

    override suspend fun sumBetween(from: Long, to: Long): Long =
        db.payablePaymentDao().sumBetween(from, to)
}
