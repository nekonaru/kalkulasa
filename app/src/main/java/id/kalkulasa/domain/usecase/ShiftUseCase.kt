package id.kalkulasa.domain.usecase

/** Shift and overtime rules. Pure Kotlin, unit-tested. */
object ShiftUseCase {

    /** Overtime minutes past the scheduled end, ignoring early arrival. */
    fun lemburMenit(jamSelesaiAktual: Int, jamSelesaiJadwal: Int): Int =
        (jamSelesaiAktual - jamSelesaiJadwal).coerceAtLeast(0)

    /** Overtime pay from daily wage and an 8-hour day. */
    fun upahLembur(upahHarian: Long, menit: Int): Long {
        if (upahHarian <= 0L || menit <= 0) return 0L
        return kotlin.math.round(upahHarian.toDouble() / 480.0 * menit).toLong()
    }
}
