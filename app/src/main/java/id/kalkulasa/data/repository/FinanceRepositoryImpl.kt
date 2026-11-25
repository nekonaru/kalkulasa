package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.domain.model.CapitalEntryType
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.mapper.toDomain
import id.kalkulasa.domain.model.CapitalEntry
import id.kalkulasa.domain.model.Expense
import id.kalkulasa.domain.model.PurchaseRecord
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.usecase.HitungHppUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : FinanceRepository {

    override fun observeRecentExpenses(limit: Int): Flow<List<Expense>> =
        db.expenseDao().observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeRecentCapitalEntries(limit: Int): Flow<List<CapitalEntry>> =
        db.capitalEntryDao().observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeRecentPurchases(limit: Int): Flow<List<PurchaseRecord>> =
        db.purchaseDao().observeRecent(limit).map { list ->
            list.map { PurchaseRecord(it.id, it.pemasok, it.total, it.catatan, it.waktu) }
        }

    override suspend fun addCapital(tipe: String, nominal: Long, catatan: String?) {
        val now = RepositorySupport.now()
        val id = RepositorySupport.newId()
        db.capitalEntryDao().insert(
            CapitalEntryEntity(
                id = id,
                tipe = tipe,
                nominal = nominal,
                catatan = catatan?.ifBlank { null },
                waktu = now,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(changeLogDao, "capital_entry", id, "INSERT")
    }

    override suspend fun addExpense(kategoriId: String?, kategoriNama: String, nominal: Long, catatan: String?): Result<Unit> = runCatching {
        val now = RepositorySupport.now()
        val id = RepositorySupport.newId()
        db.expenseDao().insert(
            ExpenseEntity(
                id = id,
                kategoriId = kategoriId,
                kategoriNama = kategoriNama,
                nominal = nominal,
                catatan = catatan?.ifBlank { null },
                waktu = now,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(changeLogDao, "expense", id, "INSERT")
    }

    override suspend fun cashBalance(): Long {
        val now = RepositorySupport.now()
        val allTimeFrom = 0L
        // Hutang (credit) sales are not cash yet; count only cash/transfer/QRIS sales plus debt payments received.
        val salesCash = db.saleDao().sumRevenueBetweenExcludingMethod(allTimeFrom, now, id.kalkulasa.domain.model.PaymentMethod.HUTANG)
        val debtReceived = db.receivableDao().sumPaid()
        val purchases = db.purchaseDao().sumBetween(allTimeFrom, now)
        val expenses = db.expenseDao().sumBetween(allTimeFrom, now)
        val capital = db.capitalEntryDao().sumCapital()
        val draws = db.capitalEntryDao().sumPrivateDraws()
        return HitungHppUseCase.cashBalance(capital, draws, salesCash + debtReceived, purchases, expenses)
    }

    override suspend fun expensesBetween(from: Long, to: Long): Long =
        db.expenseDao().sumBetween(from, to)

    override suspend fun purchasesBetween(from: Long, to: Long): Long =
        db.purchaseDao().sumBetween(from, to)
}
