package id.kalkulasa.domain.usecase

/** Quantity conversion between base and packaging units. Pure Kotlin, unit-tested. */
object KonversiSatuanUseCase {

    fun keSatuanKecil(dus: Double, isiPerDus: Double): Double =
        dus.coerceAtLeast(0.0) * isiPerDus.coerceAtLeast(0.0)

    fun keSatuanBesar(pcs: Double, isiPerDus: Double): Double {
        val isi = isiPerDus.coerceAtLeast(0.0)
        return if (isi <= 0.0) 0.0 else pcs.coerceAtLeast(0.0) / isi
    }

    fun tampil(pcs: Double, isiPerDus: Double): String {
        val isi = isiPerDus.coerceAtLeast(0.0)
        if (isi <= 0.0) return "$pcs pcs"
        val dus = (pcs / isi).toLong()
        val sisa = pcs - dus * isi
        return if (sisa <= 0.0) "$dus dus" else "$dus dus $sisa pcs"
    }
}
