package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberPlatformSettings(): Settings = remember {
    NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
