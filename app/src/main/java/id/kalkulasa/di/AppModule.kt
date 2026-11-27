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
import id.kalkulasa.data.repository.EmployeeRepositoryImpl
import id.kalkulasa.data.repository.AttendanceRepositoryImpl
import id.kalkulasa.data.repository.KasbonRepositoryImpl
import id.kalkulasa.data.repository.PayrollRepositoryImpl
import id.kalkulasa.domain.repository.EmployeeRepository
import id.kalkulasa.domain.repository.AttendanceRepository
import id.kalkulasa.domain.repository.KasbonRepository
import id.kalkulasa.domain.repository.PayrollRepository
import id.kalkulasa.data.repository.PayableRepositoryImpl
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.data.repository.SalesTargetRepositoryImpl
import id.kalkulasa.domain.repository.SalesTargetRepository
import id.kalkulasa.data.repository.ActivityRepositoryImpl
import id.kalkulasa.domain.repository.ActivityRepository
import id.kalkulasa.data.repository.PayablePaymentRepositoryImpl
import id.kalkulasa.domain.repository.PayablePaymentRepository
import id.kalkulasa.data.repository.VoucherRepositoryImpl
import id.kalkulasa.domain.repository.VoucherRepository
import id.kalkulasa.data.repository.DepositRepositoryImpl
import id.kalkulasa.domain.repository.DepositRepository
import id.kalkulasa.data.repository.PreorderRepositoryImpl
import id.kalkulasa.domain.repository.PreorderRepository
import id.kalkulasa.data.repository.BundleRepositoryImpl
import id.kalkulasa.domain.repository.BundleRepository
import id.kalkulasa.data.repository.BudgetRepositoryImpl
import id.kalkulasa.domain.repository.BudgetRepository
import id.kalkulasa.data.repository.CashAuditRepositoryImpl
import id.kalkulasa.domain.repository.CashAuditRepository
import id.kalkulasa.data.repository.SupplierRepositoryImpl
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
import id.kalkulasa.domain.repository.SupplierRepository
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
    fun provideSupplierRepository(db: KalkulasaDatabase): SupplierRepository =
        SupplierRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideEmployeeRepository(db: KalkulasaDatabase, deviceId: String): EmployeeRepository =
        EmployeeRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideAttendanceRepository(db: KalkulasaDatabase, deviceId: String): AttendanceRepository =
        AttendanceRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideKasbonRepository(db: KalkulasaDatabase, deviceId: String): KasbonRepository =
        KasbonRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun providePayrollRepository(db: KalkulasaDatabase, deviceId: String): PayrollRepository =
        PayrollRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun providePayableRepository(db: KalkulasaDatabase, deviceId: String): PayableRepository =
        PayableRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideSalesTargetRepository(db: KalkulasaDatabase, deviceId: String): SalesTargetRepository =
        SalesTargetRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideActivityRepository(db: KalkulasaDatabase): ActivityRepository =
        ActivityRepositoryImpl(db)

    @Provides
    @Singleton
    fun providePayablePaymentRepository(db: KalkulasaDatabase): PayablePaymentRepository =
        PayablePaymentRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideVoucherRepository(db: KalkulasaDatabase, deviceId: String): VoucherRepository =
        VoucherRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideDepositRepository(db: KalkulasaDatabase, deviceId: String): DepositRepository =
        DepositRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun providePreorderRepository(db: KalkulasaDatabase, deviceId: String): PreorderRepository =
        PreorderRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideBundleRepository(db: KalkulasaDatabase, deviceId: String): BundleRepository =
        BundleRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideBudgetRepository(db: KalkulasaDatabase, deviceId: String): BudgetRepository =
        BudgetRepositoryImpl(db, deviceId)

    @Provides
    @Singleton
    fun provideCashAuditRepository(
        db: KalkulasaDatabase,
        deviceId: String,
        financeRepository: id.kalkulasa.domain.repository.FinanceRepository,
    ): CashAuditRepository = CashAuditRepositoryImpl(db, deviceId, financeRepository)

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
