package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Struk58UseCaseTest {
    @Test
    fun line_width_is_respected() {
        assertThat(Struk58UseCase.garis().length).isEqualTo(32)
        assertThat(Struk58UseCase.tengah("TOKO").length).isEqualTo(32)
        assertThat(Struk58UseCase.baris("Kopi", "20.000").length).isEqualTo(32)
    }

    @Test
    fun receipt_contains_totals() {
        val struk = Struk58UseCase.build(
            "Warungku", listOf("Kopi" to "20.000"), "20.000", "50.000", "30.000",
        )
        assertThat(struk).contains("TOTAL")
        assertThat(struk).contains("KEMBALI")
    }
}
