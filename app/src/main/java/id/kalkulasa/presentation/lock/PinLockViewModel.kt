package id.kalkulasa.presentation.lock

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val settings: SettingsDataStore,
) : ViewModel() {

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    suspend fun verify(pin: String): Boolean = settings.verifyPin(pin).also { ok -> _error.value = !ok }
}
