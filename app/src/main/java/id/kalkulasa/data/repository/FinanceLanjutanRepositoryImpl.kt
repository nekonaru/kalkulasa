package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.BudgetEntity
import id.kalkulasa.data.local.entity.CashAuditEntity
import id.kalkulasa.domain.model.Budget
import id.kalkulasa.domain.model.CashAudit
import id.kalkulasa.domain.repository.BudgetRepository
import id.kalkulasa.domain.repository.CashAuditRepository
import id.kalkulasa.core.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : BudgetRepository {

    override fun observeForMonth(bulan: String): Flow<List<Budget>> =
        db.budgetDao().observeForMonth(bulan).map { rows ->
            rows.map { Budget(it.kategoriNama, it.nominal, it.bulan) }
        }

    override suspend fun save(kategori: String, nominal: Long, bulan: String): Result<Unit> = runCatching {
        require(kategori.isNotBlank()) { "Kategori wajib diisi" }
        db.budgetDao().upsert(
            BudgetEntity(
                id = "${bulan}-${kategori.trim()}",
                kategoriNama = kategori.trim(),
                nominal = nominal.coerceAtLeast(0L),
                bulan = bulan,
                updatedAt = RepositorySupport.now(),
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "budget", bulan, "UPDATE")
    }
}

class CashAuditRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
    private val financeRepository: id.kalkulasa.domain.repository.FinanceRepository,
) : CashAuditRepository {

    override fun observeRecent(limit: Int): Flow<List<CashAudit>> =
        db.cashAuditDao().observeRecent(limit).map { rows ->
            rows.map {
                CashAudit(
                    it.id, it.tanggal, it.sistem, it.fisik, it.selisih,
                    it.catatan, it.waktu,
                )
            }
        }

    override suspend fun record(sistem: Long, fisik: Long, catatan: String?): Result<Unit> = runCatching {
        val now = RepositorySupport.now()
        db.cashAuditDao().insert(
            CashAuditEntity(
                id = RepositorySupport.newId(),
                tanggal = DateFormatter.startOfDay(now),
                sistem = sistem,
                fisik = fisik,
                selisih = fisik - sistem,
                catatan = catatan?.ifBlank { null },
                waktu = now,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "cash_audit", "cash", "INSERT")
    }
}
