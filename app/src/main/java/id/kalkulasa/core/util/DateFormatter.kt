package id.kalkulasa.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Date and quantity helpers shared across features. */
object DateFormatter {
    private val dayMonth = SimpleDateFormat("d MMM", Locale.forLanguageTag("id-ID"))
    private val dayMonthYear = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
    private val dateTime = SimpleDateFormat("d MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"))
    private val fileStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)

    fun formatDayMonth(millis: Long): String = dayMonth.format(Date(millis))
    fun formatDayMonthYear(millis: Long): String = dayMonthYear.format(Date(millis))
    fun formatDateTime(millis: Long): String = dateTime.format(Date(millis))
    fun fileStamp(millis: Long): String = fileStamp.format(Date(millis))

    fun startOfDay(now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun endOfDay(now: Long = System.currentTimeMillis()): Long = startOfDay(now) + 24L * 60 * 60 * 1000 - 1

    /** Start of day N days ago (0 = today). */
    fun startOfDayDaysAgo(daysAgo: Int, now: Long = System.currentTimeMillis()): Long =
        startOfDay(now) - daysAgo.toLong() * 24 * 60 * 60 * 1000

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
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else String.format(Locale.forLanguageTag("id-ID"), "%.2f", qty)
}
