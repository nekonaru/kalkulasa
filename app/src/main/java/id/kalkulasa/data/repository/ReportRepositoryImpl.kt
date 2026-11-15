package id.kalkulasa.data.repository

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.data.local.KalkulasaDatabase
import id.kalkulasa.domain.model.PeriodReport
import id.kalkulasa.domain.repository.ReportRepository
import id.kalkulasa.domain.usecase.HitungHppUseCase
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val db: KalkulasaDatabase,
) : ReportRepository {

    override suspend fun reportBetween(from: Long, to: Long): PeriodReport {
        val omzet = db.saleDao().sumRevenueBetween(from, to)
        val hpp = db.saleDao().sumCogsBetween(from, to)
        val pengeluaran = db.expenseDao().sumBetween(from, to)
        val labaKotor = HitungHppUseCase.grossProfit(omzet, hpp)
        val labaBersih = HitungHppUseCase.netProfit(labaKotor, pengeluaran)
        val jumlahTransaksi = db.saleDao().countBetween(from, to)
        val inventoryValue = db.productDao().getAllActive()
            .sumOf { (it.stok * it.hargaBeliRata2).toLong() }
        return PeriodReport(
            omzet = omzet,
            hpp = hpp,
            pengeluaran = pengeluaran,
            labaKotor = labaKotor,
            labaBersih = labaBersih,
            marginPersen = HitungHppUseCase.marginPercent(labaBersih, omzet),
            jumlahTransaksi = jumlahTransaksi,
            nilaiPersediaan = inventoryValue,
        )
    }

    override suspend fun exportCsv(from: Long, to: Long): String {
        val sales = db.saleDao().getBetween(from, to)
        val builder = StringBuilder()
        builder.append("Waktu,Status,Metode bayar,Subtotal,Diskon,Total\n")
        sales.forEach { sale ->
            builder.append(DateFormatter.formatDateTime(sale.waktu)).append(',')
                .append(sale.status).append(',')
                .append(sale.metodeBayar).append(',')
                .append(sale.subtotal).append(',')
                .append(sale.diskon).append(',')
                .append(sale.total).append('\n')
        }
        return builder.toString()
    }

    override suspend fun exportPdfBytes(from: Long, to: Long): ByteArray {
        val profile = db.businessProfileDao().getProfile()
        val report = reportBetween(from, to)
        val topProducts = db.saleDao().topProductsBetween(from, to, 5)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val title = Paint().apply { textSize = 22f; isFakeBoldText = true; color = 0xFF0F4C5C.toInt() }
        val heading = Paint().apply { textSize = 14f; isFakeBoldText = true; color = 0xFF0F4C5C.toInt() }
        val body = Paint().apply { textSize = 12f; color = 0xFF0A2A33.toInt() }
        val muted = Paint().apply { textSize = 10f; color = 0xFF5A6B70.toInt() }

        var y = 60f
        canvas.drawText("Kalkulasa", 40f, y, title)
        y += 24f
        canvas.drawText(profile?.namaUsaha ?: "Usaha", 40f, y, body)
        y += 18f
        canvas.drawText(
            "Periode ${DateFormatter.formatDayMonthYear(from)} - ${DateFormatter.formatDayMonthYear(to)}",
            40f, y, muted,
        )
        y += 34f
        canvas.drawText("Laba rugi sederhana", 40f, y, heading)
        y += 24f
        val rows = listOf(
            "Omzet" to RupiahFormatter.format(report.omzet),
            "Modal per barang terjual" to RupiahFormatter.format(report.hpp),
            "Laba kotor" to RupiahFormatter.format(report.labaKotor),
            "Pengeluaran operasional" to RupiahFormatter.format(report.pengeluaran),
            "Untung bersih" to RupiahFormatter.format(report.labaBersih),
            "Margin" to String.format(java.util.Locale("in", "ID"), "%.1f%%", report.marginPersen),
            "Jumlah transaksi" to report.jumlahTransaksi.toString(),
            "Nilai persediaan" to RupiahFormatter.format(report.nilaiPersediaan),
        )
        rows.forEach { (label, value) ->
            canvas.drawText(label, 40f, y, body)
            canvas.drawText(value, 400f, y, body)
            y += 22f
        }
        y += 14f
        canvas.drawText("Produk terlaris", 40f, y, heading)
        y += 22f
        if (topProducts.isEmpty()) {
            canvas.drawText("Belum ada penjualan pada periode ini.", 40f, y, body)
        } else {
            topProducts.forEach { product ->
                canvas.drawText(
                    "${product.namaProduk} - ${DateFormatter.formatQty(product.qty)} terjual",
                    40f, y, body,
                )
                y += 20f
            }
        }
        y = 800f
        canvas.drawText("Dibuat dengan Kalkulasa - usaha kecil, semua urusan beres.", 40f, y, muted)
        document.finishPage(page)
        val out = ByteArrayOutputStream()
        document.writeTo(out)
        document.close()
        return out.toByteArray()
    }

    override suspend fun shareText(from: Long, to: Long): String = buildString {
        val profile = db.businessProfileDao().getProfile()
        val report = reportBetween(from, to)
        appendLine("Laporan ${profile?.namaUsaha ?: "Usaha"}")
        appendLine("Periode ${DateFormatter.formatDayMonthYear(from)} - ${DateFormatter.formatDayMonthYear(to)}")
        appendLine()
        appendLine("Omzet: ${RupiahFormatter.format(report.omzet)}")
        appendLine("Modal per barang terjual: ${RupiahFormatter.format(report.hpp)}")
        appendLine("Laba kotor: ${RupiahFormatter.format(report.labaKotor)}")
        appendLine("Pengeluaran operasional: ${RupiahFormatter.format(report.pengeluaran)}")
        appendLine("Untung bersih: ${RupiahFormatter.format(report.labaBersih)}")
        appendLine("Margin: ${String.format(java.util.Locale("in", "ID"), "%.1f%%", report.marginPersen)}")
        appendLine("Jumlah transaksi: ${report.jumlahTransaksi}")
    }
}
