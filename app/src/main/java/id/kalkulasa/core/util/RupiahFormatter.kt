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

    /**
     * Parse a raw digit string typed by the user, keeping only digits.
     * Returns a failure instead of silently turning an oversized number into 0.
     */
    fun parse(input: String): Result<Long> {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return Result.success(0L)
        val value = digits.toLongOrNull()
            ?: return Result.failure(IllegalArgumentException("Angka terlalu besar"))
        return Result.success(value)
    }

    /** Convenience for callers that must show a number even when input is invalid. */
    fun parseOrZero(input: String): Long = parse(input).getOrDefault(0L)

    fun formatPlain(amount: Long): String = formatter().format(amount)
}
