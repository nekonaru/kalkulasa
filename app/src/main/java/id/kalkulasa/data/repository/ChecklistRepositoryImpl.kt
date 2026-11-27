package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.ChecklistItemEntity
import id.kalkulasa.data.local.entity.ChecklistLogEntity
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.ChecklistItem
import id.kalkulasa.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChecklistRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : ChecklistRepository {

    override fun observeItems(): Flow<List<ChecklistItem>> {
        return db.checklistDao().observeItems().map { items ->
            val today = DateFormatter.startOfDay()
            val done = db.checklistDao().logsFor(today).filter { it.selesai }.map { it.itemId }.toSet()
            items.map { ChecklistItem(it.id, it.nama, it.fase, it.id in done) }
        }
    }

    override suspend fun toggle(itemId: String, selesai: Boolean): Result<Unit> = runCatching {
        val today = DateFormatter.startOfDay()
        val existing = db.checklistDao().logsFor(today).firstOrNull { it.itemId == itemId }
        db.checklistDao().upsertLog(
            existing?.copy(selesai = selesai)
                ?: ChecklistLogEntity(
                    id = RepositorySupport.newId(), itemId = itemId, tanggal = today,
                    selesai = selesai, updatedAt = RepositorySupport.now(), deviceId = deviceId,
                ),
        )
    }

    override suspend fun add(nama: String, fase: String): Result<Unit> = runCatching {
        require(nama.isNotBlank()) { "Nama checklist wajib diisi" }
        db.checklistDao().upsertItem(
            ChecklistItemEntity(
                id = RepositorySupport.newId(), nama = nama.trim(), fase = fase,
                aktif = true, updatedAt = RepositorySupport.now(), deviceId = deviceId,
            ),
        )
    }
}
