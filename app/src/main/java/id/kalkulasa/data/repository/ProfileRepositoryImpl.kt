package id.kalkulasa.data.repository

import id.kalkulasa.data.local.dao.BusinessProfileDao
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.entity.BusinessProfileEntity
import id.kalkulasa.data.mapper.toDomain
import id.kalkulasa.domain.model.BusinessProfile
import id.kalkulasa.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val dao: BusinessProfileDao,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : ProfileRepository {
    override fun observeProfile(): Flow<BusinessProfile?> = dao.observeProfile().map { it?.toDomain() }

    override suspend fun getProfile(): BusinessProfile? = dao.getProfile()?.toDomain()

    override suspend fun saveProfile(profile: BusinessProfile) {
        val existing = dao.getProfile()
        val now = RepositorySupport.now()
        dao.upsert(
            BusinessProfileEntity(
                id = existing?.id ?: profile.id,
                namaUsaha = profile.namaUsaha,
                jenisUsaha = profile.jenisUsaha,
                modalAwal = profile.modalAwal,
                dibuatPada = existing?.dibuatPada ?: profile.dibuatPada,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(changeLogDao, "business_profile", existing?.id ?: profile.id, if (existing == null) "INSERT" else "UPDATE")
    }
}
