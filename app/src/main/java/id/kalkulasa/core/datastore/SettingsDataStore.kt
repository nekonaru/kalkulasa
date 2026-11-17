package id.kalkulasa.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "kalkulasa_settings")

enum class ThemeMode(val key: String) { SYSTEM("system"), LIGHT("light"), DARK("dark") }

/** App preferences backed by DataStore: theme, PIN lock, onboarding completion. */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val pinHash = stringPreferencesKey("pin_hash") // sha256 hex, empty = no PIN
        val warungMode = booleanPreferencesKey("warung_mode")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.theme]) {
            ThemeMode.LIGHT.key -> ThemeMode.LIGHT
            ThemeMode.DARK.key -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = mode.key }
    }

    val hasPin: Flow<Boolean> = context.dataStore.data.map { !it[Keys.pinHash].isNullOrEmpty() }

    /** Verify a PIN attempt against the stored hash. */
    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.pinHash] != null && prefs[Keys.pinHash] == hash(pin)
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { it[Keys.pinHash] = hash(pin) }
    }

    suspend fun removePin() {
        context.dataStore.edit { it.remove(Keys.pinHash) }
    }

    /** Mode Warung: simplified home for first-time sellers. False = Mode Pro (default). */
    val warungMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.warungMode] ?: false }

    suspend fun setWarungMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.warungMode] = enabled }
    }

    private fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
