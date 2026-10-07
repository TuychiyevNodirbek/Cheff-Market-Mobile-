package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable

// UNVERIFIED on this machine (see docs/ios-phase-plan.md): compose-resources' iOS language
// lookup follows NSLocale.currentLocale, which needs "AppleLanguages" in NSUserDefaults updated
// and the app relaunched to take effect - there's no in-process recreate equivalent to Android's
// Activity.recreate(). Left as a no-op until the iOS target actually builds.
@Composable
actual fun ApplyAppLanguage(languageCode: String) {
}
