package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FinancialFormulasTest {

    @Test
    fun inventoryValue_sums_before_conversion() {
        // Three items of 0.5 kg at 3.333 each sum to 4999.5, truncated once to 4999.
        assertThat(
            HitungHppUseCase.inventoryValue(
                listOf(0.5 to 3_333L, 0.5 to 3_333L, 0.5 to 3_333L),
            ),
        ).isEqualTo(4_999L)
    }

    @Test
    fun newAverageCost_firstEntry_usesBuyPrice() {
        assertThat(HitungHppUseCase.newAverageCost(0.0, 0L, 10.0, 5_000L)).isEqualTo(5_000L)
    }

    @Test
    fun newAverageCost_weightedAverage_mixesOldAndNew() {
        // 10 pcs @ 5.000 + 4 pcs @ 6.000 = 74.000 / 14 = 5.285 (rounded)
        assertThat(HitungHppUseCase.newAverageCost(10.0, 5_000L, 4.0, 6_000L)).isEqualTo(5_286L)
    }

    @Test
    fun newAverageCost_stockOut_thenRestock_usesCurrentAverage() {
        // remaining 3 pcs @ 5.286 after sales, then 7 pcs @ 7.000
        // (3*5286 + 7*7000) / 10 = 6486 (approx)
        val result = HitungHppUseCase.newAverageCost(3.0, 5_286L, 7.0, 7_000L)
        assertThat(result).isEqualTo(6_486L)
    }

    @Test
    fun newAverageCost_zeroTotalQuantity_returnsBuyPrice() {
        assertThat(HitungHppUseCase.newAverageCost(0.0, 0L, 0.0, 9_000L)).isEqualTo(9_000L)
    }

    @Test
    fun marginPercent_zeroRevenue_returnsZero() {
        assertThat(HitungHppUseCase.marginPercent(0L, 0L)).isEqualTo(0.0)
    }

    @Test
    fun marginPercent_normalCase() {
        assertThat(HitungHppUseCase.marginPercent(250_000L, 1_000_000L)).isEqualTo(25.0)
    }

    @Test
    fun netProfit_subtractsOperatingExpenses() {
        val gross = HitungHppUseCase.grossProfit(1_000_000L, 700_000L)
        assertThat(gross).isEqualTo(300_000L)
        assertThat(HitungHppUseCase.netProfit(gross, 100_000L)).isEqualTo(200_000L)
    }

    @Test
    fun cashBalance_combinesCapitalSalesPurchasesExpensesAndDraws() {
        // modal awal 5.000.000 + penjualan 3.000.000 - pembelian 1.500.000 - listrik 200.000 - prive 100.000
        val balance = HitungHppUseCase.cashBalance(
            capitalIn = 5_000_000L,
            privateDraws = 100_000L,
            salesTotal = 3_000_000L,
            purchaseTotal = 1_500_000L,
            expenseTotal = 200_000L,
        )
        assertThat(balance).isEqualTo(6_200_000L)
    }

    @Test
    fun inventoryValue_sumsStockTimesAverageCost() {
        val value = HitungHppUseCase.inventoryValue(listOf(2.0 to 5_000L, 1.5 to 10_000L))
        assertThat(value).isEqualTo(25_000L)
    }
}
