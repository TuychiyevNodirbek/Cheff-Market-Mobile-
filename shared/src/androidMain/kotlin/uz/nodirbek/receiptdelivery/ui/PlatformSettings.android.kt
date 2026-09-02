package uz.nodirbek.receiptdelivery.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import uz.nodirbek.receiptdelivery.data.cartPrefsName

@Composable
actual fun rememberPlatformSettings(): Settings {
    val context = LocalContext.current
    return remember {
        SharedPreferencesSettings(context.getSharedPreferences(cartPrefsName(), Context.MODE_PRIVATE))
    }
}
