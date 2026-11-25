package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.SalesTargetEntity
import id.kalkulasa.domain.model.SalesTarget
import id.kalkulasa.domain.repository.SalesTargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SalesTargetRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : SalesTargetRepository {

    override fun observeAll(): Flow<List<SalesTarget>> =
        db.salesTargetDao().observeAll().map { list ->
            list.map { SalesTarget(it.jenis, it.nominal) }
        }

    override suspend fun save(jenis: String, nominal: Long): Result<Unit> = runCatching {
        require(jenis == SalesTarget.HARIAN || jenis == SalesTarget.BULANAN) { "Jenis target tidak valid" }
        db.salesTargetDao().upsert(
            SalesTargetEntity(
                jenis = jenis,
                nominal = nominal.coerceAtLeast(0L),
                updatedAt = RepositorySupport.now(),
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "sales_target", jenis, "UPDATE")
    }

    companion object {
        const val HARIAN = "HARIAN"
        const val BULANAN = "BULANAN"
    }
}
