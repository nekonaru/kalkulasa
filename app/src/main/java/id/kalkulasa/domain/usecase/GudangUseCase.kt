package id.kalkulasa.domain.usecase

/** Warehouse rules. Pure Kotlin, unit-tested. */
object GudangUseCase {

    /** Suggested reorder quantity from daily sales, supplier lead time and safety stock. */
    fun saranRestok(
        penjualanHarianRata2: Double,
        leadTimeDays: Int,
        stokSekarang: Double,
        stokMinimum: Double,
    ): Double {
        val kebutuhan = penjualanHarianRata2 * leadTimeDays.coerceAtLeast(0) + stokMinimum
        return (kebutuhan - stokSekarang).coerceAtLeast(0.0)
    }

    /** Barcode internal: prefix plus a compact unique suffix. */
    fun barcodeInternal(id: String): String = "KLK-" + id.take(8).uppercase()

    /** Expiring within N days, computed from midnight-based day differences. */
    fun mendekatiKedaluwarsa(expiry: Long, now: Long, days: Int): Boolean {
        if (expiry <= 0L) return false
        val diff = expiry - now
        return diff >= 0L && diff <= days.toLong() * 24 * 60 * 60 * 1000
    }
}
