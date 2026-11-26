package id.kalkulasa.domain.usecase

/** Receivables aging buckets. Pure Kotlin, unit-tested. */
object PiutangAgingUseCase {

    enum class Bucket { CURRENT, D1_30, D31_60, OVER_60 }

    /** Age is measured from the due date, or from the record date when no due date exists. */
    fun bucket(reference: Long, now: Long): Bucket {
        val days = (now - reference) / (24L * 60 * 60 * 1000)
        return when {
            days <= 0 -> Bucket.CURRENT
            days <= 30 -> Bucket.D1_30
            days <= 60 -> Bucket.D31_60
            else -> Bucket.OVER_60
        }
    }

    fun totals(items: List<Triple<Long, Long, Long>>): Map<Bucket, Long> {
        val result = mutableMapOf<Bucket, Long>()
        items.forEach { (sisa, reference, now) ->
            if (sisa <= 0L) return@forEach
            val bucket = bucket(reference, now)
            result[bucket] = (result[bucket] ?: 0L) + sisa
        }
        return result
    }
}
