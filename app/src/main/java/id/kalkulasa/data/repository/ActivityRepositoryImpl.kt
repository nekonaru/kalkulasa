package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.domain.model.ActivityLog
import id.kalkulasa.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
) : ActivityRepository {

    override fun observeRecent(limit: Int): Flow<List<ActivityLog>> =
        db.changeLogDao().observeRecent(limit).map { rows ->
            rows.map { ActivityLog(it.id, it.entitas, it.entitasId, it.aksi, it.createdAt) }
        }
}
