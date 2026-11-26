package id.kalkulasa.domain.usecase

/** Budget progress and cash reconciliation rules. Pure Kotlin, unit-tested. */
object KeuanganLanjutanUseCase {

    fun persentaseBudget(terpakai: Long, anggaran: Long): Double {
        if (anggaran <= 0L) return 0.0
        return (terpakai.toDouble() / anggaran.toDouble() * 100.0).coerceAtMost(100.0)
    }

    fun selisihKas(sistem: Long, fisik: Long): Long = fisik - sistem

    fun statusSelisih(selisih: Long): String = when {
        selisih == 0L -> "SESUAI"
        selisih > 0L -> "LEBIH"
        else -> "KURANG"
    }
}
