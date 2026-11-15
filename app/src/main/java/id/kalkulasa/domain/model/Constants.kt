package id.kalkulasa.domain.model

/** Shared constants used by both data and presentation layers. */

object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}

object StockMovementType {
    const val MASUK = "MASUK"
    const val KELUAR = "KELUAR"
    const val PENYESUAIAN = "PENYESUAIAN"
    const val RETUR = "RETUR"
}

object SaleStatus {
    const val SELESAI = "SELESAI"
    const val BATAL = "BATAL"
}

object PaymentMethod {
    const val TUNAI = "TUNAI"
    const val TRANSFER = "TRANSFER"
    const val QRIS = "QRIS"
}

object CapitalEntryType {
    const val MODAL_AWAL = "MODAL_AWAL"
    const val TAMBAH_MODAL = "TAMBAH_MODAL"
    const val PRIVE = "PRIVE"
}

object CategoryType {
    const val PRODUCT = "PRODUCT"
    const val EXPENSE = "EXPENSE"
}
