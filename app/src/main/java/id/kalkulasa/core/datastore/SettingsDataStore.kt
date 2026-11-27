package id.kalkulasa.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
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
        val pinHash = stringPreferencesKey("pin_hash") // pbkdf2$salt$hash, empty = no PIN
        val warungMode = booleanPreferencesKey("warung_mode")
        val ppnEnabled = booleanPreferencesKey("ppn_enabled")
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

    /** Verify a PIN attempt. Legacy unsalted hashes are upgraded on success. */
    suspend fun verifyPin(pin: String): Boolean {
        val stored = context.dataStore.data.first()[Keys.pinHash] ?: return false
        return withContext(Dispatchers.Default) {
            if (stored.startsWith(PREFIX)) {
                val parts = stored.split('$')
                if (parts.size != 3) return@withContext false
                val expected = try {
                    derive(pin, hexToBytes(parts[1]))
                } catch (e: Exception) {
                    return@withContext false
                }
                return@withContext constantTimeEquals(parts[2], expected)
            }
            if (constantTimeEquals(stored, legacyHash(pin))) {
                setPin(pin)
                return@withContext true
            }
            false
        }
    }

    suspend fun setPin(pin: String) {
        val value = withContext(Dispatchers.Default) {
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            listOf(PREFIX, hex(salt), derive(pin, salt)).joinToString("$")
        }
        context.dataStore.edit { it[Keys.pinHash] = value }
    }

    suspend fun removePin() {
        context.dataStore.edit { it.remove(Keys.pinHash) }
    }

    /** Mode Warung: simplified home for first-time sellers. False = Mode Pro (default). */
    val warungMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.warungMode] ?: false }

    suspend fun setWarungMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.warungMode] = enabled }
    }

    val ppnEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ppnEnabled] ?: false }

    suspend fun setPpnEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ppnEnabled] = enabled }
    }

    private fun derive(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256)
        return hex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)
    }

    private fun legacyHash(pin: String): String =
        hex(MessageDigest.getInstance("SHA-256").digest(pin.toByteArray()))

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(value: String): ByteArray =
        ByteArray(value.length / 2) { i -> value.substring(i * 2, i * 2 + 2).toInt(16).toByte() }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }

    private companion object {
        const val PREFIX = "pbkdf2"
        const val ITERATIONS = 120_000
    }
}
