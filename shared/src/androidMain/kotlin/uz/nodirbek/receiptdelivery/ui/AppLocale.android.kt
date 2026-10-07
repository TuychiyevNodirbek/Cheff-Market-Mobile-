package uz.nodirbek.receiptdelivery.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** The Android actual: [java.util.Locale.setDefault] is what
 *  `androidx.compose.ui.text.intl.Locale.current` actually reads (see
 *  `AndroidLocaleDelegateAPI24`), which is what compose-resources' `stringResource` keys its
 *  language lookup on. Recreating the Activity forces every already-composed screen to recompose
 *  against the new default instead of just the screens navigated to afterwards. [MainActivity]
 *  already applies the persisted language before the first composition, so this is a no-op then. */
@Composable
actual fun ApplyAppLanguage(languageCode: String) {
    val context = LocalContext.current
    LaunchedEffect(languageCode) {
        if (Locale.getDefault().language == languageCode) return@LaunchedEffect
        Locale.setDefault(Locale(languageCode))
        (context as? Activity)?.recreate()
    }
}
