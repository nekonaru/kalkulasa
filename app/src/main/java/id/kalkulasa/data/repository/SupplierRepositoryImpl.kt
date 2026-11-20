package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.domain.model.PurchaseRecord
import id.kalkulasa.domain.model.SupplierSpend
import id.kalkulasa.domain.repository.SupplierRepository
import javax.inject.Inject

class SupplierRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
) : SupplierRepository {

    override suspend fun supplierSpend(): List<SupplierSpend> =
        db.purchaseDao().supplierSpend().map {
            SupplierSpend(
                pemasok = it.pemasok?.trim().orEmpty(),
                totalSpent = it.totalSpent,
                purchaseCount = it.purchaseCount,
                lastPurchaseAt = it.lastPurchaseAt,
            )
        }

    override suspend fun purchasesFromSupplier(name: String, from: Long, to: Long): List<PurchaseRecord> =
        db.purchaseDao().purchasesFromSupplier(name, from, to).map {
            PurchaseRecord(it.id, it.pemasok, it.total, it.catatan, it.waktu)
        }
}
