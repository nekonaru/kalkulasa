package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UtangUseCaseTest {

    @Test
    fun sisa_never_negative() {
        assertThat(UtangUseCase.sisa(100_000L, 40_000L)).isEqualTo(60_000L)
        assertThat(UtangUseCase.sisa(100_000L, 150_000L)).isEqualTo(0L)
    }

    @Test
    fun validatePayment_partial_succeeds() {
        val result = UtangUseCase.validatePayment(100_000L, 40_000L, 25_000L)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(65_000L)
    }

    @Test
    fun validatePayment_exact_remaining_succeeds() {
        val result = UtangUseCase.validatePayment(100_000L, 40_000L, 60_000L)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(100_000L)
    }

    @Test
    fun validatePayment_over_payment_rejected() {
        val result = UtangUseCase.validatePayment(100_000L, 40_000L, 60_001L)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun validatePayment_zero_rejected() {
        assertThat(UtangUseCase.validatePayment(100_000L, 0L, 0L).isFailure).isTrue()
        assertThat(UtangUseCase.validatePayment(100_000L, 0L, -5_000L).isFailure).isTrue()
    }

    @Test
    fun isLunas_true_at_exact_payment() {
        assertThat(UtangUseCase.isLunas(100_000L, 100_000L)).isTrue()
        assertThat(UtangUseCase.isLunas(100_000L, 99_999L)).isFalse()
    }
}
