package id.kalkulasa.domain.usecase

/** Tax and batch payment rules. Pure Kotlin, unit-tested. */
object PpnBatchUseCase {

    fun ppnKeluaran(totalSebelumPpn: Long, aktif: Boolean): Long {
        if (!aktif || totalSebelumPpn <= 0L) return 0L
        return kotlin.math.round(totalSebelumPpn * 0.11).toLong()
    }

    /** A batch payment must not exceed the sum of selected outstanding debts. */
    fun validasiBatch(totalBayar: Long, sisaList: List<Long>, porsi: List<Long>): Result<Unit> {
        if (porsi.size != sisaList.size) return Result.failure(IllegalArgumentException("Jumlah porsi tidak cocok"))
        if (porsi.any { it < 0L }) return Result.failure(IllegalArgumentException("Porsi tidak valid"))
        if (porsi.indices.any { porsi[it] > sisaList[it] }) {
            return Result.failure(IllegalArgumentException("Porsi melebihi sisa tagihan"))
        }
        if (porsi.sum() != totalBayar) {
            return Result.failure(IllegalArgumentException("Total porsi tidak sama dengan pembayaran"))
        }
        return Result.success(Unit)
    }
}
