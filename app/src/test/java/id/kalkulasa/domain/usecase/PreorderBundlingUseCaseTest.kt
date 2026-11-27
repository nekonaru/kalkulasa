package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PreorderBundlingUseCaseTest {
    @Test
    fun bundle_price_capped_at_normal_total() {
        assertThat(PreorderBundlingUseCase.totalPaket(listOf(10_000L to 2.0), 15_000L)).isEqualTo(15_000L)
        assertThat(PreorderBundlingUseCase.totalPaket(listOf(10_000L to 2.0), 50_000L)).isEqualTo(20_000L)
    }

    @Test
    fun split_payment_must_match_total() {
        assertThat(PreorderBundlingUseCase.splitValid(100_000L, 60_000L, 40_000L).isSuccess).isTrue()
        assertThat(PreorderBundlingUseCase.splitValid(100_000L, 60_000L, 30_000L).isFailure).isTrue()
    }

    @Test
    fun line_discount_is_capped() {
        assertThat(PreorderBundlingUseCase.diskonPerBaris(50_000L, 20_000L)).isEqualTo(20_000L)
        assertThat(PreorderBundlingUseCase.diskonPerBaris(50_000L, 80_000L)).isEqualTo(50_000L)
    }
}
