package id.kalkulasa.domain.usecase

import java.util.UUID

/** CSV import for products. Pure Kotlin, unit-tested. No Android dependencies. */
object ImportProdukUseCase {

    data class ImportedProduct(
        val id: String,
        val nama: String,
        val satuan: String,
        val barcode: String?,
        val hargaBeli: Long,
        val hargaJual: Long,
        val stokAwal: Double,
        val stokMinimum: Double,
        val kategori: String?,
    )

    data class RowError(val rowNumber: Int, val reason: String)

    data class ImportResult(val products: List<ImportedProduct>, val errors: List<RowError>)

    private val HEADER_TOKENS = setOf(
        "nama", "name", "product", "produk",
        "satuan", "unit",
        "harga_beli", "hargabeli", "harga beli", "buy", "hpp", "hpp_saat_jual",
        "harga_jual", "hargajual", "harga jual", "price", "harga",
        "stok", "stock", "stok_awal",
        "stok_minimum", "stokmin", "min", "minimum", "min_stock", "minstock",
        "barcode", "kode", "sku",
        "kategori", "category", "cat",
    )

    /** Parse the raw CSV text. Never throws on bad data; collects RowError per row instead. */
    fun parse(rawCsv: String): ImportResult {
        val cleaned = rawCsv.trimStart('\uFEFF')
        val lines = cleaned.split('\n').map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return ImportResult(emptyList(), listOf(RowError(0, "empty file")))

        val delimiter = detectDelimiter(lines.first())
        val products = mutableListOf<ImportedProduct>()
        val errors = mutableListOf<RowError>()

        var startIndex = 0
        val firstCells = splitLine(lines[0], delimiter).map { it.trim().lowercase() }
        if (firstCells.any { it in HEADER_TOKENS }) startIndex = 1

        for (i in startIndex until lines.size) {
            val rowNumber = i + 1 // 1-indexed, matching what the user sees in a text editor
            val cells = splitLine(lines[i], delimiter).map { it.trim() }
            if (cells.all { it.isEmpty() }) continue // skip blank row silently
            val parsed = parseRow(cells, rowNumber)
            parsed.onSuccess { products.add(it) }.onFailure { errors.add(RowError(rowNumber, it.message ?: "invalid row")) }
        }
        return ImportResult(products, errors)
    }

    private fun detectDelimiter(headerLine: String): Char {
        val commas = headerLine.count { it == ',' }
        val semicolons = headerLine.count { it == ';' }
        return if (semicolons > commas) ';' else ','
    }

    private fun splitLine(line: String, delimiter: Char): List<String> {
        val cells = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    cells.add(sb.toString())
                    sb.setLength(0)
                }
                else -> sb.append(c)
            }
            i++
        }
        cells.add(sb.toString())
        return cells
    }

    private fun parseRow(cells: List<String>, rowNumber: Int): Result<ImportedProduct> {
        fun cell(index: Int): String = cells.getOrElse(index) { "" }.trim()

        val nama = cell(0)
        if (nama.isEmpty()) return Result.failure(IllegalArgumentException("nama produk kosong"))

        val satuan = cell(1).ifBlank { "pcs" }
        val hargaBeli = parseRupiah(cell(2)).getOrNull() ?: run {
            if (cell(2).isBlank()) 0L else return Result.failure(IllegalArgumentException("harga beli tidak valid"))
        }
        val hargaJual = parseRupiah(cell(3)).getOrNull() ?: return Result.failure(IllegalArgumentException("harga jual tidak valid"))
        if (hargaJual <= 0) return Result.failure(IllegalArgumentException("harga jual harus lebih dari 0"))
        val stok = parseQuantity(cell(4)).getOrNull() ?: 0.0
        val stokMinimum = parseQuantity(cell(5)).getOrNull() ?: 0.0
        val barcode = cell(6).ifBlank { null }
        val kategori = cell(7).ifBlank { null }

        return Result.success(
            ImportedProduct(
                id = UUID.randomUUID().toString(),
                nama = nama,
                satuan = satuan,
                barcode = barcode,
                hargaBeli = hargaBeli,
                hargaJual = hargaJual,
                stokAwal = stok,
                stokMinimum = stokMinimum,
                kategori = kategori,
            ),
        )
    }

    /** Parse a rupiah amount tolerant of dots as thousand separators and comma decimals. */
    fun parseRupiah(raw: String): Result<Long> {
        val trimmed = raw.trim().removePrefix("Rp").removePrefix("rp").trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("empty"))
        // Remove dots that follow exactly 3 digits (thousand separators like 1.250.000)
        val withoutSeparators = trimmed.replace(Regex("\\.(?=\\d{3})"), "")
        val normalized = withoutSeparators.replace(',', '.')
        return normalized.toBigDecimalOrNull()?.toLong()?.let { Result.success(it) }
            ?: Result.failure(IllegalArgumentException("not a number: $raw"))
    }

    fun parseQuantity(raw: String): Result<Double> = parseRupiah(raw).map { it.toDouble() }

    /** Example CSV with header and two sample rows, ready to share with the user. */
    fun templateCsv(): String = """
        nama,satuan,harga_beli,harga_jual,stok,stok_minimum,barcode,kategori
        Kopi Sachet,pck,2500,4000,48,12,8997891001234,Minuman
        Telur 1kg,kg,22000,28000,10,2,,Sembako
    """.trimIndent()
}
