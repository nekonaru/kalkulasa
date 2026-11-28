package id.kalkulasa.domain.usecase

/** Customer segmentation rules. Pure Kotlin, unit-tested. */
object SegmentasiUseCase {

    fun cocok(
        tag: String?,
        tierTotalBelanja: Long,
        tagDiinginkan: String,
        minimalBelanja: Long,
    ): Boolean {
        val tagOk = tagDiinginkan.isBlank() || tag?.equals(tagDiinginkan, ignoreCase = true) == true
        return tagOk && tierTotalBelanja >= minimalBelanja.coerceAtLeast(0L)
    }

    fun pesanBroadcast(namaUsaha: String, namaPelanggan: String, pesan: String): String =
        "$namaPelanggan, $pesan\n\n- $namaUsaha"
}
