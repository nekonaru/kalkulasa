package id.kalkulasa.domain.usecase

/** Plain-text receipt formatted for a 58 mm thermal printer (32 characters wide). */
object Struk58UseCase {

    const val LEBAR = 32

    fun garis(): String = "-".repeat(LEBAR)

    fun tengah(text: String): String {
        val value = text.take(LEBAR)
        val sisa = LEBAR - value.length
        val kiri = sisa / 2
        return " ".repeat(kiri) + value + " ".repeat(sisa - kiri)
    }

    fun baris(kiri: String, kanan: String): String {
        val sisa = LEBAR - kiri.length - kanan.length
        return if (sisa <= 0) {
            kiri.take(LEBAR)
        } else {
            kiri + " ".repeat(sisa) + kanan
        }
    }

    fun build(
        namaUsaha: String,
        items: List<Pair<String, String>>,
        total: String,
        bayar: String,
        kembalian: String,
    ): String = buildString {
        appendLine(tengah(namaUsaha.take(LEBAR)))
        appendLine(garis())
        items.forEach { (nama, nilai) -> appendLine(baris(nama.take(20), nilai.take(10))) }
        appendLine(garis())
        appendLine(baris("TOTAL", total))
        appendLine(baris("BAYAR", bayar))
        appendLine(baris("KEMBALI", kembalian))
    }
}
