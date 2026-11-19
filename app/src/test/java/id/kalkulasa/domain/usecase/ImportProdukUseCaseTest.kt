package id.kalkulasa.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImportProdukUseCaseTest {

    private fun parse(csv: String) = ImportProdukUseCase.parse(csv)

    @Test
    fun template_parses_cleanly() {
        val result = parse(ImportProdukUseCase.templateCsv())
        assertThat(result.products).hasSize(2)
        assertThat(result.errors).isEmpty()
        assertThat(result.products[0].nama).isEqualTo("Kopi Sachet")
        assertThat(result.products[0].hargaJual).isEqualTo(4000L)
        assertThat(result.products[0].stokAwal).isEqualTo(48.0)
        assertThat(result.products[1].kategori).isEqualTo("Sembako")
    }

    @Test
    fun thousand_separator_and_no_header() {
        val result = parse("Telur 1kg,kg,22000,28000,10,2\n" +
            "Beras 5kg,sak,65000,78000,5,1\n")
        assertThat(result.products).hasSize(2)
        assertThat(result.products[0].nama).isEqualTo("Telur 1kg")
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun indonesian_thousand_separator() {
        val result = parseRupiahSafe("1.250.000")
        assertThat(result).isEqualTo(1250000L)
        assertThat(parseRupiahSafe("Rp 1.250.000")).isEqualTo(1250000L)
        assertThat(parseRupiahSafe("28000")).isEqualTo(28000L)
        assertThat(parseRupiahSafe("1.500,50")).isEqualTo(1500L) // decimal comma kept: 1.500,50 = 1500,50 -> 1500 as long
    }

    @Test
    fun row_with_missing_name_generates_error() {
        val result = parse(",,22000,28000,10,2\n")
        assertThat(result.products).isEmpty()
        assertThat(result.errors).hasSize(1)
        assertThat(result.errors[0].rowNumber).isEqualTo(1)
    }

    @Test
    fun row_with_zero_price_generates_error() {
        val result = parse("Telur,kg,22000,0,10,2\n")
        assertThat(result.products).isEmpty()
        assertThat(result.errors).hasSize(1)
    }

    @Test
    fun valid_and_invalid_rows_mixed() {
        val result = parse(
            "nama,satuan,harga_beli,harga_jual\n" +
                "Kopi,pck,2500,4000\n" +
                "\n" +
                "Rusak,pck,2500,ini bukan angka\n",
        )
        assertThat(result.products).hasSize(1)
        assertThat(result.errors).hasSize(1)
        assertThat(result.errors[0].rowNumber).isEqualTo(3)
    }

    @Test
    fun semicolon_delimited_file_works() {
        val result = parse("nama;satuan;harga_beli;harga_jual;stok\nKopi;pck;2500;4000;48\n")
        assertThat(result.products).hasSize(1)
        assertThat(result.products[0].satuan).isEqualTo("pck")
    }

    @Test
    fun quoted_cell_with_embedded_comma() {
        val result = parse("\"Telur, Ayam\",kg,22000,28000,10,2\n")
        assertThat(result.products).hasSize(1)
        assertThat(result.products[0].nama).isEqualTo("Telur, Ayam")
    }

    @Test
    fun bom_is_handled() {
        val result = parse("\uFEFFnama,satuan,harga_beli,harga_jual\nKopi,pck,2500,4000\n")
        assertThat(result.products).hasSize(1)
        assertThat(result.errors).isEmpty()
    }

    private fun parseRupiahSafe(raw: String): Long =
        ImportProdukUseCase.parseRupiah(raw).getOrThrow()
}
