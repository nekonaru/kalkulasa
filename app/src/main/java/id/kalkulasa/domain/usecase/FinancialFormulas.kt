package id.kalkulasa.domain.usecase

import java.math.BigDecimal
import java.math.RoundingMode

/** Weighted-average cost (HPP rata-rata tertimbang). Pure Kotlin, unit-tested. */
object HitungHppUseCase {
    /**
     * New average cost after a stock-in batch.
     * newHpp = (oldStock * oldHpp + qty * buyPrice) / (oldStock + qty)
     */
    fun newAverageCost(oldStock: Double, oldHpp: Long, qtyIn: Double, buyPrice: Long): Long {
        val totalQty = oldStock + qtyIn
        if (totalQty <= 0.0) return buyPrice
        val totalValue = BigDecimal(oldStock).multiply(BigDecimal(oldHpp))
            .add(BigDecimal(qtyIn).multiply(BigDecimal(buyPrice)))
        return totalValue.divide(BigDecimal(totalQty), 0, RoundingMode.HALF_UP).toLong()
    }

    fun grossProfit(omzet: Long, cogs: Long): Long = omzet - cogs

    fun netProfit(grossProfit: Long, operatingExpenses: Long): Long = grossProfit - operatingExpenses

    /** Margin as a percentage of revenue. Returns 0 when revenue is zero. */
    fun marginPercent(netProfit: Long, omzet: Long): Double =
        if (omzet <= 0L) 0.0 else netProfit.toDouble() / omzet.toDouble() * 100.0

    /** Sum first, then convert once, matching the SQL aggregate used by the DAO. */
    fun inventoryValue(items: List<Pair<Double, Long>>): Long =
        items.fold(0.0) { acc, (stok, hpp) -> acc + stok * hpp }.toLong()

    /**
     * Cash balance = capital (initial + added) - private draws
     *              + sales takings - stock purchases - operating expenses
     */
    fun cashBalance(
        capitalIn: Long,
        privateDraws: Long,
        salesTotal: Long,
        purchaseTotal: Long,
        expenseTotal: Long,
    ): Long = capitalIn - privateDraws + salesTotal - purchaseTotal - expenseTotal
}
