package id.kalkulasa.domain.usecase

/** Receivables aging buckets. Pure Kotlin, unit-tested. */
object PiutangAgingUseCase {

    enum class Bucket { CURRENT, D1_30, D31_60, OVER_60 }

    /** Age is measured from the due date, or from the record date when no due date exists. */
    fun bucket(reference: Long, now: Long): Bucket {
        val days = kotlin.math.ceil((now - reference).toDouble() / (24L * 60 * 60 * 1000)).toLong().coerceAtLeast(0L)
        return when {
            days <= 0 -> Bucket.CURRENT
            days <= 30 -> Bucket.D1_30
            days <= 60 -> Bucket.D31_60
            else -> Bucket.OVER_60
        }
    }

    /**
     * Items with no due date are always CURRENT. For dated items, a partial day
     * over the due date already counts as one day.
     */
    fun totals(items: List<Triple<Long, Long?, Long>>): Map<Bucket, Long> {
        val result = mutableMapOf<Bucket, Long>()
        items.forEach { (sisa, due, now) ->
            if (sisa <= 0L) return@forEach
            val bucket = if (due == null) Bucket.CURRENT else bucket(due, now)
            result[bucket] = (result[bucket] ?: 0L) + sisa
        }
        return result
    }
}
