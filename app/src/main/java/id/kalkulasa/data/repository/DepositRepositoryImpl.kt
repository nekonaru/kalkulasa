package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.DepositEntity
import id.kalkulasa.domain.model.Deposit
import id.kalkulasa.domain.repository.DepositRepository
import id.kalkulasa.domain.usecase.DepositUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DepositRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : DepositRepository {

    override fun observeAll(): Flow<List<Deposit>> =
        db.depositDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun add(nama: String, nominal: Long, catatan: String?): Result<Unit> = runCatching {
        require(nama.isNotBlank()) { "Nama pelanggan wajib diisi" }
        require(nominal > 0) { "Nominal harus lebih dari 0" }
        val now = RepositorySupport.now()
        db.depositDao().upsert(
            DepositEntity(
                id = RepositorySupport.newId(),
                namaPelanggan = nama.trim(),
                nominal = nominal,
                terpakai = 0L,
                waktu = now,
                catatan = catatan?.ifBlank { null },
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "deposit", RepositorySupport.newId(), "INSERT")
    }

    override suspend fun use(nama: String, jumlah: Long): Result<Unit> = runCatching {
        db.withTransaction {
            val rows = db.depositDao().getForCustomer(nama.trim())
            val available = DepositUseCase.totalSisa(rows.map { it.nominal to it.terpakai })
            DepositUseCase.validasiPenarikan(available, jumlah).getOrThrow()
            var remaining = jumlah
            rows.forEach { row ->
                if (remaining <= 0L) return@forEach
                val sisa = DepositUseCase.sisa(row.nominal, row.terpakai)
                val taken = minOf(sisa, remaining)
                if (taken > 0L) {
                    db.depositDao().upsert(row.copy(terpakai = row.terpakai + taken, updatedAt = RepositorySupport.now()))
                    remaining -= taken
                }
            }
            RepositorySupport.log(db.changeLogDao(), "deposit", nama.trim(), "UPDATE")
        }
    }

    private fun DepositEntity.toDomain() = Deposit(id, namaPelanggan, nominal, DepositUseCase.sisa(nominal, terpakai), waktu, catatan)
}
