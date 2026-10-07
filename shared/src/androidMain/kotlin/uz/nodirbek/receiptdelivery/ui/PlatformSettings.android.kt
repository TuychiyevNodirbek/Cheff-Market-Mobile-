package uz.nodirbek.receiptdelivery.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import uz.nodirbek.receiptdelivery.data.cartPrefsName
import uz.nodirbek.receiptdelivery.data.loadSettings

@Composable
actual fun rememberPlatformSettings(): Settings {
    val context = LocalContext.current
    return remember {
        SharedPreferencesSettings(context.getSharedPreferences(cartPrefsName(), Context.MODE_PRIVATE))
    }
}

/** Non-composable equivalent of [rememberPlatformSettings], for reading the persisted language
 *  before the first composition (see MainActivity.applyPersistedLocale) - androidApp doesn't
 *  depend on multiplatform-settings directly, so this stays behind :shared's public API. */
fun persistedLanguage(context: Context): String =
    SharedPreferencesSettings(context.getSharedPreferences(cartPrefsName(), Context.MODE_PRIVATE))
        .loadSettings().language
