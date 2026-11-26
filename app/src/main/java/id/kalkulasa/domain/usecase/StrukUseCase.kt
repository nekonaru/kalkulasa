package id.kalkulasa.domain.usecase

import id.kalkulasa.domain.model.SaleLine
import java.text.NumberFormat
import java.util.Locale

data class ReceiptLabels(
    val diskon: String,
    val total: String,
    val bayar: String,
    val thanks: String,
    val cash: String,
    val transfer: String,
    val qris: String,
    val hutang: String,
)

/** Builds a plain-text receipt (struk) for sharing via WhatsApp or other apps. */
object StrukUseCase {
    fun build(
        businessName: String,
        tanggal: String,
        items: List<SaleLine>,
        diskon: Long,
        total: Long,
        metodeBayar: String,
        labels: ReceiptLabels,
        locale: Locale = Locale.forLanguageTag("id-ID"),
    ): String {
        val rupiah = NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        return buildString {
            appendLine("*$businessName*")
            appendLine(tanggal)
            appendLine()
            items.forEach { line ->
                val qty = if (line.jumlah == line.jumlah.toLong().toDouble()) {
                    line.jumlah.toLong().toString()
                } else {
                    String.format(locale, "%.2f", line.jumlah)
                }
                appendLine("${line.namaProduk}  ${qty} x ${rupiah.format(line.hargaJual)}")
            }
            appendLine()
            if (diskon > 0) {
                appendLine("${labels.diskon} -${rupiah.format(diskon)}")
            }
            appendLine("*${labels.total}: ${rupiah.format(total)}*")
            appendLine("${labels.bayar} ${metodeLabel(metodeBayar, labels)}")
            appendLine()
            append(labels.thanks)
        }
    }

    private fun metodeLabel(metode: String, labels: ReceiptLabels): String = when (metode) {
        "TUNAI" -> labels.cash
        "TRANSFER" -> labels.transfer
        "QRIS" -> labels.qris
        "HUTANG" -> labels.hutang
        else -> metode
    }
}
