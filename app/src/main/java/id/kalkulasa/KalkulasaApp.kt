package id.kalkulasa

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KalkulasaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load the SQLCipher native library before Room opens the database.
        System.loadLibrary("sqlcipher")
    }
}
