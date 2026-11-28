package id.kalkulasa.data.repository

import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.data.local.entity.EmployeePinEntity
import id.kalkulasa.data.local.entity.OvertimeEntity
import id.kalkulasa.data.local.entity.WorkShiftEntity
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.WorkShift
import id.kalkulasa.domain.repository.ShiftRepository
import id.kalkulasa.domain.usecase.ShiftUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

class ShiftRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
    private val deviceId: String,
) : ShiftRepository {

    override fun observeShifts(): Flow<List<WorkShift>> =
        db.workShiftDao().observeActive().map { rows ->
            rows.map { WorkShift(it.id, it.nama, it.jamMulaiMenit, it.jamSelesaiMenit, it.aktif) }
        }

    override suspend fun saveShift(nama: String, mulai: Int, selesai: Int): Result<Unit> = runCatching {
        require(nama.isNotBlank()) { "Nama shift wajib diisi" }
        db.workShiftDao().upsert(
            WorkShiftEntity(
                id = RepositorySupport.newId(), nama = nama.trim(),
                jamMulaiMenit = mulai.coerceIn(0, 1439), jamSelesaiMenit = selesai.coerceIn(0, 1439),
                aktif = true, updatedAt = RepositorySupport.now(), deviceId = deviceId,
            ),
        )
    }

    override suspend fun setPin(employeeId: String, pin: String): Result<Unit> = runCatching {
        require(pin.length == 6) { "PIN harus 6 digit" }
        val hash = withContext(Dispatchers.Default) {
            MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
        db.employeePinDao().upsert(
            EmployeePinEntity(
                id = RepositorySupport.newId(), employeeId = employeeId, pinHash = hash,
                updatedAt = RepositorySupport.now(), deviceId = deviceId,
            ),
        )
    }

    override suspend fun verifyPin(employeeId: String, pin: String): Boolean {
        val row = db.employeePinDao().getFor(employeeId) ?: return false
        val hash = withContext(Dispatchers.Default) {
            MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
        return row.pinHash == hash
    }

    override suspend fun addOvertime(employeeId: String, menit: Int, upahHarian: Long): Result<Unit> = runCatching {
        val now = RepositorySupport.now()
        db.overtimeDao().upsert(
            OvertimeEntity(
                id = RepositorySupport.newId(), employeeId = employeeId,
                tanggal = DateFormatter.startOfDay(now), menit = menit.coerceAtLeast(0),
                nominal = ShiftUseCase.upahLembur(upahHarian, menit), dibayar = false,
                updatedAt = now, deviceId = deviceId,
            ),
        )
    }
}
