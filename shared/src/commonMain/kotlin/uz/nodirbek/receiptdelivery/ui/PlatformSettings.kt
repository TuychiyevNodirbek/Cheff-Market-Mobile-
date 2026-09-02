package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings

/** The app's single Settings store (auth/cart/orders/addresses/preferences - see the data package's
 *  storage files). Android actual wraps SharedPreferences; iOS actual uses NSUserDefaults directly
 *  via multiplatform-settings' own NSUserDefaultsSettings, no custom platform code needed there. */
@Composable
expect fun rememberPlatformSettings(): Settings
