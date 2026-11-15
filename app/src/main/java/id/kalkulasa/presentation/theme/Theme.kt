package id.kalkulasa.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealLumbung,
    onPrimary = KremKasa,
    primaryContainer = Color(0xFFD5E5E9),
    onPrimaryContainer = TintaMalam,
    secondary = AmberCuan,
    onSecondary = TintaMalam,
    secondaryContainer = Color(0xFFFFE3A8),
    onSecondaryContainer = Color(0xFF4A3100),
    background = KremKasa,
    onBackground = TintaMalam,
    surface = Color(0xFFFFFDF6),
    onSurface = TintaMalam,
    surfaceVariant = Color(0xFFF1ECDD),
    onSurfaceVariant = Color(0xFF3E4A50),
    error = Rugi,
    onError = Color.White,
    outline = Color(0xFF8A979C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FB4C4),
    onPrimary = TintaMalam,
    primaryContainer = TealLumbung,
    onPrimaryContainer = KremKasa,
    secondary = AmberCuan,
    onSecondary = TintaMalam,
    secondaryContainer = Color(0xFF5A4205),
    onSecondaryContainer = Color(0xFFFFE3A8),
    background = KremKasaDark,
    onBackground = KremKasa,
    surface = Color(0xFF22333A),
    onSurface = KremKasa,
    surfaceVariant = Color(0xFF2B3E45),
    onSurfaceVariant = Color(0xFFC3CDD1),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF5F0000),
    outline = Color(0xFF8A979C),
)

@Composable
fun KalkulasaTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KalkulasaTypography,
        content = content,
    )
}
