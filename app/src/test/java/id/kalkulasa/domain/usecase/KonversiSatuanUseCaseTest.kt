package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KonversiSatuanUseCaseTest {
    @Test
    fun conversion_round_trip() {
        assertThat(KonversiSatuanUseCase.keSatuanKecil(2.0, 12.0)).isEqualTo(24.0)
        assertThat(KonversiSatuanUseCase.keSatuanBesar(24.0, 12.0)).isEqualTo(2.0)
        assertThat(KonversiSatuanUseCase.keSatuanBesar(10.0, 0.0)).isEqualTo(0.0)
    }

    @Test
    fun display_combines_units() {
        assertThat(KonversiSatuanUseCase.tampil(26.0, 12.0)).isEqualTo("2 dus 2.0 pcs")
        assertThat(KonversiSatuanUseCase.tampil(24.0, 12.0)).isEqualTo("2 dus")
    }
}
