package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberPhoneDialer(): (phone: String) -> Unit = { phone ->
    NSURL.URLWithString("tel:$phone")?.let { UIApplication.sharedApplication.openURL(it) }
}
