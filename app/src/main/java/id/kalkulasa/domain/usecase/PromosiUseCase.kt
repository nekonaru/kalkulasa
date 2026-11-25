package id.kalkulasa.domain.usecase

/** Promotion and loyalty redemption rules. Pure Kotlin, unit-tested. */
object PromosiUseCase {

    /** One loyalty point is worth Rp100 when redeemed. Earning stays at 1 point per Rp1.000. */
    const val NILAI_POIN: Long = 100L

    /** Convert a percentage discount into rupiah, capped at the subtotal. */
    fun diskonPersen(subtotal: Long, persen: Int): Long {
        if (subtotal <= 0L || persen <= 0) return 0L
        return (subtotal.toDouble() * persen.coerceAtMost(100) / 100.0).toLong().coerceAtMost(subtotal)
    }

    fun nilaiPenukaranPoin(poin: Int): Long = poin.coerceAtLeast(0).toLong() * NILAI_POIN

    /** A customer cannot redeem more points than they own. */
    fun validasiPenukaran(poinTersedia: Long, poinDitukar: Int): Result<Unit> {
        if (poinDitukar < 0) return Result.failure(IllegalArgumentException("Poin tidak valid"))
        if (poinTersedia < poinDitukar) {
            return Result.failure(IllegalArgumentException("Poin pelanggan tidak cukup"))
        }
        return Result.success(Unit)
    }

    /** Final discount must never exceed the cart subtotal. */
    fun gabungkanDiskon(subtotal: Long, diskonRupiah: Long, potonganPoin: Long): Long =
        (diskonRupiah.coerceAtLeast(0L) + potonganPoin.coerceAtLeast(0L)).coerceAtMost(subtotal.coerceAtLeast(0L))
}
