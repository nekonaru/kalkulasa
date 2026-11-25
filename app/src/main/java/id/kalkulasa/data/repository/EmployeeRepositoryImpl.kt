package id.kalkulasa.data.repository

import androidx.room.withTransaction
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.AttendanceEntity
import id.kalkulasa.data.local.entity.EmployeeEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.KasbonEntity
import id.kalkulasa.data.local.entity.PayrollEntity
import id.kalkulasa.domain.model.Attendance
import id.kalkulasa.domain.model.Employee
import id.kalkulasa.domain.model.Kasbon
import id.kalkulasa.domain.model.PayrollRecord
import id.kalkulasa.domain.repository.AttendanceRepository
import id.kalkulasa.domain.repository.EmployeeRepository
import id.kalkulasa.domain.repository.KasbonRepository
import id.kalkulasa.domain.repository.PayrollRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmployeeRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : EmployeeRepository {

    override fun observeAll(): Flow<List<Employee>> =
        db.employeeDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Employee? = db.employeeDao().getById(id)?.toDomain()

    override suspend fun save(employee: Employee) {
        val now = RepositorySupport.now()
        db.employeeDao().upsert(
            EmployeeEntity(
                id = employee.id,
                nama = employee.nama.trim(),
                jabatan = employee.jabatan?.ifBlank { null },
                telepon = employee.telepon?.ifBlank { null },
                upahHarian = employee.upahHarian.coerceAtLeast(0L),
                aktif = employee.aktif,
                dibuatPada = db.employeeDao().getById(employee.id)?.dibuatPada ?: now,
                updatedAt = now,
                deviceId = deviceId,
            ),
        )
        RepositorySupport.log(db.changeLogDao(), "employee", employee.id, "UPDATE")
    }

    override suspend fun setActive(id: String, aktif: Boolean) {
        db.employeeDao().setActive(id, aktif, RepositorySupport.now())
        RepositorySupport.log(db.changeLogDao(), "employee", id, "UPDATE")
    }

    private fun EmployeeEntity.toDomain() = Employee(
        id = id, nama = nama, jabatan = jabatan, telepon = telepon,
        upahHarian = upahHarian, aktif = aktif,
    )
}

class AttendanceRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : AttendanceRepository {

    override fun observeBetween(from: Long, to: Long): Flow<List<Attendance>> =
        db.attendanceDao().observeBetween(from, to).map { list -> list.map { it.toDomain() } }

    override suspend fun getBetween(from: Long, to: Long): List<Attendance> =
        db.attendanceDao().getBetween(from, to).map { it.toDomain() }

    override suspend fun mark(
        karyawanId: String,
        tanggal: Long,
        status: String,
        upahDibayar: Long,
        catatan: String?,
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val employee = db.employeeDao().getById(karyawanId)
                ?: throw IllegalStateException("Karyawan tidak ditemukan")
            val now = RepositorySupport.now()
            val existing = db.attendanceDao().getForEmployeeAndDate(karyawanId, tanggal)
            val id = existing?.id ?: RepositorySupport.newId()
            db.attendanceDao().upsert(
                AttendanceEntity(
                    id = id,
                    karyawanId = karyawanId,
                    tanggal = tanggal,
                    status = status,
                    upahDibayar = upahDibayar,
                    catatan = catatan?.ifBlank { null },
                    waktu = existing?.waktu ?: now,
                    updatedAt = now,
                    deviceId = deviceId,
                ),
            )
            RepositorySupport.log(db.changeLogDao(), "attendance", id, if (existing == null) "INSERT" else "UPDATE")
            // keep local variable referenced so compiler doesn't warn
            employee.id
        }
    }

    private fun AttendanceEntity.toDomain() = Attendance(
        id = id, karyawanId = karyawanId, tanggal = tanggal,
        status = status, upahDibayar = upahDibayar, catatan = catatan,
    )
}

class KasbonRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : KasbonRepository {

    override suspend fun add(karyawanId: String, nominal: Long, catatan: String?) {
        if (nominal <= 0) return
        val now = RepositorySupport.now()
        val id = RepositorySupport.newId()
        db.withTransaction {
            db.kasbonDao().upsert(
                KasbonEntity(
                    id = id, karyawanId = karyawanId, nominal = nominal,
                    catatan = catatan?.ifBlank { null }, waktu = now, updatedAt = now, deviceId = deviceId,
                ),
            )
            RepositorySupport.log(db.changeLogDao(), "kasbon", id, "INSERT")
        }
    }

    override suspend fun getForEmployee(karyawanId: String, from: Long, to: Long): List<Kasbon> =
        db.kasbonDao().getForEmployee(karyawanId, from, to).map {
            Kasbon(it.id, it.karyawanId, it.nominal, it.catatan, it.waktu)
        }
}

class PayrollRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : PayrollRepository {

    override fun observeAll(): Flow<List<PayrollRecord>> =
        db.payrollDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun pay(
        karyawan: Employee,
        from: Long,
        to: Long,
        upah: Long,
        kasbon: Long,
        dibayar: Long,
        kategoriNama: String,
        catatan: String,
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val now = RepositorySupport.now()
            val id = RepositorySupport.newId()
            db.payrollDao().upsert(
                PayrollEntity(
                    id = id, karyawanId = karyawan.id, namaKaryawan = karyawan.nama,
                    periodeAwal = from, periodeAkhir = to,
                    totalUpah = upah, totalKasbon = kasbon, dibayar = dibayar,
                    waktu = now, updatedAt = now, deviceId = deviceId,
                ),
            )
            // Payroll and its expense must succeed together. A partial write would
            // lock the Pay button forever while leaving cash untouched.
            val expenseId = RepositorySupport.newId()
            db.expenseDao().insert(
                ExpenseEntity(
                    id = expenseId,
                    kategoriId = null,
                    kategoriNama = kategoriNama,
                    nominal = dibayar,
                    catatan = catatan.ifBlank { null },
                    waktu = now,
                    updatedAt = now,
                    deviceId = deviceId,
                ),
            )
            RepositorySupport.log(db.changeLogDao(), "payroll", id, "INSERT")
            RepositorySupport.log(db.changeLogDao(), "expense", expenseId, "INSERT")
        }
    }

    private fun PayrollEntity.toDomain() = PayrollRecord(
        id = id, karyawanId = karyawanId, namaKaryawan = namaKaryawan,
        periodeAwal = periodeAwal, periodeAkhir = periodeAkhir,
        totalUpah = totalUpah, totalKasbon = totalKasbon, dibayar = dibayar, waktu = waktu,
    )
}
