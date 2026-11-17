package id.kalkulasa.core.localization

import android.content.Context
import android.content.SharedPreferences

/** Synchronous language preference used by attachBaseContext. Defaults to Indonesian. */
object LanguagePreference {
    const val PREFS_NAME = "kalkulasa_lang"
    const val KEY_LANG = "lang"
    const val INDONESIAN = "id"
    const val ENGLISH = "en"

    fun get(context: Context): String =
        prefs(context).getString(KEY_LANG, INDONESIAN) ?: INDONESIAN

    fun set(context: Context, lang: String) {
        prefs(context).edit().putString(KEY_LANG, lang).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
