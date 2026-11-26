package id.kalkulasa.presentation.cashier

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.PaymentMethod
import id.kalkulasa.domain.model.Product
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CashierScreen(
    onOpenSalesHistory: () -> Unit,
    viewModel: CashierViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val cart by viewModel.cartEntries.collectAsStateWithLifecycle()
    val subtotal by viewModel.subtotal.collectAsStateWithLifecycle()
    val discount by viewModel.discount.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val tendered by viewModel.tendered.collectAsStateWithLifecycle()
    val change by viewModel.change.collectAsStateWithLifecycle()
    val namaPelanggan by viewModel.namaPelanggan.collectAsStateWithLifecycle()
    val todaySales by viewModel.todaySales.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val scanCode by viewModel.scanCode.collectAsStateWithLifecycle()
    var receiptToShare by remember { mutableStateOf<String?>(null) }

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var saleToCancel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CashierEvent.Message -> snackbar.showSnackbar(
                    when {
                        event.text == "SALE_SAVED" -> context.getString(R.string.sale_saved)
                        event.text == "SALE_CANCELLED" -> context.getString(R.string.sale_cancelled)
                        event.text == "CUSTOMER_NAME_REQUIRED" -> context.getString(R.string.customer_name_required)
                        event.text == "INSUFFICIENT_PAYMENT" -> context.getString(R.string.insufficient_payment)
                        event.text == "POINTS_INSUFFICIENT" -> context.getString(R.string.points_insufficient)
                        event.text == "VOUCHER_INVALID" -> context.getString(R.string.voucher_invalid)
                        event.text == "SCAN_NOT_FOUND" -> context.getString(R.string.scan_not_found)
                        event.text == "SCAN_ADDED" -> context.getString(R.string.scan_added)
                        event.text == "NO_LAST_SALE" -> context.getString(R.string.repeat_none)
                        event.text == "REPEAT_EMPTY" -> context.getString(R.string.repeat_empty)
                        event.text.startsWith("REPEAT_OK:") -> String.format(context.getString(R.string.repeat_ok), event.text.removePrefix("REPEAT_OK:").toIntOrNull() ?: 0)
                        event.text == "ERROR" -> context.getString(R.string.error_generic)
                        else -> event.text
                    },
                )
                is CashierEvent.SaleSaved -> receiptToShare = event.receipt
            }
        }
    }
    LaunchedEffect(Unit) { viewModel.refreshTodaySales() }

    val wide = LocalContext.current.resources.configuration.screenWidthDp >= 700

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        if (wide) {
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                ProductPickerSection(
                    query = query,
                    products = products,
                    onQueryChange = viewModel::onQueryChange,
                    onAdd = viewModel::addToCart,
                    onTogglePin = viewModel::togglePin,
                    onRepeatLast = viewModel::repeatLastSale,
                    scanCode = scanCode,
                    onScanCodeChange = viewModel::onScanCodeChange,
                    onScan = viewModel::scanBarcode,
                    modifier = Modifier.weight(1.15f).padding(12.dp),
                )
                CartSection(
                    cart = cart,
                    subtotal = subtotal,
                    discount = discount,
                    discountAmount = discountAmount,
                    total = total,
                    paymentMethod = paymentMethod,
                    tendered = tendered,
                    change = change,
                    onTenderedChange = viewModel::onTenderedChange,
                    namaPelanggan = namaPelanggan,
                    onNamaPelangganChange = viewModel::onNamaPelangganChange,
                    saving = saving,
                    todaySales = todaySales,
                    viewModel = viewModel,
                    onRequestCancel = { saleToCancel = it },
                    onOpenSalesHistory = onOpenSalesHistory,
                    modifier = Modifier.weight(1f).padding(12.dp),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                ProductPickerSection(
                    query = query,
                    products = products,
                    onQueryChange = viewModel::onQueryChange,
                    onAdd = viewModel::addToCart,
                    onTogglePin = viewModel::togglePin,
                    onRepeatLast = viewModel::repeatLastSale,
                    scanCode = scanCode,
                    onScanCodeChange = viewModel::onScanCodeChange,
                    onScan = viewModel::scanBarcode,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                )
                CartSection(
                    cart = cart,
                    subtotal = subtotal,
                    discount = discount,
                    discountAmount = discountAmount,
                    total = total,
                    paymentMethod = paymentMethod,
                    tendered = tendered,
                    change = change,
                    onTenderedChange = viewModel::onTenderedChange,
                    namaPelanggan = namaPelanggan,
                    onNamaPelangganChange = viewModel::onNamaPelangganChange,
                    saving = saving,
                    todaySales = todaySales,
                    viewModel = viewModel,
                    onRequestCancel = { saleToCancel = it },
                    onOpenSalesHistory = onOpenSalesHistory,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }

    receiptToShare?.let { receiptText ->
        AlertDialog(
            onDismissRequest = { receiptToShare = null },
            title = { Text(context.getString(R.string.receipt_saved_title)) },
            text = { Text(receiptText) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(receiptText)))
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val fallback = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, receiptText)
                        }
                        context.startActivity(Intent.createChooser(fallback, null))
                    }
                    receiptToShare = null
                }) { Text(context.getString(R.string.share_receipt)) }
            },
            dismissButton = {
                TextButton(onClick = { receiptToShare = null }) { Text(context.getString(R.string.receipt_later)) }
            },
        )
    }

    saleToCancel?.let { saleId ->
        AlertDialog(
            onDismissRequest = { saleToCancel = null },
            title = { Text(context.getString(R.string.cancel_sale)) },
            text = { Text(context.getString(R.string.cancel_sale_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelSale(saleId)
                    saleToCancel = null
                }) { Text(context.getString(R.string.button_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { saleToCancel = null }) { Text(context.getString(R.string.button_cancel)) }
            },
        )
    }
}

@Composable
private fun ProductPickerSection(
    query: String,
    products: List<Product>,
    onQueryChange: (String) -> Unit,
    onAdd: (Product) -> Unit,
    onTogglePin: (Product) -> Unit,
    onRepeatLast: () -> Unit,
    scanCode: String,
    onScanCodeChange: (String) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(context.getString(R.string.search_product)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = scanCode,
            onValueChange = onScanCodeChange,
            label = { Text(context.getString(R.string.scan_barcode)) },
            placeholder = { Text(context.getString(R.string.scan_barcode_hint)) },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onScan() }),
            trailingIcon = {
                TextButton(onClick = onScan, enabled = scanCode.isNotBlank()) {
                    Text(context.getString(R.string.scan_barcode))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onRepeatLast) {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(context.getString(R.string.repeat_last_sale))
        }
        if (products.isEmpty()) {
            KalkulasaEmptyState(
                title = context.getString(R.string.product_empty_title),
                body = context.getString(R.string.product_empty),
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                gridItems(products, key = { it.id }) { product ->
                    ProductPickCard(product = product, onClick = { onAdd(product) }, onTogglePin = onTogglePin)
                }
            }
        }
    }
}

@Composable
private fun CartSection(
    cart: List<CartEntry>,
    subtotal: Long,
    discount: String,
    discountAmount: Long,
    total: Long,
    paymentMethod: String,
    tendered: String,
    change: Long,
    onTenderedChange: (String) -> Unit,
    namaPelanggan: String,
    onNamaPelangganChange: (String) -> Unit,
    saving: Boolean,
    todaySales: List<id.kalkulasa.domain.model.Sale>,
    viewModel: CashierViewModel,
    onRequestCancel: (String) -> Unit,
    onOpenSalesHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val discountMode by viewModel.discountMode.collectAsStateWithLifecycle()
    val availablePoints by viewModel.availablePoints.collectAsStateWithLifecycle()
    val poinDitukar by viewModel.poinDitukar.collectAsStateWithLifecycle()
    val combinedDiscount by viewModel.totalDiscount.collectAsStateWithLifecycle()
    val voucherCode by viewModel.voucherCode.collectAsStateWithLifecycle()
    val ppnEnabled by viewModel.ppnEnabled.collectAsStateWithLifecycle()
    val ppnAmount by viewModel.ppnAmount.collectAsStateWithLifecycle()
    val memberDiscount by viewModel.memberDiscount.collectAsStateWithLifecycle()
    val voucherDiscount by viewModel.voucherDiscount.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(context.getString(R.string.cart), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        if (cart.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.cart_empty), body = "")
        } else {
            cart.forEach { entry ->
                CartRow(
                    name = entry.product.nama,
                    qty = entry.qty,
                    lineTotal = entry.lineTotal,
                    onIncrement = { viewModel.increment(entry.product.id) },
                    onDecrement = { viewModel.decrement(entry.product.id) },
                    onRemove = { viewModel.removeFromCart(entry.product.id) },
                )
            }
        }
        HorizontalDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            id.kalkulasa.presentation.cashier.DiscountMode.entries.forEach { mode ->
                FilterChip(
                    selected = discountMode == mode,
                    onClick = { viewModel.onDiscountModeChange(mode) },
                    label = {
                        Text(
                            context.getString(
                                if (mode == id.kalkulasa.presentation.cashier.DiscountMode.RUPIAH) R.string.discount_mode_rupiah
                                else R.string.discount_mode_percent,
                            ),
                        )
                    },
                )
            }
        }
        OutlinedTextField(
            value = discount,
            onValueChange = viewModel::onDiscountChange,
            label = {
                Text(
                    context.getString(
                        if (discountMode == id.kalkulasa.presentation.cashier.DiscountMode.RUPIAH) R.string.discount_amount
                        else R.string.discount_mode_percent,
                    ),
                )
            },
            singleLine = true,
            prefix = if (discountMode == id.kalkulasa.presentation.cashier.DiscountMode.RUPIAH) ({ Text("Rp ") }) else null,
            suffix = if (discountMode == id.kalkulasa.presentation.cashier.DiscountMode.PERCENT) ({ Text("%") }) else null,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = voucherCode,
            onValueChange = viewModel::onVoucherCodeChange,
            label = { Text(context.getString(R.string.voucher_code)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = ppnEnabled, onCheckedChange = viewModel::setPpnEnabled)
            Text(context.getString(R.string.ppn_enable))
        }
        TotalRow(context.getString(R.string.subtotal), RupiahFormatter.format(subtotal))
        if (memberDiscount > 0) TotalRow(context.getString(R.string.member_discount), "- ${RupiahFormatter.format(memberDiscount)}")
        if (voucherDiscount > 0) TotalRow(context.getString(R.string.voucher_discount), "- ${RupiahFormatter.format(voucherDiscount)}")
        TotalRow(context.getString(R.string.discount), "- ${RupiahFormatter.format(combinedDiscount)}")
        if (ppnAmount > 0) TotalRow(context.getString(R.string.ppn), RupiahFormatter.format(ppnAmount))
        TotalRow(context.getString(R.string.total), RupiahFormatter.format(total), emphasize = true)
        if (paymentMethod == PaymentMethod.TUNAI) {
            OutlinedTextField(
                value = tendered,
                onValueChange = onTenderedChange,
                label = { Text(context.getString(R.string.tendered_label)) },
                prefix = { Text("Rp ") },
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = { onTenderedChange(total.toString()) }) {
                        Text(context.getString(R.string.cash_exact))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (change > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(context.getString(R.string.change_label), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(RupiahFormatter.format(change), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Text(
            context.getString(R.string.payment_method),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                PaymentMethod.TUNAI to context.getString(R.string.payment_cash),
                PaymentMethod.TRANSFER to context.getString(R.string.payment_transfer),
                PaymentMethod.QRIS to context.getString(R.string.payment_qris),
                PaymentMethod.HUTANG to context.getString(R.string.payment_hutang),
            ).forEach { (method, label) ->
                FilterChip(
                    selected = paymentMethod == method,
                    onClick = { viewModel.onPaymentMethodChange(method) },
                    label = { Text(label) },
                )
            }
        }
        OutlinedTextField(
            value = namaPelanggan,
            onValueChange = onNamaPelangganChange,
            label = { Text(context.getString(R.string.customer_name_optional)) },
            placeholder = { Text(context.getString(R.string.customer_name_hint)) },
            supportingText = if (paymentMethod != PaymentMethod.HUTANG) ({ Text(context.getString(R.string.cashier_customer_hint)) }) else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (availablePoints > 0) {
            OutlinedTextField(
                value = poinDitukar,
                onValueChange = viewModel::onPoinDitukarChange,
                label = { Text(context.getString(R.string.redeem_points)) },
                placeholder = { Text(context.getString(R.string.redeem_points_hint)) },
                supportingText = { Text(context.getString(R.string.loyalty_points_available, availablePoints)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = viewModel::saveSale,
            enabled = viewModel.canSaveSale(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(context.getString(R.string.button_pay), fontSize = 16.sp)
        }

        if (todaySales.isNotEmpty()) {
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    context.getString(R.string.today_transactions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                TextButton(onClick = onOpenSalesHistory) { Text(context.getString(R.string.sales_history_all)) }
            }
            todaySales.forEach { sale ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(RupiahFormatter.format(sale.total), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            DateFormatter.formatDateTime(sale.waktu) +
                                if (sale.metodeBayar == PaymentMethod.HUTANG) " · ${context.getString(R.string.payment_hutang)}" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row {
                        IconButton(onClick = { viewModel.shareSaleReceipt(sale) }) {
                            Icon(Icons.Rounded.Share, contentDescription = context.getString(R.string.share_receipt))
                        }
                        TextButton(onClick = { onRequestCancel(sale.id) }) {
                            Text(context.getString(R.string.cancel_sale))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ProductPickCard(product: Product, onClick: () -> Unit, onTogglePin: (Product) -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(product.nama, style = MaterialTheme.typography.bodyLarge, maxLines = 2, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onTogglePin(product) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        if (product.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (product.isPinned) context.getString(R.string.unpin_product) else context.getString(R.string.pin_product),
                        modifier = Modifier.size(18.dp),
                        tint = if (product.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Text(
                RupiahFormatter.format(product.hargaJual),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                context.getString(R.string.stock_label, DateFormatter.formatQty(product.stok), product.satuan),
                style = MaterialTheme.typography.bodyMedium,
                color = if (product.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CartRow(
    name: String,
    qty: Double,
    lineTotal: Long,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text(
                RupiahFormatter.format(lineTotal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDecrement) { Icon(Icons.Rounded.Remove, contentDescription = context.getString(R.string.decrease)) }
        Text(DateFormatter.formatQty(qty), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onIncrement) { Icon(Icons.Rounded.Add, contentDescription = context.getString(R.string.increase)) }
        IconButton(onClick = onRemove) { Icon(Icons.Rounded.Delete, contentDescription = null) }
    }
}

@Composable
private fun TotalRow(label: String, value: String, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
