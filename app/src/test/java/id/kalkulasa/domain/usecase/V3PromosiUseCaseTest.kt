package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MemberUseCaseTest {
    @Test
    fun tiers_follow_lifetime_spend() {
        assertThat(MemberUseCase.tier(0L)).isEqualTo(MemberUseCase.Tier.REGULER)
        assertThat(MemberUseCase.tier(1_000_000L)).isEqualTo(MemberUseCase.Tier.SILVER)
        assertThat(MemberUseCase.tier(5_000_000L)).isEqualTo(MemberUseCase.Tier.GOLD)
        assertThat(MemberUseCase.tier(15_000_000L)).isEqualTo(MemberUseCase.Tier.PLATINUM)
    }

    @Test
    fun tier_discount_is_capped() {
        assertThat(MemberUseCase.diskon(5_000_000L, 200_000L)).isEqualTo(20_000L)
        assertThat(MemberUseCase.diskon(0L, 200_000L)).isEqualTo(0L)
    }
}

class PromosiLanjutanUseCaseTest {
    @Test
    fun voucher_discount_respects_type_and_cap() {
        assertThat(PromosiLanjutanUseCase.diskonVoucher("NOMINAL", 20_000L, 10_000L)).isEqualTo(10_000L)
        assertThat(PromosiLanjutanUseCase.diskonVoucher("PERSEN", 10L, 200_000L)).isEqualTo(20_000L)
        assertThat(PromosiLanjutanUseCase.diskonVoucher("PERSEN", 150L, 200_000L)).isEqualTo(200_000L)
    }

    @Test
    fun wholesale_price_applies_at_threshold() {
        assertThat(PromosiLanjutanUseCase.hargaEfektif(10_000L, 8_000L, 11.0, 10.0)).isEqualTo(8_000L)
        assertThat(PromosiLanjutanUseCase.hargaEfektif(10_000L, 8_000L, 9.0, 10.0)).isEqualTo(10_000L)
    }

    @Test
    fun ppn_is_eleven_percent() {
        assertThat(PromosiLanjutanUseCase.ppn(100_000L, true)).isEqualTo(11_000L)
        assertThat(PromosiLanjutanUseCase.ppn(100_000L, false)).isEqualTo(0L)
    }

    @Test
    fun voucher_validation_rejects_expired_and_insufficient() {
        assertThat(
            PromosiLanjutanUseCase.validasiVoucher(5, 5, 0L, 10_000L, 0L, 100L, 50L).isFailure,
        ).isTrue()
        assertThat(
            PromosiLanjutanUseCase.validasiVoucher(5, 0, 20_000L, 10_000L, 0L, 100L, 50L).isFailure,
        ).isTrue()
        assertThat(
            PromosiLanjutanUseCase.validasiVoucher(5, 0, 0L, 10_000L, 0L, 100L, 50L).isSuccess,
        ).isTrue()
    }
}

class DepositUseCaseTest {
    @Test
    fun deposit_balance_and_withdrawal() {
        assertThat(DepositUseCase.totalSisa(listOf(100_000L to 40_000L, 50_000L to 0L))).isEqualTo(110_000L)
        assertThat(DepositUseCase.validasiPenarikan(50_000L, 60_000L).isFailure).isTrue()
        assertThat(DepositUseCase.validasiPenarikan(50_000L, 20_000L).isSuccess).isTrue()
    }
}
