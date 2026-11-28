package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShiftUseCaseTest {
    @Test
    fun overtime_minutes_only_after_schedule() {
        assertThat(ShiftUseCase.lemburMenit(600, 480)).isEqualTo(120)
        assertThat(ShiftUseCase.lemburMenit(450, 480)).isEqualTo(0)
    }

    @Test
    fun overtime_pay_is_proportional() {
        assertThat(ShiftUseCase.upahLembur(480_000L, 60)).isEqualTo(60_000L)
        assertThat(ShiftUseCase.upahLembur(480_000L, 0)).isEqualTo(0L)
    }
}
