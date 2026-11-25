package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LaporanKeuanganUseCaseTest {

    @Test
    fun retained_earnings_are_revenue_minus_costs() {
        assertThat(LaporanKeuanganUseCase.labaDitahan(500_000L, 200_000L, 50_000L)).isEqualTo(250_000L)
        assertThat(LaporanKeuanganUseCase.labaDitahan(100_000L, 150_000L, 0L)).isEqualTo(-50_000L)
    }

    @Test
    fun balance_sheet_net_worth_is_assets_minus_liabilities() {
        val neraca = LaporanKeuanganUseCase.neraca(
            kas = 1_000_000L,
            piutang = 200_000L,
            persediaan = 500_000L,
            kasbon = 50_000L,
            hutangUsaha = 300_000L,
            modalBersih = 1_000_000L,
            labaDitahan = 450_000L,
        )
        assertThat(neraca.aset).isEqualTo(1_750_000L)
        assertThat(neraca.liabilitas).isEqualTo(300_000L)
        assertThat(neraca.ekuitas).isEqualTo(1_450_000L)
    }

    @Test
    fun negative_cash_is_visible_while_other_assets_are_clamped() {
        val neraca = LaporanKeuanganUseCase.neraca(-1L, -2L, -3L, -4L, -5L, 0L, 0L)
        assertThat(neraca.kas).isEqualTo(-1L)
        assertThat(neraca.aset).isEqualTo(-1L)
        assertThat(neraca.liabilitas).isEqualTo(0L)
    }

    @Test
    fun percentage_handles_zero_total() {
        assertThat(LaporanKeuanganUseCase.persentase(50L, 0L)).isEqualTo(0.0)
        assertThat(LaporanKeuanganUseCase.persentase(25L, 100L)).isEqualTo(25.0)
    }
}
