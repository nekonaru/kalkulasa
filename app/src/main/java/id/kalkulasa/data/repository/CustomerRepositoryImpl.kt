package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.domain.model.Customer
import id.kalkulasa.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
) : CustomerRepository {

    override fun observeAll(): Flow<List<Customer>> =
        db.customerDao().observeAll("").map { list -> list.map { it.toDomain() } }

    override fun observeAll(query: String): Flow<List<Customer>> =
        db.customerDao().observeAll(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Customer? = db.customerDao().getById(id)?.toDomain()
    override suspend fun getByName(name: String): Customer? = db.customerDao().findByName(name.trim())?.toDomain()

    override suspend fun resetPoints(id: String) {
        val customer = db.customerDao().getById(id) ?: return
        db.customerDao().upsert(
            customer.copy(
                poin = 0,
                updatedAt = RepositorySupport.now(),
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "customer", id, "UPDATE")
    }

    override suspend fun updateNotes(id: String, catatan: String?, tag: String?) {
        val customer = db.customerDao().getById(id) ?: return
        db.customerDao().upsert(
            customer.copy(
                catatan = catatan?.ifBlank { null },
                tag = tag?.ifBlank { null },
                updatedAt = RepositorySupport.now(),
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "customer", id, "UPDATE")
    }
}

fun id.kalkulasa.data.local.entity.CustomerEntity.toDomain() = Customer(
    id = id,
    nama = nama,
    poin = poin,
    kunjungan = kunjungan,
    totalBelanja = totalBelanja,
    catatan = catatan,
    tag = tag,
)
