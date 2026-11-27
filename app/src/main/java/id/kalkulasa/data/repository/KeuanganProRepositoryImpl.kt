package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.ProfitShareEntity
import id.kalkulasa.data.local.entity.RecurringExpenseEntity
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.ProfitShare
import id.kalkulasa.domain.model.RecurringExpense
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.ProfitShareRepository
import id.kalkulasa.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecurringExpenseRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val financeRepository: FinanceRepository,
    private val deviceId: String,
) : RecurringExpenseRepository {

    override fun observeActive(): Flow<List<RecurringExpense>> =
        db.recurringExpenseDao().observeActive().map { rows ->
            rows.map { RecurringExpense(it.kategoriNama, it.nominal, it.hariPerBulan, it.aktif) }
        }

    override suspend fun save(kategori: String, nominal: Long, hari: Int): Result<Unit> = runCatching {
        require(kategori.isNotBlank()) { "Kategori wajib diisi" }
        db.recurringExpenseDao().upsert(
            RecurringExpenseEntity(
                id = RepositorySupport.newId(), kategoriNama = kategori.trim(),
                nominal = nominal.coerceAtLeast(0L), hariPerBulan = hari.coerceIn(1, 28),
                aktif = true, terakhirDijalankan = null,
                updatedAt = RepositorySupport.now(), deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "recurring_expense", kategori, "INSERT")
    }

    override suspend fun runOnce(id: String): Result<Unit> = runCatching {
        val row = db.recurringExpenseDao().getAll().firstOrNull { it.id == id }
            ?: throw IllegalStateException("Pengeluaran berulang tidak ditemukan")
        financeRepository.addExpense(null, row.kategoriNama, row.nominal, "Rutin bulan ini")
            .getOrThrow()
        db.recurringExpenseDao().upsert(
            row.copy(terakhirDijalankan = DateFormatter.fileStamp(System.currentTimeMillis()).take(6)),
        )
    }
}

class ProfitShareRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : ProfitShareRepository {

    override fun observeActive(): Flow<List<ProfitShare>> =
        db.profitShareDao().observeActive().map { rows ->
            rows.map { ProfitShare(it.namaPenerima, it.persentase, it.aktif) }
        }

    override suspend fun save(nama: String, persentase: Int): Result<Unit> = runCatching {
        require(nama.isNotBlank()) { "Nama penerima wajib diisi" }
        db.profitShareDao().upsert(
            ProfitShareEntity(
                id = RepositorySupport.newId(), namaPenerima = nama.trim(),
                persentase = persentase.coerceIn(0, 100), aktif = true,
                updatedAt = RepositorySupport.now(), deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "profit_share", nama, "INSERT")
    }
}
