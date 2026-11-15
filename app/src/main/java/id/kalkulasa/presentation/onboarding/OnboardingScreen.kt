package id.kalkulasa.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            Icons.Rounded.Storefront,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(context.getString(R.string.onboarding_welcome), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(context.getString(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = state.namaUsaha,
            onValueChange = viewModel::onNama,
            label = { Text(context.getString(R.string.label_business_name)) },
            placeholder = { Text(context.getString(R.string.hint_business_name)) },
            singleLine = true,
            isError = state.error == "NAME_REQUIRED",
            supportingText = { if (state.error == "NAME_REQUIRED") Text(context.getString(R.string.onboarding_name_required)) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        OutlinedTextField(
            value = state.jenisUsaha,
            onValueChange = viewModel::onJenis,
            label = { Text(context.getString(R.string.label_business_type)) },
            placeholder = { Text(context.getString(R.string.hint_business_type)) },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.modalAwal,
            onValueChange = viewModel::onModal,
            label = { Text(context.getString(R.string.label_initial_capital)) },
            placeholder = { Text(context.getString(R.string.hint_initial_capital)) },
            singleLine = true,
            prefix = { Text("Rp ") },
        )
        Button(
            onClick = { viewModel.save(onDone) },
            enabled = !state.saving,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            if (state.saving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text(context.getString(R.string.button_start), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
