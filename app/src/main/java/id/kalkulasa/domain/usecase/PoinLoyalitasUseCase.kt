package id.kalkulasa.domain.usecase

/** Loyalty points: 1 point per 1.000 rupiah of the discounted total. Pure Kotlin, unit-tested. */
object PoinLoyalitasUseCase {
    fun pointsFor(total: Long): Long = (total / 1000L).coerceAtLeast(0L)
}
