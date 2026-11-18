package id.kalkulasa

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import id.kalkulasa.core.datastore.SettingsDataStore
import id.kalkulasa.core.datastore.ThemeMode
import id.kalkulasa.presentation.MainViewModel
import id.kalkulasa.presentation.RootState
import id.kalkulasa.presentation.navigation.KalkulasaNavHost
import id.kalkulasa.presentation.theme.KalkulasaTheme
import id.kalkulasa.presentation.theme.TealLumbung
import javax.inject.Inject
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settings: SettingsDataStore

    override fun attachBaseContext(newBase: Context) {
        val lang = id.kalkulasa.core.localization.LanguagePreference.get(newBase)
        val locale = java.util.Locale.forLanguageTag(lang)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val themeMode by settings.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            KalkulasaTheme(darkTheme = darkTheme) {
                val viewModel: MainViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                when (val current = state) {
                    is RootState.Loading -> Box(
                        Modifier.fillMaxSize().background(TealLumbung),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Color.White) }
                    is RootState.Locked -> id.kalkulasa.presentation.lock.PinLockScreen(
                        onUnlock = viewModel::unlock,
                    )
                    is RootState.Ready -> KalkulasaNavHost(startDestination = current.startDestination)
                }
            }
        }
    }
}
