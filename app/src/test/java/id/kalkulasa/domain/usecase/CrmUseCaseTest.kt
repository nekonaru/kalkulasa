package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CrmUseCaseTest {
    @Test
    fun rfm_band_is_between_one_and_five() {
        assertThat(CrmUseCase.skorRfm(0, 0L)).isEqualTo(1)
        assertThat(CrmUseCase.skorRfm(100, 100_000_000L)).isEqualTo(5)
    }

    @Test
    fun birthday_without_date_returns_null() {
        assertThat(CrmUseCase.hariMenujuUlangTahun(0L, System.currentTimeMillis())).isNull()
    }
}
