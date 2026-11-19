package id.kalkulasa.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.kalkulasa.data.local.DatabaseKeyProvider
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.repository.BackupRepositoryImpl
import id.kalkulasa.data.repository.FinanceRepositoryImpl
import id.kalkulasa.data.repository.ProductRepositoryImpl
import id.kalkulasa.data.repository.ProfileRepositoryImpl
import id.kalkulasa.data.repository.ReceivableRepositoryImpl
import id.kalkulasa.data.repository.ReportRepositoryImpl
import id.kalkulasa.data.repository.SaleRepositoryImpl
import id.kalkulasa.data.repository.StockRepositoryImpl
import id.kalkulasa.data.repository.CategoryRepositoryImpl
import id.kalkulasa.data.repository.CustomerRepositoryImpl
import id.kalkulasa.domain.repository.BackupRepository
import id.kalkulasa.domain.repository.CategoryRepository
import id.kalkulasa.domain.repository.CustomerRepository
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.repository.ReceivableRepository
import id.kalkulasa.domain.repository.ReportRepository
import id.kalkulasa.domain.repository.SaleRepository
import id.kalkulasa.domain.repository.StockRepository
import java.io.File
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDeviceId(@ApplicationContext context: Context): String {
        val file = File(context.filesDir, "device_id")
        if (file.exists()) return file.readText().trim()
        val id = UUID.randomUUID().toString()
        file.writeText(id)
        return id
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        deviceId: String,
        keyProvider: DatabaseKeyProvider,
    ): KalkulasaDatabase =
        KalkulasaDatabase.build(context, deviceId, keyProvider.getPassphrase())

    @Provides
    @Singleton
    fun provideProfileRepository(db: KalkulasaDatabase, deviceId: String): ProfileRepository =
        ProfileRepositoryImpl(db.businessProfileDao(), db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideProductRepository(db: KalkulasaDatabase, deviceId: String): ProductRepository =
        ProductRepositoryImpl(db, db.productDao(), db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideCategoryRepository(db: KalkulasaDatabase, deviceId: String): CategoryRepository =
        CategoryRepositoryImpl(db.categoryDao(), db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideStockRepository(db: KalkulasaDatabase, deviceId: String): StockRepository =
        StockRepositoryImpl(db, db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideSaleRepository(db: KalkulasaDatabase, deviceId: String): SaleRepository =
        SaleRepositoryImpl(db, db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideReceivableRepository(db: KalkulasaDatabase, deviceId: String): ReceivableRepository =
        ReceivableRepositoryImpl(db, db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideCustomerRepository(db: KalkulasaDatabase): CustomerRepository =
        CustomerRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideFinanceRepository(db: KalkulasaDatabase, deviceId: String): FinanceRepository =
        FinanceRepositoryImpl(db, db.changeLogDao(), deviceId)

    @Provides
    @Singleton
    fun provideReportRepository(db: KalkulasaDatabase): ReportRepository =
        ReportRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideBackupRepository(db: KalkulasaDatabase): BackupRepository =
        BackupRepositoryImpl(db)
}
