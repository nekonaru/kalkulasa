package id.kalkulasa.domain.usecase

import id.kalkulasa.domain.model.Attendance
import id.kalkulasa.domain.model.Employee
import id.kalkulasa.domain.model.GajiKaryawan
import id.kalkulasa.domain.model.Kasbon

/** Payroll rules. Pure Kotlin, unit-tested. No Android dependencies. */
object HitungGajiUseCase {

    const val HADIR = "HADIR"
    const val IZIN = "IZIN"
    const val SAKIT = "SAKIT"
    const val ALPHA = "ALPHA"

    val STATUSES = listOf(HADIR, IZIN, SAKIT, ALPHA)

    /** Wage factor per attendance status. IZIN and SAKIT are unpaid by default. */
    fun upahUntukStatus(upahHarian: Long, status: String): Long = when (status) {
        HADIR -> upahHarian.coerceAtLeast(0L)
        else -> 0L
    }

    /**
     * Summarizes one employee's payroll for a period.
     * Total wage is taken from the snapshot in each attendance row so changing
     * the daily wage later never rewrites historical payroll.
     */
    fun hitung(
        karyawan: Employee,
        absensi: List<Attendance>,
        kasbon: List<Kasbon>,
        sudahDibayar: Long,
    ): GajiKaryawan {
        val hadir = absensi.count { it.status == HADIR }
        val izin = absensi.count { it.status == IZIN }
        val sakit = absensi.count { it.status == SAKIT }
        val alpha = absensi.count { it.status == ALPHA }
        val totalUpah = absensi.sumOf { row ->
            if (row.status == HADIR) row.upahDibayar.coerceAtLeast(0L) else 0L
        }
        val totalKasbon = kasbon.sumOf { it.nominal.coerceAtLeast(0L) }
        return GajiKaryawan(
            karyawan = karyawan,
            hadir = hadir,
            izin = izin,
            sakit = sakit,
            alpha = alpha,
            totalUpah = totalUpah,
            totalKasbon = totalKasbon,
            dibayar = (totalUpah - totalKasbon - sudahDibayar.coerceAtLeast(0L)).coerceAtLeast(0L),
        )
    }
}
