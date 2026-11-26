package id.kalkulasa.domain.usecase

/** Customer deposit rules. Pure Kotlin, unit-tested. */
object DepositUseCase {
    fun sisa(nominal: Long, terpakai: Long): Long = (nominal - terpakai).coerceAtLeast(0L)

    fun totalSisa(deposits: List<Pair<Long, Long>>): Long =
        deposits.sumOf { sisa(it.first, it.second) }

    fun validasiPenarikan(sisaTersedia: Long, jumlah: Long): Result<Unit> {
        if (jumlah <= 0) return Result.failure(IllegalArgumentException("Nominal tidak valid"))
        if (jumlah > sisaTersedia) return Result.failure(IllegalArgumentException("Saldo deposit tidak cukup"))
        return Result.success(Unit)
    }
}
