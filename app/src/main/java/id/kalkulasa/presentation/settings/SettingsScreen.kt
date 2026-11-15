@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package id.kalkulasa.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.BuildConfig
import id.kalkulasa.R
import id.kalkulasa.core.datastore.ThemeMode
import id.kalkulasa.presentation.component.SectionHeader
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var pinDialog by remember { mutableStateOf(false) }
    var removingPin by remember { mutableStateOf(false) }
    var restoreConfirm by remember { mutableStateOf(false) }

    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.restoreFrom(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.Message -> {
                    val message = when {
                        event.text == "SAVED" -> "Tersimpan"
                        event.text == "RESTORED" -> context.getString(R.string.restore_done)
                        event.text.startsWith("BACKUP:") -> context.getString(R.string.backup_done) + ": " + event.text.removePrefix("BACKUP:")
                        else -> event.text
                    }
                    snackbar.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Business profile
            SectionHeader(context.getString(R.string.settings_business))
            OutlinedTextField(
                value = state.namaUsaha,
                onValueChange = viewModel::onNama,
                label = { Text(context.getString(R.string.label_business_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.jenisUsaha,
                onValueChange = viewModel::onJenis,
                label = { Text(context.getString(R.string.label_business_type)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = viewModel::saveProfile, modifier = Modifier.fillMaxWidth()) {
                Text(context.getString(R.string.button_save))
            }

            HorizontalDivider()

            // Appearance
            SectionHeader(context.getString(R.string.settings_appearance))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ThemeMode.SYSTEM to R.string.theme_system,
                    ThemeMode.LIGHT to R.string.theme_light,
                    ThemeMode.DARK to R.string.theme_dark,
                ).forEach { (mode, label) ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(context.getString(label)) },
                    )
                }
            }

            HorizontalDivider()

            // Security
            SectionHeader(context.getString(R.string.settings_security))
            Text(context.getString(R.string.pin_enable), style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasPin) {
                    OutlinedButton(onClick = { pinDialog = true }) { Text(context.getString(R.string.pin_change)) }
                    OutlinedButton(
                        onClick = { removingPin = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text(context.getString(R.string.pin_remove)) }
                } else {
                    Button(onClick = { pinDialog = true }) { Text(context.getString(R.string.pin_set)) }
                }
            }

            HorizontalDivider()

            // Data
            SectionHeader(context.getString(R.string.settings_data))
            Button(onClick = viewModel::backupNow, modifier = Modifier.fillMaxWidth()) { Text(context.getString(R.string.backup_now)) }
            OutlinedButton(onClick = { restoreConfirm = true }, modifier = Modifier.fillMaxWidth()) { Text(context.getString(R.string.restore_now)) }

            HorizontalDivider()

            // About
            SectionHeader(context.getString(R.string.about))
            Text(
                context.getString(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                context.getString(R.string.licenses),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (pinDialog) {
        PinDialog(
            onDismiss = { pinDialog = false },
            onSet = { pin -> viewModel.setPin(pin); pinDialog = false },
        )
    }

    if (removingPin) {
        AlertDialog(
            onDismissRequest = { removingPin = false },
            title = { Text(context.getString(R.string.pin_remove)) },
            text = { Text(context.getString(R.string.pin_enable)) },
            confirmButton = {
                TextButton(onClick = { viewModel.removePin(); removingPin = false }) {
                    Text(context.getString(R.string.button_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { removingPin = false }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }

    if (restoreConfirm) {
        AlertDialog(
            onDismissRequest = { restoreConfirm = false },
            title = { Text(context.getString(R.string.restore_now)) },
            text = { Text(context.getString(R.string.restore_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    restoreConfirm = false
                    pickBackup.launch(arrayOf("application/json", "*/*"))
                }) { Text(context.getString(R.string.button_confirm)) }
            },
            dismissButton = { TextButton(onClick = { restoreConfirm = false }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
}

@Composable
private fun PinDialog(
    onDismiss: () -> Unit,
    onSet: (String) -> Unit,
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.pin_set)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(6); error = null },
                    label = { Text(context.getString(R.string.pin_new)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it.filter(Char::isDigit).take(6); error = null },
                    label = { Text(context.getString(R.string.pin_confirm)) },
                    singleLine = true,
                    isError = error != null,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin.length < 6 -> error = context.getString(R.string.pin_too_short)
                    pin != confirm -> error = context.getString(R.string.pin_mismatch)
                    else -> onSet(pin)
                }
            }) { Text(context.getString(R.string.button_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(context.getString(R.string.button_cancel)) } },
    )
}
