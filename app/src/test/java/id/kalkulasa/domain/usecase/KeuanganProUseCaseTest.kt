package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KeuanganProUseCaseTest {
    @Test
    fun recurring_day_is_clamped_to_month_length() {
        assertThat(KeuanganProUseCase.jatuhBulanIni(31, 30)).isEqualTo(30)
        assertThat(KeuanganProUseCase.jatuhBulanIni(5, 30)).isEqualTo(5)
    }

    @Test
    fun profit_share_never_exceeds_profit() {
        assertThat(KeuanganProUseCase.bagianLaba(1_000_000L, 10)).isEqualTo(100_000L)
        assertThat(KeuanganProUseCase.totalBagian(1_000_000L, listOf(40, 40))).isEqualTo(800_000L)
        assertThat(KeuanganProUseCase.bagianLaba(-5_000L, 10)).isEqualTo(0L)
    }
}
