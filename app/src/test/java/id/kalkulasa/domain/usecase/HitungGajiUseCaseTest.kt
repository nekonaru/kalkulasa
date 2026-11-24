package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import id.kalkulasa.domain.model.Attendance
import id.kalkulasa.domain.model.Employee
import id.kalkulasa.domain.model.Kasbon
import org.junit.Test

class HitungGajiUseCaseTest {

    private val employee = Employee("1", "Siti", "Kasir", null, 100_000L, true)

    private fun absen(status: String, upah: Long = 100_000L) =
        Attendance("a-${status}-${upah}", "1", 0L, status, upah, null)

    @Test
    fun only_present_days_are_paid() {
        assertThat(HitungGajiUseCase.upahUntukStatus(100_000L, HitungGajiUseCase.HADIR)).isEqualTo(100_000L)
        assertThat(HitungGajiUseCase.upahUntukStatus(100_000L, HitungGajiUseCase.IZIN)).isEqualTo(0L)
        assertThat(HitungGajiUseCase.upahUntukStatus(100_000L, HitungGajiUseCase.SAKIT)).isEqualTo(0L)
        assertThat(HitungGajiUseCase.upahUntukStatus(100_000L, HitungGajiUseCase.ALPHA)).isEqualTo(0L)
    }

    @Test
    fun payroll_counts_statuses_and_uses_wage_snapshot() {
        val result = HitungGajiUseCase.hitung(
            employee,
            listOf(absen("HADIR", 100_000), absen("HADIR", 90_000), absen("IZIN"), absen("ALPHA")),
            emptyList(),
            0L,
        )
        assertThat(result.hadir).isEqualTo(2)
        assertThat(result.izin).isEqualTo(1)
        assertThat(result.alpha).isEqualTo(1)
        assertThat(result.totalUpah).isEqualTo(190_000L)
        assertThat(result.dibayar).isEqualTo(190_000L)
    }

    @Test
    fun kasbon_deducts_and_never_goes_negative() {
        val result = HitungGajiUseCase.hitung(
            employee,
            listOf(absen("HADIR")),
            listOf(Kasbon("k1", "1", 40_000L, null, 0L)),
            0L,
        )
        assertThat(result.totalKasbon).isEqualTo(40_000L)
        assertThat(result.dibayar).isEqualTo(60_000L)

        val excessive = HitungGajiUseCase.hitung(
            employee,
            listOf(absen("HADIR")),
            listOf(Kasbon("k2", "1", 250_000L, null, 0L)),
            0L,
        )
        assertThat(excessive.dibayar).isEqualTo(0L)
    }

    @Test
    fun previous_payment_is_deducted() {
        val result = HitungGajiUseCase.hitung(
            employee,
            listOf(absen("HADIR")),
            emptyList(),
            25_000L,
        )
        assertThat(result.dibayar).isEqualTo(75_000L)
    }
}
