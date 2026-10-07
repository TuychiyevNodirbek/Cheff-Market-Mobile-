package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings

/** The app's single Settings store (auth/cart/addresses/preferences - see the data package's
 *  storage files; orders are no longer cached here, they're always fetched fresh from the backend).
 *  Android actual wraps SharedPreferences; iOS actual uses NSUserDefaults directly via
 *  multiplatform-settings' own NSUserDefaultsSettings, no custom platform code needed there. */
@Composable
expect fun rememberPlatformSettings(): Settings
