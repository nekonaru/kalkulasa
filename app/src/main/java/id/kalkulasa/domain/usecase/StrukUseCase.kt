package id.kalkulasa.domain.usecase

import id.kalkulasa.domain.model.SaleLine
import java.text.NumberFormat
import java.util.Locale

/** Builds a plain-text receipt (struk) for sharing via WhatsApp or other apps. */
object StrukUseCase {
    private val rupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun build(
        businessName: String,
        tanggal: String,
        items: List<SaleLine>,
        diskon: Long,
        total: Long,
        metodeBayar: String,
    ): String = buildString {
        appendLine("*$businessName*")
        appendLine(tanggal)
        appendLine()
        items.forEach { line ->
            val qty = if (line.jumlah == line.jumlah.toLong().toDouble()) {
                line.jumlah.toLong().toString()
            } else {
                String.format(Locale.forLanguageTag("id-ID"), "%.2f", line.jumlah)
            }
            appendLine("${line.namaProduk}  ${qty} x ${rupiah.format(line.hargaJual)}")
        }
        appendLine()
        if (diskon > 0) {
            appendLine("Diskon: -${rupiah.format(diskon)}")
        }
        appendLine("*Total: ${rupiah.format(total)}*")
        appendLine("Bayar: ${metodeLabel(metodeBayar)}")
        appendLine()
        append("Terima kasih sudah belanja!")
    }

    private fun metodeLabel(metode: String): String = when (metode) {
        "TUNAI" -> "Tunai"
        "TRANSFER" -> "Transfer"
        "QRIS" -> "QRIS"
        "HUTANG" -> "Hutang (dicatat di buku utang)"
        else -> metode
    }
}
