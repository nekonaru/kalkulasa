<div align="center">
  <img src="assets/kalkulasa-mark.png" alt="Kalkulasa logo" width="120" />
  <h1>Kalkulasa: Stock and Cash for Small Business</h1>
  <p><b>Run the daily stock and cash of a small shop with a one-tap cashier,<br/>
  automatic average cost, and simple profit reports.</b></p>

  <a href="https://github.com/nekonaru/kalkulasa/releases"><img src="https://img.shields.io/github/v/release/nekonaru/kalkulasa?style=flat-square&color=blue" alt="Release" /></a>
  <a href="https://github.com/nekonaru/kalkulasa/actions/workflows/ci.yml"><img src="https://github.com/nekonaru/kalkulasa/actions/workflows/ci.yml/badge.svg" alt="CI" /></a>
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.0" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License MIT" />
  <img src="https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square" alt="Status Active" />
</div>

<br />

[Bahasa Indonesia](README.md) | **English**

> [!NOTE]
> Kalkulasa works fully offline. Your data never leaves the phone unless you export a backup yourself.

---

## Table of Contents

- [About](#about)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Display Language](#display-language)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Security](#security)
- [Continuous Integration](#continuous-integration)
- [FAQ](#faq)
- [Author](#author)
- [Disclaimer](#disclaimer)
- [License](#license)

---

## About

**Kalkulasa** is a free, offline-first Android app for small businesses and home shops: stock in, stock out, a one-tap cashier, and profit reports with average cost (HPP) calculated automatically. Your business data stays on your phone in an encrypted local database. No account, no ads, no internet required.

> _"Small business, everything sorted."_

---

## Features

### Cashier (Kasir)

| Feature | Description |
|---------|-------------|
| **One-tap cashier** | Tap products to add them to the cart, adjust quantities, apply a discount in rupiah, pick cash / transfer / QRIS / Hutang as a label |
| **Pinned products at the cashier** | Pin your most frequently bought products so they always appear on top |
| **Repeat last sale** | One tap fills the cart with the contents of the last completed sale |
| **Automatic change calculation** | When paying with cash, enter the amount received and Kalkulasa computes the change; the Exact button fills the amount precisely |
| **Sales history** | All transactions per period, period revenue, share receipts and cancel transactions from one screen |
| **Automatic stock deduction** | Saving a sale deducts stock and records average cost (HPP) per sold item |
| **Customer debt at the cashier** | Sell with Hutang, enter the customer name, debt lands in the receivables book automatically |
| **Loyalty points** | Enter a customer name and earn points automatically (1 point per Rp 1,000) |
| **Digital receipt** | Share the receipt via WhatsApp after saving a sale |
| **Cancel with restock** | Cancelling a sale returns the stock and restores the movement history |

### Stock (Stok)

| Feature | Description |
|---------|-------------|
| **Product management** | Create, edit, search, archive and restore products with unit, barcode field and minimum stock |
| **Stock in with price** | Record purchases per product; average cost is recalculated automatically |
| **Stock adjustment** | Set the counted stock with a reason (damaged, lost, expired, opname) |
| **Movement history** | Every stock in, stock out, adjustment and return is logged |
| **Low stock warnings** | Beranda and Stok highlight products at or below their minimum stock |
| **Category management** | Add, rename, and delete product categories from the Stok tab |
| **Import products from CSV** | Import hundreds of products from a text file; supports comma/semicolon delimiters, Indonesian number format, optional header row |

### Finance (Keuangan)

| Feature | Description |
|---------|-------------|
| **Cash balance** | One glance at the cash position, computed from capital, sales, purchases, expenses and private draws |
| **Capital tracking** | Starting capital and additional capital recorded as separate entries |
| **Private draws (Prive)** | Owner takes are recorded separately so profit stays clean |
| **Operating expenses** | Expenses per category (Electricity, Rent, Salary, Transport, Other); sales and stock purchases are recorded automatically |
| **Purchase history** | Every stock-in is recorded in the finance history labeled with the supplier |
| **Suppliers** | Total spend, transaction count and last purchase date per supplier; tap to see purchase details per period |
| **Accounts payable** | Record supplier bills, pay partially or in full, filter overdue items and send a payment confirmation via WhatsApp |
| **Full financial statements** | Balance sheet, cash detail and expense categories to see the business position and money flow |
| **Discounts and points** | Rupiah or percentage discounts, redeem customer points for a discount, and daily and monthly sales targets |

### Customers and points

| Feature | Description |
|---------|-------------|
| **Loyalty points** | 1 point per Rp 1,000 spent; points accumulate per customer matching the name entered at the cashier |
| **Customer book** | Customer list with total points, visits and total spent |
| **Point reset** | Reset points per customer anytime |

### Restock suggestions

| Feature | Description |
|---------|-------------|
| **Automatic restock suggestions** | Products below their minimum are flagged with a suggested reorder quantity (twice the minimum) |
| **Share with supplier** | Send the restock list via WhatsApp with one tap |

### Receivables (Piutang)

| Feature | Description |
|---------|-------------|
| **Record debt** | From Hutang sales or manually with an optional due date |
| **Partial payments** | Pay any amount, auto-settled when paid off; overpayments are rejected |
| **Remind via WhatsApp** | Send a polite payment reminder to the customer with one tap |

### Dashboard (Beranda)

| Feature | Description |
|---------|-------------|
| **Today at a glance** | Omzet, net profit, transaction count and low-stock count |
| **7-day trend** | Bar chart of revenue with profit markers per day |
| **Best sellers** | Top 3 products of the last 7 days |
| **Business health score** | 0-100 score from margin, profit, inventory, receivables and cash, with three most impactful suggestions |

### Reports (Laporan)

| Feature | Description |
|---------|-------------|
| **Simple profit and loss** | Revenue, cost of goods sold, gross profit, operating expenses and net profit, for today, 7 days, 30 days or this month |
| **Inventory value** | Current stock valued at average cost |
| **Most profitable products** | Top five products by profit for the selected period, with units sold |
| **Export PDF and CSV** | Customer-ready PDF summary or spreadsheet-ready CSV export |
| **Share to WhatsApp** | Send a text summary of a period report via WhatsApp or any app |

### Data and backup

| Feature | Description |
|---------|-------------|
| **Encrypted local database** | All data is stored in an SQLCipher-encrypted Room database, unlocked by a key held in the Android Keystore |
| **JSON backup** | Export a full backup to the Downloads folder and restore it later on the same or a new phone |
| **Sync-ready schema** | Every table uses UUID ids with `updatedAt`, `deletedAt`, `deviceId` and `syncStatus` columns |

### Settings (Pengaturan)

| Feature | Description |
|---------|-------------|
| **Theme** | Light, dark or follow the system |
| **Warung / Pro mode** | Warung mode for a simplified view, Pro mode for the full feature set |
| **App language** | Bahasa Indonesia (default) or English, switch in Settings and it applies instantly |
| **PIN lock** | Optional 6-digit PIN to protect the app |
| **Business profile** | Edit business name and type anytime |

---

## Requirements

| Dependency | Minimum Version | Notes |
|------------|-----------------|-------|
| **JDK** | 17 (21 recommended) | Required only to build from source |
| **Android SDK** | API 35 | Required only to build from source |
| **Android device or emulator** | Android 7.0 (API 24) | Required to run the app |

> [!TIP]
> If you only want to use the app, you do not need JDK or the Android SDK. Download the APK from the [Releases](https://github.com/nekonaru/kalkulasa/releases) page instead.

### Never used a terminal? Start here

<details>
<summary><b>How to open a terminal on Windows</b></summary>

1. Press the **Windows** key, type `PowerShell`, then press **Enter**
2. A blue window opens. That is where you type commands
3. Every `git ...` or `./gradlew ...` command in this guide is typed in that window, followed by **Enter**

</details>

---

## Installation

### Option A: Download the APK (easiest)

1. Open the [Releases](https://github.com/nekonaru/kalkulasa/releases) page
2. Download `kalkulasa-v2.5.0.apk`
3. Open it on your phone and allow installs from unknown sources if asked
4. Open the app and fill in your business profile

> [!IMPORTANT]
> Kalkulasa requests no runtime permissions. Allow "install from unknown sources" only to install the APK you downloaded yourself from this repository.

### Option B: Build from source (for development / to read or change the code)

#### 1. Get the project code

<details>
<summary><b>I have Git installed</b></summary>

```bash
git clone https://github.com/nekonaru/kalkulasa.git
cd kalkulasa
```

</details>

<details>
<summary><b>I don't have Git</b></summary>

1. Open the repository page on GitHub
2. Click the green **`Code`** button, choose **`Download ZIP`**
3. Extract the ZIP to a folder of your choice
4. In the terminal, go into the extracted folder, for example:

```bash
cd Downloads/kalkulasa-main
```

</details>

#### 2. Build the APK

```bash
./gradlew :app:assembleDebug
```

The result is at `app/build/outputs/apk/debug/app-debug.apk`.

#### 3. Install on a device or emulator

```bash
./gradlew :app:installDebug
```

---

## Usage

### First launch

1. Open Kalkulasa and fill in your business name, business type and starting capital
2. Open the **Stok** tab, add your products (name, buy price, sell price, stock, minimum stock)
3. Record a **Stok Masuk** entry each time you restock
4. Sell from the **Kasir** tab; stock goes down and profit is recorded automatically
5. Open **Beranda** and **Laporan** to see how the shop is doing

### Everyday use

- Sell: **Kasir**, tap products, tap **Simpan penjualan**
- Restock: **Stok**, tab **Stok Masuk**, pick the product, fill quantity and buy price
- Check profit: **Beranda** for today, **Laporan** for a period
- Back up your data monthly: **Pengaturan**, **Backup data**

---

## Display Language

The app UI ships in **Bahasa Indonesia** (default) and **English**. The app always opens in Bahasa Indonesia, whatever the phone's system language is. To switch to English, open **Settings** and pick **English** under **Language**. Your choice is saved and applies immediately.

---

## Project Structure

```
kalkulasa/
├── app/
│   ├── schemas/                  # Room schema history (generated, committed)
│   └── src/main/
│       ├── java/id/kalkulasa/
│       │   ├── core/             # formatters (Rupiah, dates), DataStore preferences
│       │   ├── data/             # Room entities, DAOs, database, repositories
│       │   ├── domain/           # pure Kotlin models, repository interfaces, use cases
│       │   ├── di/               # Hilt modules
│       │   └── presentation/     # screens per feature, theme, navigation
│       └── res/                  # strings (Indonesian default, English), icons, theme
├── assets/                       # app mark and store assets
├── .github/workflows/            # ci.yml (build and test), release.yml (signed APK)
├── LICENSE
├── README.en.md                  # documentation (English)
└── README.md                     # documentation (Bahasa Indonesia)
```

---

## Development

### Adding a new user-facing string

1. Add the key to `app/src/main/res/values/strings.xml` (Bahasa Indonesia) and `app/src/main/res/values-en/strings.xml` (English)
2. Use it with `stringResource(R.string.your_key)` or from a ViewModel via the context

### Adding a new screen

1. Create `presentation/<feature>/<Feature>Screen.kt` and `<Feature>ViewModel.kt`
2. Register the route in `presentation/navigation/Routes.kt` and `KalkulasaNavHost.kt`
3. Keep business logic in the domain layer (use cases in `domain/usecase`), never in composables

> [!WARNING]
> `app/schemas/` is generated by Room on every build. Commit it, but never edit it by hand.

---

## Testing

Run the checks before pushing:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

| Check | Requirement |
|-------|-------------|
| `./gradlew :app:testDebugUnitTest` | Must pass the full suite |
| `./gradlew :app:assembleDebug` | Must build without errors |
| `./gradlew :app:assembleRelease` | Release must pass lint and R8 |

Lint is enforced by Android Gradle Plugin's `lintVitalRelease` on release builds.

---

## Security

Kalkulasa stores everything locally in an SQLCipher-encrypted database; the key is generated on the device and sealed by the Android Keystore. The app makes no network connections and requests no permissions. Backups you export are plain JSON; store them somewhere private. See [`SECURITY.md`](SECURITY.md) for how to report a vulnerability.

> [!NOTE]
> A local PIN only gates the UI. Anyone with full access to an unlocked phone can read the data the app shows.

---

## Continuous Integration

| Workflow | Purpose |
|----------|---------|
| `ci.yml` | Unit tests and debug APK build on every push and pull request |
| `release.yml` | Signed release APK and GitHub Release on `v*` tags |

The release workflow signs the APK with a keystore held in repository secrets and generates the release notes from `CHANGELOG.md` and `CHANGELOG.en.md`.

---

## FAQ

<details>
<summary><b>Where is my data stored?</b></summary>

On your phone, in an encrypted local database. Nothing is uploaded anywhere. Export a JSON backup monthly from **Pengaturan** to protect yourself from losing or breaking the phone.

</details>

<details>
<summary><b>Is English supported?</b></summary>

Yes. The UI follows your phone's language: Bahasa Indonesia by default, English when the phone language is English.

</details>

<details>
<summary><b>Does it need internet?</b></summary>

No. Every feature works without internet. Internet is only used if you choose to share a report to WhatsApp.

</details>

<details>
<summary><b>Can I scan barcodes?</b></summary>

Products have a barcode field, but scanning is not implemented yet. It is on the roadmap.

</details>

---

## Author

<div align="center">

| [![Nicolas Dwi Dharma](https://github.com/nekonaru.png?size=100)](https://github.com/nekonaru) |
|:---:|
| **Nicolas Dwi Dharma** |
| [github.com/nekonaru](https://github.com/nekonaru) |

</div>

---

## Disclaimer

Kalkulasa is a recording tool. Net profit and reports are estimates computed from your own records; they are not tax, accounting or legal advice. Consult a professional for anything official.

---

## License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for more information.
