package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KaryawanProUseCaseTest {
    @Test
    fun score_is_capped_and_non_negative() {
        assertThat(KaryawanProUseCase.skor(1_000_000L, 100, 22)).isEqualTo(100.0)
        assertThat(KaryawanProUseCase.skor(-5L, -1, -1)).isEqualTo(0.0)
    }

    @Test
    fun payslip_contains_all_components() {
        val slip = KaryawanProUseCase.slipGaji(1_000_000L, 100_000L, 50_000L, 950_000L)
        assertThat(slip).contains("Upah: 1000000")
        assertThat(slip).contains("Kasbon: -100000")
        assertThat(slip).contains("Lembur: +50000")
    }
}
