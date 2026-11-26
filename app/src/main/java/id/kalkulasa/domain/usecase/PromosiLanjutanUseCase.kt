package id.kalkulasa.domain.usecase

/** Voucher, wholesale price and PPN rules for the cashier. Pure Kotlin, unit-tested. */
object PromosiLanjutanUseCase {

    fun diskonVoucher(tipe: String, nilai: Long, subtotal: Long): Long {
        if (subtotal <= 0L) return 0L
        return when (tipe) {
            "NOMINAL" -> nilai.coerceAtLeast(0L).coerceAtMost(subtotal)
            "PERSEN" -> (subtotal.toDouble() * nilai.coerceAtLeast(0L).coerceAtMost(100L) / 100.0).toLong()
            else -> 0L
        }
    }

    /** Use the wholesale price once the cart quantity reaches the threshold. */
    fun hargaEfektif(hargaJual: Long, hargaGrosir: Long, qty: Double, qtyGrosir: Double): Long {
        if (hargaGrosir <= 0L || qtyGrosir <= 0.0 || qty < qtyGrosir) return hargaJual
        return hargaGrosir
    }

    fun ppn(subtotalSetelahDiskon: Long, aktif: Boolean): Long {
        if (!aktif || subtotalSetelahDiskon <= 0L) return 0L
        return kotlin.math.round(subtotalSetelahDiskon * 0.11).toLong()
    }

    fun validasiVoucher(
        kuota: Int,
        terpakai: Int,
        minimalBelanja: Long,
        subtotal: Long,
        berlakuMulai: Long,
        berlakuSampai: Long,
        now: Long,
    ): Result<Unit> {
        if (terpakai >= kuota) return Result.failure(IllegalArgumentException("Kuota voucher habis"))
        if (now < berlakuMulai || now > berlakuSampai) {
            return Result.failure(IllegalArgumentException("Voucher tidak berlaku"))
        }
        if (subtotal < minimalBelanja) {
            return Result.failure(IllegalArgumentException("Belanja belum mencapai minimum"))
        }
        return Result.success(Unit)
    }
}
