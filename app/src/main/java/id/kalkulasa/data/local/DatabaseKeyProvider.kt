package id.kalkulasa.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the passphrase for the SQLCipher database.
 *
 * A random 32-byte passphrase is generated once, encrypted with an
 * Android Keystore AES key, and stored as ciphertext in SharedPreferences.
 * The Keystore key never leaves secure hardware (or the Keystore-backed
 * implementation on emulators), so the passphrase cannot be recovered from
 * app data alone. If the Keystore key is invalidated (for example, after a
 * system change), a new passphrase is generated, leaving the old database
 * unreadable; backups made before that moment can still be restored via the
 * JSON backup feature.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kalkulasa_db_key", Context.MODE_PRIVATE)

    fun getPassphrase(): ByteArray {
        readStored()?.let { return it }
        val passphrase = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        store(passphrase)
        return passphrase
    }

    private fun readStored(): ByteArray? {
        val blob = prefs.getString(KEY_BLOB, null) ?: return null
        return try {
            val parts = blob.split(':')
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(128, iv))
            cipher.doFinal(ciphertext)
        } catch (_: Exception) {
            // Key invalidated or blob corrupt: regenerate below.
            prefs.edit().remove(KEY_BLOB).apply()
            null
        }
    }

    private fun store(passphrase: ByteArray) {
        val key = keystoreKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext = cipher.doFinal(passphrase)
        val iv = cipher.iv
        prefs.edit()
            .putString(KEY_BLOB, Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .apply()
    }

    private fun keystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "kalkulasa_db_master_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BLOB = "passphrase_blob"
    }
}
