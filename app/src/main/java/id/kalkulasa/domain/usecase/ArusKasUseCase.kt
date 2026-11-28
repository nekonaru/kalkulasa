package id.kalkulasa.domain.usecase

/** Cash flow statement rules. Pure Kotlin, unit-tested. */
object ArusKasUseCase {

    data class Ringkasan(val masuk: Long, val keluar: Long, val bersih: Long)

    fun ringkasan(
        penjualan: Long,
        piutangDiterima: Long,
        modalMasuk: Long,
        pembelian: Long,
        pengeluaran: Long,
        prive: Long,
        hutangDibayar: Long,
        kasbon: Long,
    ): Ringkasan {
        val masuk = penjualan + piutangDiterima + modalMasuk
        val keluar = pembelian + pengeluaran + prive + hutangDibayar + kasbon
        return Ringkasan(masuk, keluar, masuk - keluar)
    }
}
