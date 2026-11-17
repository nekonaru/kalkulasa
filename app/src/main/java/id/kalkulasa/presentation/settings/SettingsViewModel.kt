package id.kalkulasa.presentation.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.core.datastore.SettingsDataStore
import id.kalkulasa.core.datastore.ThemeMode
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.BusinessProfile
import id.kalkulasa.domain.repository.BackupRepository
import id.kalkulasa.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface SettingsEvent {
    /** User-facing message, SAVED marker, or RESTORED marker. */
    data class Message(val text: String) : SettingsEvent
}

data class SettingsUiState(
    val namaUsaha: String = "",
    val jenisUsaha: String = "",
    val businessLoaded: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsDataStore,
    private val profileRepository: ProfileRepository,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val hasPin: StateFlow<Boolean> = settings.hasPin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val warungMode: StateFlow<Boolean> = settings.warungMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setWarungMode(enabled: Boolean) {
        viewModelScope.launch { settings.setWarungMode(enabled) }
    }

    private val _language = MutableStateFlow(id.kalkulasa.core.localization.LanguagePreference.get(context))
    val language: StateFlow<String> = _language.asStateFlow()

    /** Persist the language choice; the caller should recreate the activity. */
    fun setLanguage(lang: String) {
        id.kalkulasa.core.localization.LanguagePreference.set(context, lang)
        _language.value = lang
    }

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            _state.value = SettingsUiState(
                namaUsaha = profile?.namaUsaha ?: "",
                jenisUsaha = profile?.jenisUsaha ?: "",
                businessLoaded = true,
            )
        }
    }

    fun onNama(value: String) { _state.value = _state.value.copy(namaUsaha = value) }
    fun onJenis(value: String) { _state.value = _state.value.copy(jenisUsaha = value) }

    fun saveProfile() {
        viewModelScope.launch {
            val current = profileRepository.getProfile()
            if (current != null) {
                profileRepository.saveProfile(
                    BusinessProfile(
                        id = current.id,
                        namaUsaha = _state.value.namaUsaha.trim().ifEmpty { current.namaUsaha },
                        jenisUsaha = _state.value.jenisUsaha.trim().ifEmpty { current.jenisUsaha },
                        modalAwal = current.modalAwal,
                        dibuatPada = current.dibuatPada,
                    ),
                )
            }
            _events.send(SettingsEvent.Message("SAVED"))
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setPin(pin: String) {
        viewModelScope.launch { settings.setPin(pin); _events.send(SettingsEvent.Message("SAVED")) }
    }

    fun removePin() {
        viewModelScope.launch { settings.removePin(); _events.send(SettingsEvent.Message("SAVED")) }
    }

    /** Export a full JSON backup to the public Downloads folder (app-specific dir below API 29). */
    fun backupNow() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val json = backupRepository.exportJson()
            val fileName = "kalkulasa-backup-${DateFormatter.fileStamp(System.currentTimeMillis())}.json"
            val location: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = android.content.ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    fileName
                } else {
                    writeToAppDir(fileName, json)
                }
            } else {
                writeToAppDir(fileName, json)
            }
            _events.send(SettingsEvent.Message("BACKUP:$location"))
        }
    }

    private fun writeToAppDir(fileName: String, json: String): String {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        File(dir, fileName).writeText(json)
        return fileName
    }

    /** Restore from a content URI chosen by the user. */
    fun restoreFrom(uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
                if (size != null && size > MAX_BACKUP_BYTES) {
                    _events.send(SettingsEvent.Message("BACKUP_TOO_BIG"))
                    return@launch
                }
                val json = resolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                if (json == null) {
                    _events.send(SettingsEvent.Message("ERROR"))
                    return@launch
                }
                backupRepository.importJson(json)
                    .onSuccess { _events.send(SettingsEvent.Message("RESTORED")) }
                    .onFailure { _events.send(SettingsEvent.Message(it.message ?: "ERROR")) }
            } catch (e: Exception) {
                _events.send(SettingsEvent.Message(e.message ?: "ERROR"))
            }
        }
    }

    /** Share the last backup? Kept minimal: restore only + backup export. */
    fun ignore() = Unit

    private companion object {
        const val MAX_BACKUP_BYTES = 100L * 1024 * 1024 // 100 MB: generous for years of shop data
    }
}
