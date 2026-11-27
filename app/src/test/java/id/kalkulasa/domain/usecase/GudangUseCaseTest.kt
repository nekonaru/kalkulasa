package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GudangUseCaseTest {
    @Test
    fun reorder_uses_lead_time_and_safety_stock() {
        assertThat(GudangUseCase.saranRestok(2.0, 5, 4.0, 4.0)).isEqualTo(10.0)
        assertThat(GudangUseCase.saranRestok(2.0, 5, 100.0, 4.0)).isEqualTo(0.0)
    }

    @Test
    fun internal_barcode_has_prefix() {
        assertThat(GudangUseCase.barcodeInternal("abcdefgh1234")).isEqualTo("KLK-ABCDEFGH")
    }

    @Test
    fun expiry_window_detects_soon_but_not_past() {
        val now = 1_000_000L
        assertThat(GudangUseCase.mendekatiKedaluwarsa(now + 2 * 86_400_000L, now, 7)).isTrue()
        assertThat(GudangUseCase.mendekatiKedaluwarsa(now - 86_400_000L, now, 7)).isFalse()
        assertThat(GudangUseCase.mendekatiKedaluwarsa(0L, now, 7)).isFalse()
    }
}
