package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KesehatanUsahaUseCaseTest {

    private fun input(
        marginPersen: Double = 25.0,
        labaBersih: Long = 1_000_000L,
        omzet: Long = 5_000_000L,
        nilaiPersediaan: Long = 4_000_000L,
        piutangOutstanding: Long = 0L,
        saldoKas: Long = 3_000_000L,
    ) = KesehatanUsahaUseCase.Input(marginPersen, labaBersih, omzet, nilaiPersediaan, piutangOutstanding, saldoKas)

    @Test
    fun healthy_business_scores_prima_or_sehat() {
        val result = KesehatanUsahaUseCase.evaluate(input())
        assertThat(result.score).isAtLeast(85)
        assertThat(result.verdict).isEqualTo(KesehatanUsahaUseCase.Verdict.PRIMA)
        assertThat(result.suggestions).containsExactly(KesehatanUsahaUseCase.Suggestion.BAGUS)
    }

    @Test
    fun loss_making_business_needs_attention() {
        val result = KesehatanUsahaUseCase.evaluate(
            input(marginPersen = -5.0, labaBersih = -500_000L, saldoKas = -100_000L),
        )
        assertThat(result.score).isLessThan(40)
        assertThat(result.verdict).isEqualTo(KesehatanUsahaUseCase.Verdict.PERLU_PERHATIAN)
        assertThat(result.suggestions).contains(KesehatanUsahaUseCase.Suggestion.KURANGI_PENGELUARAN)
    }

    @Test
    fun margin_below_10_percent_pulls_score_down() {
        val good = KesehatanUsahaUseCase.evaluate(input(marginPersen = 25.0)).score
        val weak = KesehatanUsahaUseCase.evaluate(input(marginPersen = 5.0)).score
        assertThat(good - weak).isAtLeast(20)
    }

    @Test
    fun outstanding_receivables_reduce_score() {
        val noDebt = KesehatanUsahaUseCase.evaluate(input(piutangOutstanding = 0L)).score
        val heavyDebt = KesehatanUsahaUseCase.evaluate(input(piutangOutstanding = 3_500_000L)).score
        assertThat(noDebt - heavyDebt).isAtLeast(12)
        assertThat(KesehatanUsahaUseCase.evaluate(input(piutangOutstanding = 3_500_000L)).suggestions)
            .contains(KesehatanUsahaUseCase.Suggestion.TAGIH_PIUTANG)
    }

    @Test
    fun zero_revenue_is_neutral_not_crashing() {
        val result = KesehatanUsahaUseCase.evaluate(
            input(omzet = 0L, labaBersih = 0L, nilaiPersediaan = 0L),
        )
        assertThat(result.score).isAtLeast(0)
        assertThat(result.score).isAtMost(100)
    }

    @Test
    fun score_always_within_0_and_100() {
        val best = KesehatanUsahaUseCase.evaluate(
            input(marginPersen = 50.0, labaBersih = 9_999_999L, saldoKas = 999_999_999L),
        )
        assertThat(best.score).isAtMost(100)
        val worst = KesehatanUsahaUseCase.evaluate(
            input(
                marginPersen = -100.0,
                labaBersih = -9_999_999L,
                nilaiPersediaan = 999_999_999L,
                piutangOutstanding = 999_999_999L,
                saldoKas = -9_999_999L,
            ),
        )
        assertThat(worst.score).isAtLeast(0)
    }

    @Test
    fun stock_piling_up_triggers_stock_suggestion() {
        // inventory >> revenue
        val result = KesehatanUsahaUseCase.evaluate(
            input(omzet = 1_000_000L, nilaiPersediaan = 10_000_000L),
        )
        assertThat(result.suggestions).contains(KesehatanUsahaUseCase.Suggestion.PUTAR_STOK)
    }
}
