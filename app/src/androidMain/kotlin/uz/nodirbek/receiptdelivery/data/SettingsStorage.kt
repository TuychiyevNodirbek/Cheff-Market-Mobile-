package uz.nodirbek.receiptdelivery.data

import android.content.SharedPreferences
import androidx.core.content.edit

data class SettingsSnapshot(
    val notificationsEnabled: Boolean,
    val language: String,
    val payment: String,
    val dietaryPrefs: Set<String>
)

private const val KEY_NOTIFICATIONS = "settings_notifications"
private const val KEY_LANGUAGE = "settings_language"
private const val KEY_PAYMENT = "settings_payment"
private const val KEY_DIETARY = "settings_dietary"
private const val DIETARY_SEP = ","

fun SharedPreferences.saveSettings(snapshot: SettingsSnapshot) {
    edit {
        putBoolean(KEY_NOTIFICATIONS, snapshot.notificationsEnabled)
        putString(KEY_LANGUAGE, snapshot.language)
        putString(KEY_PAYMENT, snapshot.payment)
        putString(KEY_DIETARY, snapshot.dietaryPrefs.joinToString(DIETARY_SEP))
    }
}

fun SharedPreferences.loadSettings(): SettingsSnapshot = SettingsSnapshot(
    notificationsEnabled = getBoolean(KEY_NOTIFICATIONS, true),
    language = getString(KEY_LANGUAGE, "ru") ?: "ru",
    payment = getString(KEY_PAYMENT, "payme") ?: "payme",
    dietaryPrefs = (getString(KEY_DIETARY, "") ?: "").split(DIETARY_SEP).filter { it.isNotBlank() }.toSet()
)
