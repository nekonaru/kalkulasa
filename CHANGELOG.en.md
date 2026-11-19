# Changelog

## v1.7.0

Repeat-sale shortcut to cut cashier time.

### New features

- **Repeat last sale.** One tap on Kasir loads the last completed sale into the cart; archived or out-of-stock products are skipped automatically.

## v1.6.0

Cleaner design and quick access to best-sellers at the cashier.

### New features

- **Best-sellers pinned to the top of the cashier.** Tap pin on a product card; pinned products surface first, then the rest. Ideal for frequently bought items.
- **Time-aware greeting on Beranda.** Good morning, afternoon or evening follows the device clock.

### Improvements

- **Product pin state persists** and travels with backups.
- **Beranda header** remembers your business name with a time-aware greeting.

## v1.5.0

Design polish, the Suppliers module, and a fuller finance history.

### New features

- **Suppliers module.** A new screen in Finance listing suppliers from your purchase history (supplier entered free-form, e.g. "Distributor Jawa"), total spend per supplier, number of transactions, and last purchase date.
- **Stock purchases in finance history.** Every stock-in is now recorded in Finance history as an outflow labeled with the supplier.
- **Roomier stat cards, smaller tonal icons, friendlier empty states.**

### Improvements

- **Design components simplified and unified** (20dp rounded corners, tonal chips).

## v1.4.0

Complete sales history and CSV product import, plus removal of deprecated code.

### New features

- **Sales history.** A new screen with all transactions per period (7, 30, 90 days), period revenue, share-receipt and cancel-with-restock actions. Reach it from Cashier > View all transactions.
- **Import products from CSV.** Prepare a text file with columns nama, satuan, harga_beli, harga_jual, stok, stok_minimum, barcode (optional), kategori (optional). Import hundreds of rows in seconds. Get a format example directly from the app.
- **Advanced CSV parser.** Supports comma or semicolon delimiters, quoted cells with embedded delimiters, optional header row, BOM handling, and Indonesian number format (dot thousands, comma decimals).

### Improvements

- **Deprecated Locale APIs replaced** with Locale.forLanguageTag("id-ID") across the codebase; no more deprecation warnings.
- **Modern WhatsApp icon** on the Piutang and low-stock screens.

## v1.3.0

Advanced cashier and reporting features, plus performance and UI/UX polish.

### New features

- **Change calculator (Cash).** When paying with cash, enter the amount received and Kalkulasa computes the change instantly.
- **Product category management.** Add, rename, and delete categories directly from the Products tab in Stok.
- **Most profitable products.** Reports show the five most profitable products for the selected period, with units sold and profit.

### Improvements

- **Branded splash screen** with the Kalkulasa logo.
- **Compatibility and code hygiene**: replaced deprecated Locale calls, modernized the WhatsApp icon.
- **Piutang screen reminder icon** switched to the auto-mirrored variant.

## v1.2.0

What is new: Warung mode for first-time sellers, customer loyalty points, and automatic restock suggestions.

### New features

- **Warung mode.** A super-simple experience for owners who are just learning: big buttons, only the numbers that matter. Switch anytime in Settings > Display mode.
- **Customers and loyalty points.** Enter a customer name at the cashier (optional); every 1,000 rupiah earns 1 point. The Customers screen shows points history, visits and total spent per customer, with point resets.
- **Restock suggestions.** Kalkulasa flags products below their minimum stock and computes how many units to reorder (target twice the minimum), ready to share as a WhatsApp message to your supplier.
- **Customer names on all sales.** Names are not only for Hutang sales; any named sale accumulates loyalty points.

### Improvements

- **CSV export** no longer includes cancelled sales.
- **Automatic database migration v2 to v3**; old data is safe.
- **Backup format v3** includes customer and points data; restoring v1 and v2 backups is still supported.

## v1.1.0

Customer debt tracking, shareable digital receipts, and a business health score right on the home screen.

### New features

- **Receivables module.** Record customer debt from sales (pick Hutang at the cashier) or manually; partial or full payments with overpayment rejected; debt book with Unpaid / Paid tabs; remind customers via WhatsApp with one tap.
- **Credit sales at the cashier.** Pick Hutang, enter the customer name, and the sale records plus the debt appears automatically in the receivables book. Cancelling a credit sale deletes its debt.
- **Digital receipt.** After a sale is saved, share the receipt via WhatsApp or any app; receipts can also be shared per transaction from today's list.
- **Business health score.** The home screen shows a 0-100 score with Prima / Sehat / Cukup / Perlu perhatian verdicts, built from margin, profit, inventory, receivables and cash, with the three most impactful suggestions.
- **In-app language switch.** Settings > Language: Bahasa Indonesia (default) or English, applied instantly.

### Improvements

- **Bahasa Indonesia is now the project default** across the app, README, repo metadata and release notes; English stays as the second language.
- **More accurate cash balance**: credit sales are not counted as cash; debt payments count when collected.
- **Faster stock history** (single join query, no N+1).
- **Safer backup**: backup versions supported up to the latest schema, files validated fully before replacement, 100 MB cap.

### Bug fixes

- **Backup restore no longer risks a crash**; parsing and restore now run on the IO thread inside an atomic database transaction.
- **First-launch SQLCipher crash** fixed by loading the native library in Application.
- **Network permission no longer declared** after removing the unused WorkManager; the manifest now has zero runtime permissions.

## v1.0.0

First release of Kalkulasa: offline stock and cash recording for small businesses, with a one-tap cashier and automatic average cost.

### New features

- **One-tap cashier.** Tap products into the cart, apply a rupiah discount, pick cash / transfer / QRIS as a payment label, and sales deduct stock with average cost recorded per item.
- **Product and stock management.** Create, edit, search, archive and restore products; stock in with purchase price; stock adjustment with reasons; full movement history.
- **Automatic average cost (HPP).** Stock in recalculates weighted-average cost, and historical profit never shifts when prices change.
- **Low stock warnings.** Beranda and Stok highlight products at or below their minimum stock.
- **Finance module.** Starting and additional capital, private draws kept separate, operating expenses per category, and a live cash balance.
- **Dashboard.** Omzet, net profit, transaction count, low-stock count, 7-day revenue trend and best sellers.
- **Reports.** Simple profit and loss for today, 7 days, 30 days or this month; inventory value; PDF and CSV export; share a text summary via WhatsApp.
- **Encrypted local database.** SQLCipher database with the key sealed by the Android Keystore; no network, no permissions, no account.
- **Backup and restore.** Full JSON backup to the Downloads folder and restore on the same or a new phone.
- **PIN lock and themes.** Optional 6-digit PIN, plus light, dark and system themes.
- **Bahasa Indonesia and English.** Bahasa Indonesia is the default; English follows the device language.
