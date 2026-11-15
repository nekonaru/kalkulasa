package id.kalkulasa.data.repository

import id.kalkulasa.data.local.dao.CategoryDao
import id.kalkulasa.data.local.dao.ChangeLogDao
import id.kalkulasa.data.local.dao.ProductDao
import id.kalkulasa.data.local.entity.CategoryEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.mapper.toDomain
import id.kalkulasa.domain.model.Category
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.repository.CategoryRepository
import id.kalkulasa.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : ProductRepository {
    override fun observeActive(query: String): Flow<List<Product>> =
        dao.observeActive(query).map { list -> list.map { it.toDomain() } }

    override fun observeAll(query: String): Flow<List<Product>> =
        dao.observeAll(query).map { list -> list.map { it.toDomain() } }

    override fun observeLowStockCount(): Flow<Int> = dao.observeLowStockCount()

    override fun observeLowStock(): Flow<List<Product>> =
        dao.observeLowStock().map { list -> list.map { it.toDomain() } }

    override fun observeInventoryValue(): Flow<Long> = dao.observeInventoryValue()

    override suspend fun getById(id: String): Product? = dao.getById(id)?.toDomain()

    override suspend fun getAll(): List<Product> = dao.getAllActive().map { it.toDomain() }

    override suspend fun save(product: Product) {
        val existing = dao.getById(product.id)
        val now = RepositorySupport.now()
        dao.upsert(
            ProductEntity(
                id = product.id,
                nama = product.nama,
                kategoriId = product.kategoriId,
                satuan = product.satuan,
                barcode = product.barcode?.ifBlank { null },
                hargaBeliRata2 = product.hargaBeliRata2,
                hargaJual = product.hargaJual,
                stok = product.stok,
                stokMinimum = product.stokMinimum,
                fotoUri = product.fotoUri,
                diarsipkan = product.diarsipkan,
                updatedAt = now,
                deletedAt = null,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(changeLogDao, "product", product.id, if (existing == null) "INSERT" else "UPDATE")
    }

    override suspend fun setArchived(id: String, archived: Boolean) {
        dao.setArchived(id, archived, RepositorySupport.now())
        RepositorySupport.log(changeLogDao, "product", id, "UPDATE")
    }
}

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val changeLogDao: ChangeLogDao,
    private val deviceId: String,
) : CategoryRepository {
    override fun observeByType(jenis: String): Flow<List<Category>> =
        dao.observeByType(jenis).map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addCategory(name: String, jenis: String) {
        val now = RepositorySupport.now()
        val id = RepositorySupport.newId()
        dao.upsert(
            CategoryEntity(
                id = id,
                nama = name,
                jenis = jenis,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(changeLogDao, "category", id, "INSERT")
    }
}
