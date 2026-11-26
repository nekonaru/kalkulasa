package id.kalkulasa.data.mapper

import id.kalkulasa.data.local.entity.BusinessProfileEntity
import id.kalkulasa.data.local.entity.CapitalEntryEntity
import id.kalkulasa.data.local.entity.CategoryEntity
import id.kalkulasa.data.local.entity.ExpenseEntity
import id.kalkulasa.data.local.entity.ProductEntity
import id.kalkulasa.data.local.entity.PurchaseEntity
import id.kalkulasa.data.local.entity.SaleEntity
import id.kalkulasa.data.local.entity.StockMovementEntity
import id.kalkulasa.domain.model.BusinessProfile
import id.kalkulasa.domain.model.CapitalEntry
import id.kalkulasa.domain.model.Category
import id.kalkulasa.domain.model.Expense
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.model.StockMovement

fun BusinessProfileEntity.toDomain() = BusinessProfile(id, namaUsaha, jenisUsaha, modalAwal, dibuatPada)

fun CategoryEntity.toDomain() = Category(id, nama, jenis)

fun ProductEntity.toDomain() = Product(
    id = id,
    nama = nama,
    kategoriId = kategoriId,
    satuan = satuan,
    barcode = barcode,
    hargaBeliRata2 = hargaBeliRata2,
    hargaJual = hargaJual,
    stok = stok,
    stokMinimum = stokMinimum,
    fotoUri = fotoUri,
    diarsipkan = diarsipkan,
    isPinned = isPinned,
    isFavorite = isFavorite,
    hargaGrosir = hargaGrosir,
    qtyGrosir = qtyGrosir,
)

fun StockMovementEntity.toDomain(produkNama: String) = StockMovement(
    id = id,
    produkId = produkId,
    produkNama = produkNama,
    tipe = tipe,
    jumlah = jumlah,
    hargaSatuan = hargaSatuan,
    referensiId = referensiId,
    catatan = catatan,
    waktu = waktu,
)

fun id.kalkulasa.data.local.dao.MovementWithProduct.toDomain() = StockMovement(
    id = id,
    produkId = produkId,
    produkNama = produkNama,
    tipe = tipe,
    jumlah = jumlah,
    hargaSatuan = hargaSatuan,
    referensiId = referensiId,
    catatan = catatan,
    waktu = waktu,
)

fun SaleEntity.toDomain() = Sale(id, subtotal, diskon, total, metodeBayar, status, waktu, customerId, ppn, voucherKode)

fun ExpenseEntity.toDomain() = Expense(id, kategoriNama, nominal, catatan, waktu)

fun CapitalEntryEntity.toDomain() = CapitalEntry(id, tipe, nominal, catatan, waktu)
