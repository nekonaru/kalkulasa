package id.kalkulasa.core.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Rupiah currency formatting without decimals, id-ID locale.
 * [NumberFormat] is not thread-safe, so each call uses a fresh instance.
 */
object RupiahFormatter {
    private fun formatter(): NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

    fun format(amount: Long): String = formatter().format(amount)

    /** Format a raw digit string typed by the user, keeping only digits. */
    fun parse(input: String): Long = input.filter { it.isDigit() }.toLongOrNull() ?: 0L

    fun formatPlain(amount: Long): String = formatter().format(amount)
}
