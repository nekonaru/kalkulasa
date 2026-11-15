package id.kalkulasa.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.datastore.SettingsDataStore
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.presentation.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RootState {
    data object Loading : RootState
    data object Locked : RootState
    data class Ready(val startDestination: String) : RootState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow<RootState>(RootState.Loading)
    val state: StateFlow<RootState> = _state.asStateFlow()

    private var sessionUnlocked = false

    init {
        viewModelScope.launch { refresh() }
    }

    fun unlock() {
        sessionUnlocked = true
        viewModelScope.launch { refresh() }
    }

    private suspend fun refresh() {
        val profile = profileRepository.observeProfile().first()
        val hasPin = settings.hasPin.first()
        _state.value = when {
            hasPin && !sessionUnlocked -> RootState.Locked
            profile == null -> RootState.Ready(Routes.ONBOARDING)
            else -> RootState.Ready(Routes.HOME)
        }
    }
}
