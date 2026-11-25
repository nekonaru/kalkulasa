package id.kalkulasa.domain.usecase

import id.kalkulasa.domain.model.Neraca

/** Financial statement rules. Pure Kotlin, unit-tested. */
object LaporanKeuanganUseCase {

    /**
     * Retained earnings from all completed sales to date. Expenses and cost of
     * goods already include payroll and supplier purchases recorded as expenses.
     */
    fun labaDitahan(omzetKumulatif: Long, hppKumulatif: Long, pengeluaranKumulatif: Long): Long =
        omzetKumulatif - hppKumulatif - pengeluaranKumulatif

    /**
     * Build a simple balance sheet. Asset and liability values are clamped to
     * zero so a corrupt row can never flip the sign of a reported total.
     */
    fun neraca(
        kas: Long,
        piutang: Long,
        persediaan: Long,
        kasbon: Long,
        hutangUsaha: Long,
        modalBersih: Long,
        labaDitahan: Long,
    ): Neraca = Neraca(
        kas = kas,
        piutang = piutang.coerceAtLeast(0L),
        persediaan = persediaan.coerceAtLeast(0L),
        kasbon = kasbon.coerceAtLeast(0L),
        hutangUsaha = hutangUsaha.coerceAtLeast(0L),
        modalBersih = modalBersih,
        labaDitahan = labaDitahan,
    )

    fun persentase(partial: Long, total: Long): Double =
        if (total <= 0L) 0.0 else partial.toDouble() / total.toDouble() * 100.0
}
