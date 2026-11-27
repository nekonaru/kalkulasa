package id.kalkulasa.domain.usecase

/** CRM rules. Pure Kotlin, unit-tested. */
object CrmUseCase {

    /** Days until next birthday, ignoring the birth year. */
    fun hariMenujuUlangTahun(tanggalLahir: Long, now: Long): Int? {
        if (tanggalLahir <= 0L) return null
        val lahir = java.util.Calendar.getInstance().apply { timeInMillis = tanggalLahir }
        val hariIni = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val next = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.MONTH, lahir.get(java.util.Calendar.MONTH))
            set(java.util.Calendar.DAY_OF_MONTH, lahir.get(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        if (next.timeInMillis < hariIni.timeInMillis) next.add(java.util.Calendar.YEAR, 1)
        val diff = next.timeInMillis - hariIni.timeInMillis
        return (diff / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
    }

    /** Simple RFM band: 1 (low) to 5 (high) from visits and spend. */
    fun skorRfm(kunjungan: Int, totalBelanja: Long): Int {
        val f = (kunjungan.coerceAtLeast(0) / 10).coerceAtMost(4)
        val m = (totalBelanja.coerceAtLeast(0L) / 1_000_000L).coerceAtMost(4)
        return (f + m + 1).toInt().coerceAtMost(5)
    }
}
