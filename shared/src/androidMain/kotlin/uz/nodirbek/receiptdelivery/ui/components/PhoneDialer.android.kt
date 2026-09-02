package uz.nodirbek.receiptdelivery.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPhoneDialer(): (phone: String) -> Unit {
    val context = LocalContext.current
    return { phone -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
}
