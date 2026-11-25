package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PromosiUseCaseTest {

    @Test
    fun percentage_discount_is_calculated_and_capped() {
        assertThat(PromosiUseCase.diskonPersen(200_000L, 10)).isEqualTo(20_000L)
        assertThat(PromosiUseCase.diskonPersen(50_000L, 150)).isEqualTo(50_000L)
        assertThat(PromosiUseCase.diskonPersen(0L, 10)).isEqualTo(0L)
        assertThat(PromosiUseCase.diskonPersen(100_000L, 0)).isEqualTo(0L)
    }

    @Test
    fun point_value_and_validation() {
        assertThat(PromosiUseCase.nilaiPenukaranPoin(100)).isEqualTo(10_000L)
        assertThat(PromosiUseCase.validasiPenukaran(100L, 100).isSuccess).isTrue()
        assertThat(PromosiUseCase.validasiPenukaran(99L, 100).isFailure).isTrue()
        assertThat(PromosiUseCase.validasiPenukaran(100L, -1).isFailure).isTrue()
    }

    @Test
    fun combined_discount_never_exceeds_subtotal() {
        assertThat(PromosiUseCase.gabungkanDiskon(100_000L, 20_000L, 10_000L)).isEqualTo(30_000L)
        assertThat(PromosiUseCase.gabungkanDiskon(100_000L, 90_000L, 50_000L)).isEqualTo(100_000L)
        assertThat(PromosiUseCase.gabungkanDiskon(100_000L, -5L, -5L)).isEqualTo(0L)
    }
}
