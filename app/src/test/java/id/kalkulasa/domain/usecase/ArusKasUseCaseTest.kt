package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ArusKasUseCaseTest {
    @Test
    fun cash_flow_sums_all_components() {
        val r = ArusKasUseCase.ringkasan(
            penjualan = 100L, piutangDiterima = 20L, modalMasuk = 10L,
            pembelian = 30L, pengeluaran = 15L, prive = 5L,
            hutangDibayar = 10L, kasbon = 2L,
        )
        assertThat(r.masuk).isEqualTo(130L)
        assertThat(r.keluar).isEqualTo(62L)
        assertThat(r.bersih).isEqualTo(68L)
    }
}
