package id.kalkulasa.domain.usecase

/** Preorder and bundle rules. Pure Kotlin, unit-tested. */
object PreorderBundlingUseCase {

    const val DIPESAN = "DIPESAN"
    const val DIJUAL = "DIJUAL"
    const val DIBATALKAN = "DIBATALKAN"

    fun totalPaket(items: List<Pair<Long, Double>>, price: Long): Long {
        if (items.isEmpty()) return 0L
        val normal = items.sumOf { (harga, qty) -> kotlin.math.round(harga * qty).toLong() }
        return price.coerceAtLeast(0L).coerceAtMost(normal)
    }

    /** Total of a split payment must match the sale total exactly. */
    fun splitValid(total: Long, first: Long, second: Long): Result<Unit> {
        if (first < 0 || second < 0) return Result.failure(IllegalArgumentException("Nominal tidak valid"))
        if (first + second != total) {
            return Result.failure(IllegalArgumentException("Jumlah pembayaran tidak sama dengan total"))
        }
        return Result.success(Unit)
    }

    fun diskonPerBaris(lineTotal: Long, diskon: Long): Long =
        diskon.coerceAtLeast(0L).coerceAtMost(lineTotal)
}
