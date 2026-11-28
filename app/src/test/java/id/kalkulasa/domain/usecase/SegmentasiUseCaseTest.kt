package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SegmentasiUseCaseTest {
    @Test
    fun filter_by_tag_and_spend() {
        assertThat(SegmentasiUseCase.cocok("VIP", 2_000_000L, "VIP", 1_000_000L)).isTrue()
        assertThat(SegmentasiUseCase.cocok("VIP", 500_000L, "VIP", 1_000_000L)).isFalse()
        assertThat(SegmentasiUseCase.cocok(null, 2_000_000L, "", 1_000_000L)).isTrue()
    }

    @Test
    fun broadcast_message_has_business_footer() {
        assertThat(
            SegmentasiUseCase.pesanBroadcast("Warungku", "Siti", "Promo hari ini"),
        ).contains("Warungku")
    }
}
