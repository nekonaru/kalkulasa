package id.kalkulasa.domain.usecase

/** Employee performance ranking. Pure Kotlin, unit-tested. */
object KaryawanProUseCase {

    fun skor(omzet: Long, transaksi: Int, hadir: Int): Double {
        val o = omzet.coerceAtLeast(0L).toDouble() / 1_000_000.0 * 40.0
        val t = transaksi.coerceAtLeast(0).toDouble() / 100.0 * 30.0
        val h = hadir.coerceAtLeast(0).toDouble() / 22.0 * 30.0
        return (o + t + h).coerceAtMost(100.0)
    }

    fun slipGaji(upah: Long, kasbon: Long, lembur: Long, dibayar: Long): String =
        "Upah: $upah\nKasbon: -$kasbon\nLembur: +$lembur\nDibayar: $dibayar"
}
