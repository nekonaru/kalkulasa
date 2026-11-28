package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.domain.model.CapitalEntryType
import id.kalkulasa.domain.model.CashBreakdown
import id.kalkulasa.domain.model.CategorySpend
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

    override suspend fun cashBreakdown(): CashBreakdown {
        val c = db.capitalEntryDao().cashComponents()
        return CashBreakdown(
            modal = c.modal,
            prive = c.prive,
            penjualanTunai = c.salesCash,
            penerimaanPiutang = c.receivablePaid,
            pembelianStok = c.purchases,
            pembayaranHutang = c.payablePaid,
            kasbon = c.kasbon,
            pengeluaran = c.expenses,
        )
    }

    override suspend fun cashBalance(): Long = cashBreakdown().saldo

    override suspend fun netCapital(): Long =
        db.capitalEntryDao().sumCapital() - db.capitalEntryDao().sumPrivateDraws()

    override suspend fun unsettledKasbon(): Long = db.kasbonDao().sumUnsettled()

    override suspend fun cashFlowBetween(
        from: Long,
        to: Long,
    ): id.kalkulasa.domain.usecase.ArusKasUseCase.Ringkasan {
        val penjualan = db.saleDao().sumRevenueBetweenExcludingMethod(
            from, to, id.kalkulasa.domain.model.PaymentMethod.HUTANG,
        )
        return id.kalkulasa.domain.usecase.ArusKasUseCase.ringkasan(
            penjualan = penjualan,
            piutangDiterima = db.receivableDao().sumPaidBetween(from, to),
            modalMasuk = db.capitalEntryDao().sumCapitalBetween(from, to),
            pembelian = db.purchaseDao().sumBetween(from, to),
            pengeluaran = db.expenseDao().sumBetween(from, to),
            prive = db.capitalEntryDao().sumPrivateDrawsBetween(from, to),
            hutangDibayar = db.payablePaymentDao().sumBetween(from, to),
            kasbon = db.kasbonDao().sumBetween(from, to),
        )
    }

    override suspend fun expensesByCategoryBetween(from: Long, to: Long): List<CategorySpend> =
        db.expenseDao().sumByCategoryBetween(from, to).map { CategorySpend(it.nama, it.total) }

    override suspend fun expensesBetween(from: Long, to: Long): Long =
        db.expenseDao().sumBetween(from, to)

    override suspend fun purchasesBetween(from: Long, to: Long): Long =
        db.purchaseDao().sumBetween(from, to)
}
