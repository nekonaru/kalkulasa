package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PiutangAgingUseCaseTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun buckets_are_calculated_from_reference_date() {
        val now = 100L * day
        assertThat(PiutangAgingUseCase.bucket(now, now)).isEqualTo(PiutangAgingUseCase.Bucket.CURRENT)
        assertThat(PiutangAgingUseCase.bucket(now - 10 * day, now)).isEqualTo(PiutangAgingUseCase.Bucket.D1_30)
        assertThat(PiutangAgingUseCase.bucket(now - 45 * day, now)).isEqualTo(PiutangAgingUseCase.Bucket.D31_60)
        assertThat(PiutangAgingUseCase.bucket(now - 90 * day, now)).isEqualTo(PiutangAgingUseCase.Bucket.OVER_60)
    }

    @Test
    fun items_without_due_date_are_current() {
        val now = 100L * day
        val result = PiutangAgingUseCase.totals(
            listOf(Triple<Long, Long?, Long>(500L, null, now - 90 * day)),
        )
        assertThat(result[PiutangAgingUseCase.Bucket.CURRENT]).isEqualTo(500L)
    }

    @Test
    fun totals_ignore_settled_items_and_group_correctly() {
        val now = 100L * day
        val result = PiutangAgingUseCase.totals(
            listOf(
                Triple<Long, Long?, Long>(100L, now, now),
                Triple<Long, Long?, Long>(200L, now - 10 * day, now),
                Triple<Long, Long?, Long>(0L, now - 90 * day, now),
            ),
        )
        assertThat(result[PiutangAgingUseCase.Bucket.CURRENT]).isEqualTo(100L)
        assertThat(result[PiutangAgingUseCase.Bucket.D1_30]).isEqualTo(200L)
        assertThat(result[PiutangAgingUseCase.Bucket.OVER_60]).isNull()
    }
}
