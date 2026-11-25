package id.kalkulasa.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RupiahFormatterTest {

    @Test
    fun parse_accepts_digits_and_returns_success() {
        assertThat(RupiahFormatter.parse("12345").getOrThrow()).isEqualTo(12_345L)
        assertThat(RupiahFormatter.parse("Rp 12.000").getOrThrow()).isEqualTo(12_000L)
        assertThat(RupiahFormatter.parse("").getOrThrow()).isEqualTo(0L)
    }

    @Test
    fun parse_rejects_oversized_input_instead_of_zero() {
        val huge = "9".repeat(25)
        assertThat(RupiahFormatter.parse(huge).isFailure).isTrue()
        assertThat(RupiahFormatter.parseOrZero(huge)).isEqualTo(0L)
    }
}
