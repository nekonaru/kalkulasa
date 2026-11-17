package id.kalkulasa.domain.usecase

/** Debt (piutang) rules. Pure Kotlin, unit-tested. */
object UtangUseCase {
    /** Remaining debt after partial payments. */
    fun sisa(nominal: Long, terbayar: Long): Long = (nominal - terbayar).coerceAtLeast(0L)

    fun isLunas(nominal: Long, terbayar: Long): Boolean = terbayar >= nominal

    /**
     * Validate a payment against the outstanding debt.
     * Returns the new total paid on success; fails on non-positive or over-payment.
     */
    fun validatePayment(nominal: Long, terbayar: Long, payment: Long): Result<Long> {
        if (payment <= 0) return Result.failure(IllegalArgumentException("Nominal bayar harus lebih dari 0"))
        if (terbayar + payment > nominal) {
            return Result.failure(IllegalArgumentException("Pembayaran melebihi tagihan (sisa ${sisa(nominal, terbayar)})"))
        }
        return Result.success(terbayar + payment)
    }
}
