package id.kalkulasa.domain.usecase

/** Membership tiers derived from lifetime spend. Pure Kotlin, unit-tested. */
object MemberUseCase {
    enum class Tier(val minimal: Long, val diskonPersen: Int) {
        REGULER(0L, 0),
        SILVER(1_000_000L, 5),
        GOLD(5_000_000L, 10),
        PLATINUM(15_000_000L, 15),
    }

    fun tier(totalBelanja: Long): Tier = Tier.entries
        .sortedByDescending { it.minimal }
        .first { totalBelanja >= it.minimal }

    fun diskon(totalBelanja: Long, subtotal: Long): Long {
        val persen = tier(totalBelanja).diskonPersen
        if (persen <= 0 || subtotal <= 0) return 0L
        return (subtotal.toDouble() * persen / 100.0).toLong()
    }
}
