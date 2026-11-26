@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package id.kalkulasa.presentation.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.StockMovementType
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.theme.Rugi
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StockScreen(
    onOpenRestock: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenOpname: () -> Unit,
    viewModel: StockViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val movements by viewModel.movements.collectAsStateWithLifecycle()
    val categories by viewModel.productCategories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddProduct by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is StockEvent.Message -> snackbar.showSnackbar(
                    when (event.text) {
                        "SAVED" -> context.getString(R.string.saved_generic)
                        "STOCK_IN_SAVED" -> context.getString(R.string.stock_in_saved)
                        "ADJUSTMENT_SAVED" -> context.getString(R.string.stock_adjustment_saved)
                        "ERROR" -> context.getString(R.string.error_generic)
                        else -> event.text
                    },
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddProduct = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(context.getString(R.string.add_product)) },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(context.getString(R.string.tab_products)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(context.getString(R.string.tab_stock_in)) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(context.getString(R.string.tab_history)) })
            }
            when (selectedTab) {
                0 -> ProductsTab(
                    query = query,
                    products = products,
                    onQueryChange = viewModel::onQueryChange,
                    onEdit = { editingProduct = it },
                    onToggleArchive = { viewModel.setArchived(it.id, !it.diarsipkan) },
                    onOpenRestock = onOpenRestock,
                    onOpenCategories = onOpenCategories,
                    onOpenImport = onOpenImport,
                    onOpenOpname = onOpenOpname,
                )
                1 -> StockEntryTab(viewModel = viewModel, products = products)
                2 -> HistoryTab(movements = movements)
            }
        }
    }

    if (showAddProduct) {
        ProductFormDialog(
            initial = null,
            categories = categories,
            onAddCategory = viewModel::addCategory,
            onDismiss = { showAddProduct = false },
            onSave = { nama, kategoriId, satuan, barcode, hargaBeli, hargaJual, stokAwal, stokMinimum, pin ->
                viewModel.saveProduct(null, nama, kategoriId, satuan, barcode, hargaBeli, hargaJual, stokAwal, stokMinimum, pin)
                showAddProduct = false
            },
        )
    }
    editingProduct?.let { product ->
        ProductFormDialog(
            initial = product,
            categories = categories,
            onAddCategory = viewModel::addCategory,
            onDismiss = { editingProduct = null },
            onSave = { nama, kategoriId, satuan, barcode, hargaBeli, hargaJual, _, stokMinimum, pin ->
                viewModel.saveProduct(product, nama, kategoriId, satuan, barcode, hargaBeli, hargaJual, product.stok, stokMinimum, pin)
                editingProduct = null
            },
        )
    }
}

@Composable
private fun ProductsTab(
    query: String,
    products: List<Product>,
    onQueryChange: (String) -> Unit,
    onEdit: (Product) -> Unit,
    onToggleArchive: (Product) -> Unit,
    onOpenRestock: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenOpname: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(context.getString(R.string.search)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onOpenRestock) { Text(context.getString(R.string.restock_title)) }
            TextButton(onClick = onOpenCategories) { Text(context.getString(R.string.categories_title)) }
            TextButton(onClick = onOpenImport) { Text(context.getString(R.string.import_products)) }
            TextButton(onClick = onOpenOpname) { Text(context.getString(R.string.opname_open)) }
        }
        if (products.isEmpty()) {
            KalkulasaEmptyState(
                title = context.getString(R.string.product_empty_title),
                body = context.getString(R.string.product_empty),
                icon = Icons.Rounded.Inventory2,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEdit(product) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (product.diarsipkan) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.nama, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${DateFormatter.formatQty(product.stok)} ${product.satuan} · ${RupiahFormatter.format(product.hargaJual)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (product.isLowStock && !product.diarsipkan) Rugi else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (product.diarsipkan) {
                                    Text(context.getString(R.string.product_archived), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            IconButton(onClick = { onToggleArchive(product) }) {
                                Icon(
                                    if (product.diarsipkan) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                                    contentDescription = if (product.diarsipkan) context.getString(R.string.unarchive) else context.getString(R.string.archive),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockEntryTab(
    viewModel: StockViewModel,
    products: List<Product>,
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(0) } // 0 = masuk, 1 = penyesuaian
    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var pemasok by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var newQty by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf(context.getString(R.string.reason_opname)) }
    var reasonExpanded by remember { mutableStateOf(false) }

    val activeProducts = products.filter { !it.diarsipkan }
    val selectedProduct = activeProducts.find { it.id == selectedProductId }
    val reasons = listOf(
        R.string.reason_damaged, R.string.reason_lost, R.string.reason_expired,
        R.string.reason_opname, R.string.reason_other,
    ).map { context.getString(it) }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text(context.getString(R.string.stock_in_title)) })
            FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text(context.getString(R.string.stock_adjustment_title)) })
        }

        // Product picker
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedProduct?.nama ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(context.getString(R.string.search_product)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                activeProducts.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(product.nama) },
                        onClick = { selectedProductId = product.id; expanded = false },
                    )
                }
            }
        }

        if (mode == 0) {
            OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() || c == ',' || c == '.' } }, label = { Text(context.getString(R.string.stock_in_qty)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() } }, label = { Text(context.getString(R.string.stock_in_price)) }, singleLine = true, prefix = { Text("Rp ") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pemasok, onValueChange = { pemasok = it }, label = { Text(context.getString(R.string.stock_in_supplier)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = catatan, onValueChange = { catatan = it }, label = { Text(context.getString(R.string.stock_in_note)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    viewModel.recordStockIn(selectedProductId, qty, price, pemasok, catatan)
                    qty = ""; price = ""; pemasok = ""; catatan = ""
                },
                enabled = selectedProductId != null && qty.isNotBlank() && price.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(context.getString(R.string.stock_in_save)) }
        } else {
            OutlinedTextField(value = newQty, onValueChange = { newQty = it.filter { c -> c.isDigit() || c == ',' || c == '.' } }, label = { Text(context.getString(R.string.stock_opname_new)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            ExposedDropdownMenuBox(expanded = reasonExpanded, onExpandedChange = { reasonExpanded = !reasonExpanded }) {
                OutlinedTextField(
                    value = reason,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(context.getString(R.string.stock_adjustment_reason)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                DropdownMenu(expanded = reasonExpanded, onDismissRequest = { reasonExpanded = false }) {
                    reasons.forEach { r ->
                        DropdownMenuItem(text = { Text(r) }, onClick = { reason = r; reasonExpanded = false })
                    }
                }
            }
            if (selectedProduct != null) {
                Text(
                    context.getString(R.string.stock_recorded, DateFormatter.formatQty(selectedProduct.stok), selectedProduct.satuan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    viewModel.recordAdjustment(selectedProductId, newQty, reason)
                    newQty = ""
                },
                enabled = selectedProductId != null && newQty.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(context.getString(R.string.stock_adjustment_save)) }
        }
    }
}

@Composable
private fun HistoryTab(movements: List<id.kalkulasa.domain.model.StockMovement>) {
    val context = LocalContext.current
    if (movements.isEmpty()) {
        KalkulasaEmptyState(
            title = context.getString(R.string.history_empty),
            body = "",
            modifier = Modifier.padding(24.dp),
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(movements, key = { it.id }) { movement ->
            val label = when (movement.tipe) {
                StockMovementType.MASUK -> context.getString(R.string.movement_in)
                StockMovementType.KELUAR -> context.getString(R.string.movement_out)
                StockMovementType.PENYESUAIAN -> context.getString(R.string.movement_adjustment)
                StockMovementType.RETUR -> context.getString(R.string.movement_return)
                else -> movement.tipe
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(movement.produkNama, style = MaterialTheme.typography.bodyLarge)
                        Text("$label · ${DateFormatter.formatDateTime(movement.waktu)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "${DateFormatter.formatQty(movement.jumlah)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    initial: Product?,
    categories: List<id.kalkulasa.domain.model.Category>,
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (nama: String, kategoriId: String?, satuan: String, barcode: String, hargaBeli: Long, hargaJual: Long, stokAwal: Double, stokMinimum: Double, isPinned: Boolean) -> Unit,
) {
    val context = LocalContext.current
    var nama by remember(initial) { mutableStateOf(initial?.nama ?: "") }
    var kategoriId by remember(initial) { mutableStateOf(initial?.kategoriId) }
    var kategoriExpanded by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }
    var showAddCategory by remember { mutableStateOf(false) }
    var satuan by remember(initial) { mutableStateOf(initial?.satuan ?: "pcs") }
    var barcode by remember(initial) { mutableStateOf(initial?.barcode ?: "") }
    var hargaBeli by remember(initial) { mutableStateOf(initial?.hargaBeliRata2?.toString() ?: "") }
    var hargaJual by remember(initial) { mutableStateOf(initial?.hargaJual?.toString() ?: "") }
    var stokAwal by remember(initial) { mutableStateOf("0") }
    var stokMinimum by remember(initial) { mutableStateOf(initial?.stokMinimum?.let { DateFormatter.formatQty(it) } ?: "0") }
    var isPinned by remember(initial) { mutableStateOf(initial?.isPinned ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(if (initial == null) R.string.add_product else R.string.edit_product)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.product_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    ExposedDropdownMenuBox(expanded = kategoriExpanded, onExpandedChange = { kategoriExpanded = !kategoriExpanded }) {
                        OutlinedTextField(
                            value = categories.find { it.id == kategoriId }?.nama ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(context.getString(R.string.product_category)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kategoriExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        DropdownMenu(expanded = kategoriExpanded, onDismissRequest = { kategoriExpanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(category.nama) }, onClick = { kategoriId = category.id; kategoriExpanded = false })
                            }
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("+ ${context.getString(R.string.product_category)}") }, onClick = { kategoriExpanded = false; showAddCategory = true })
                        }
                    }
                }
                item {
                    OutlinedTextField(satuan, { satuan = it }, label = { Text(context.getString(R.string.product_unit)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(barcode, { barcode = it }, label = { Text(context.getString(R.string.product_barcode)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(hargaBeli, { hargaBeli = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.product_buy_price)) }, singleLine = true, prefix = { Text("Rp ") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(hargaJual, { hargaJual = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.product_sell_price)) }, singleLine = true, prefix = { Text("Rp ") }, modifier = Modifier.fillMaxWidth())
                }
                if (initial == null) {
                    item {
                        OutlinedTextField(stokAwal, { stokAwal = it.filter { c -> c.isDigit() || c == ',' || c == '.' } }, label = { Text(context.getString(R.string.product_initial_stock)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    OutlinedTextField(stokMinimum, { stokMinimum = it.filter { c -> c.isDigit() || c == ',' || c == '.' } }, label = { Text(context.getString(R.string.product_min_stock)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                        Text(context.getString(R.string.product_pin_dialog), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        nama.trim(),
                        kategoriId,
                        satuan.trim().ifEmpty { "pcs" },
                        barcode.trim(),
                        hargaBeli.toLongOrNull() ?: 0L,
                        hargaJual.toLongOrNull() ?: 0L,
                        stokAwal.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        stokMinimum.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        isPinned,
                    )
                },
            ) { Text(context.getString(R.string.button_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(context.getString(R.string.button_cancel)) }
        },
    )

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text(context.getString(R.string.product_category)) },
            text = { OutlinedTextField(newCategory, { newCategory = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    onAddCategory(newCategory)
                    newCategory = ""
                    showAddCategory = false
                }) { Text(context.getString(R.string.button_add)) }
            },
            dismissButton = { TextButton(onClick = { showAddCategory = false }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
}
