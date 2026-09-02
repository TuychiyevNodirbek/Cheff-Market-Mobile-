package uz.nodirbek.receiptdelivery.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** No Material You / wallpaper-derived color on iOS - always fall back to the static palette. */
@Composable
internal actual fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? = null
