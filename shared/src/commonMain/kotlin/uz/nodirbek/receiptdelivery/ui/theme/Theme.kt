package uz.nodirbek.receiptdelivery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Green,
    tertiary = Amber,
    background = CookingBg,
    onBackground = Color.White,
    surface = CookingBg,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Green,
    tertiary = Amber,
    background = Surface,
    onBackground = TextDark,
    surface = Surface,
    onSurface = TextDark,
    surfaceVariant = Border,
    onSurfaceVariant = TextMuted,
    outline = Border
)

/** Android 12+ Material You dynamic color, wallpaper-derived - no equivalent on iOS.
 *  Android actual returns it (API 31+ only); iOS actual always returns null. */
@Composable
internal expect fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme?

@Composable
fun ReceipeDeliveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = (if (dynamicColor) dynamicColorSchemeOrNull(darkTheme) else null)
        ?: if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
