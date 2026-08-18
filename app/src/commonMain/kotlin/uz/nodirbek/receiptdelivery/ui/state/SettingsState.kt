package uz.nodirbek.receiptdelivery.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.nodirbek.receiptdelivery.data.SettingsSnapshot

class SettingsState {
    var notificationsEnabled by mutableStateOf(true)
    var language by mutableStateOf("ru")
    /** Default/selected payment method - also read from the Checkout screen, not just Settings. */
    var payment by mutableStateOf("payme")
    val dietaryPrefs = mutableStateListOf<String>()

    fun toggleDietaryPref(pref: String) {
        if (dietaryPrefs.contains(pref)) dietaryPrefs.remove(pref) else dietaryPrefs.add(pref)
    }

    fun snapshot(): SettingsSnapshot = SettingsSnapshot(
        notificationsEnabled = notificationsEnabled,
        language = language,
        payment = payment,
        dietaryPrefs = dietaryPrefs.toSet()
    )

    fun apply(s: SettingsSnapshot) {
        notificationsEnabled = s.notificationsEnabled
        language = s.language
        payment = s.payment
        dietaryPrefs.clear()
        dietaryPrefs.addAll(s.dietaryPrefs)
    }
}
