package id.kalkulasa.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date and quantity helpers shared across features.
 * SimpleDateFormat instances are created per call because they are not thread-safe.
 */
object DateFormatter {
    private fun dayMonthFormat() = SimpleDateFormat("d MMM", Locale.forLanguageTag("id-ID"))
    private fun dayMonthYearFormat() = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
    private fun dateTimeFormat() = SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"))
    private fun fileStampFormat() = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)

    fun formatDayMonth(millis: Long): String = dayMonthFormat().format(Date(millis))
    fun formatDayMonthYear(millis: Long): String = dayMonthYearFormat().format(Date(millis))
    fun formatDateTime(millis: Long): String = dateTimeFormat().format(Date(millis))
    fun fileStamp(millis: Long): String = fileStampFormat().format(Date(millis))

    fun startOfDay(now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun endOfDay(now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

    /** Start of day N days ago (0 = today). Calendar.add keeps this correct across DST. */
    fun startOfDayDaysAgo(daysAgo: Int, now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = startOfDay(now)
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }.timeInMillis

    fun startOfMonth(now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Quantity display: drops the decimal part when whole. */
    fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString()
        else String.format(Locale.forLanguageTag("id-ID"), "%.2f", qty)
}
