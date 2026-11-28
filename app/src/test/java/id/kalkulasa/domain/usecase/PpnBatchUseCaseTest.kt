package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PpnBatchUseCaseTest {
    @Test
    fun vat_output_is_eleven_percent() {
        assertThat(PpnBatchUseCase.ppnKeluaran(1_000_000L, true)).isEqualTo(110_000L)
        assertThat(PpnBatchUseCase.ppnKeluaran(1_000_000L, false)).isEqualTo(0L)
    }

    @Test
    fun batch_payment_validation() {
        assertThat(
            PpnBatchUseCase.validasiBatch(100L, listOf(60L, 80L), listOf(40L, 60L)).isSuccess,
        ).isTrue()
        assertThat(
            PpnBatchUseCase.validasiBatch(100L, listOf(60L, 80L), listOf(70L, 30L)).isFailure,
        ).isTrue()
    }
}
