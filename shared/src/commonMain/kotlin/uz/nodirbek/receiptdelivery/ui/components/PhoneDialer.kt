package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable

/** Returns a function that opens the system phone dialer pre-filled with the given number
 *  (e.g. the support number in Settings). A factory rather than a plain top-level function
 *  because the Android actual needs a Context, only available inside a @Composable. */
@Composable
expect fun rememberPhoneDialer(): (phone: String) -> Unit
