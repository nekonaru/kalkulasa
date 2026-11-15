package id.kalkulasa.data.repository

import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.ChangeLogEntity
import java.util.UUID

/** Shared helpers for repository implementations: sync logging and id generation. */
object RepositorySupport {
    fun newId(): String = UUID.randomUUID().toString()

    fun now(): Long = System.currentTimeMillis()

    suspend fun log(changeLogDao: ChangeLogDao, entitas: String, entitasId: String, aksi: String) {
        changeLogDao.insert(
            ChangeLogEntity(
                id = newId(),
                entitas = entitas,
                entitasId = entitasId,
                aksi = aksi,
                createdAt = now(),
            ),
        )
    }
}
