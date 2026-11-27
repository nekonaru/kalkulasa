package id.kalkulasa.domain.usecase

/** Recurring expense and profit sharing rules. Pure Kotlin, unit-tested. */
object KeuanganProUseCase {

    fun jatuhBulanIni(hariPerBulan: Int, hariTerakhirBulan: Int): Int =
        hariPerBulan.coerceIn(1, hariTerakhirBulan.coerceAtLeast(1))

    fun bagianLaba(labaBersih: Long, persentase: Int): Long {
        if (labaBersih <= 0L) return 0L
        return (labaBersih.toDouble() * persentase.coerceIn(0, 100) / 100.0).toLong()
    }

    fun totalBagian(labaBersih: Long, persentaseList: List<Int>): Long =
        persentaseList.sumOf { bagianLaba(labaBersih, it) }
}
