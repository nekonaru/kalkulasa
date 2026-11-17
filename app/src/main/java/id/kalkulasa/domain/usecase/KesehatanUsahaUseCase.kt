package id.kalkulasa.domain.usecase

/**
 * Skor Kesehatan Usaha (0-100) from four signals an owner actually cares about.
 * Pure Kotlin, unit-tested. Display strings live in the UI layer.
 *
 * Components:
 * - Profitability, weight 30: margin of the period
 * - Profit direction, weight 20: is net profit positive
 * - Inventory health, weight 15: inventory value vs period revenue
 * - Receivables discipline, weight 20: outstanding debt vs period revenue
 * - Cash position, weight 15: cash balance sign and size
 */
object KesehatanUsahaUseCase {

    data class Input(
        val marginPersen: Double,
        val labaBersih: Long,
        val omzet: Long,
        val nilaiPersediaan: Long,
        val piutangOutstanding: Long,
        val saldoKas: Long,
    )

    data class Result(
        val score: Int,
        val verdict: Verdict,
        val suggestions: List<Suggestion>,
    )

    enum class Verdict { PRIMA, SEHAT, CUKUP, PERLU_PERHATIAN }

    enum class Suggestion {
        PERBAIKI_MARGIN,
        KURANGI_PENGELUARAN,
        PUTAR_STOK,
        TAGIH_PIUTANG,
        ISI_KAS,
        BAGUS,
    }

    fun evaluate(input: Input): Result {
        var score = 0

        // Profitability (30)
        score += when {
            input.marginPersen >= 20.0 -> 30
            input.marginPersen >= 10.0 -> 20
            input.marginPersen > 0.0 -> 10
            else -> 0
        }

        // Profit direction (20)
        if (input.labaBersih > 0) score += 20

        // Inventory health (15): healthy when inventory is a small multiple of period revenue
        score += if (input.omzet <= 0) {
            10 // neutral when no sales yet
        } else {
            val ratio = input.nilaiPersediaan.toDouble() / input.omzet.toDouble()
            when {
                ratio <= 3.0 -> 15
                ratio <= 6.0 -> 10
                else -> 5
            }
        }

        // Receivables discipline (20)
        score += when {
            input.piutangOutstanding <= 0L -> 20
            input.omzet > 0 && input.piutangOutstanding.toDouble() / input.omzet <= 0.3 -> 15
            input.omzet > 0 && input.piutangOutstanding.toDouble() / input.omzet <= 0.6 -> 8
            input.omzet <= 0L -> 8
            else -> 0
        }

        // Cash position (15)
        score += when {
            input.saldoKas >= 2_000_000L -> 15
            input.saldoKas > 0L -> 10
            else -> 0
        }

        score = score.coerceIn(0, 100)

        val verdict = when {
            score >= 85 -> Verdict.PRIMA
            score >= 70 -> Verdict.SEHAT
            score >= 40 -> Verdict.CUKUP
            else -> Verdict.PERLU_PERHATIAN
        }

        val weak = mutableListOf<Suggestion>()
        if (input.marginPersen < 10.0) weak += Suggestion.PERBAIKI_MARGIN
        if (input.labaBersih <= 0) weak += Suggestion.KURANGI_PENGELUARAN
        if (input.omzet > 0 && input.nilaiPersediaan.toDouble() / input.omzet > 6.0) weak += Suggestion.PUTAR_STOK
        if (input.piutangOutstanding > 0) weak += Suggestion.TAGIH_PIUTANG
        if (input.saldoKas <= 0) weak += Suggestion.ISI_KAS
        val suggestions = if (weak.isEmpty()) listOf(Suggestion.BAGUS) else weak.distinct().take(3)

        return Result(score = score, verdict = verdict, suggestions = suggestions)
    }
}
