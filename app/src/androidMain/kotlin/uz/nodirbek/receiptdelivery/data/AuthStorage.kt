package uz.nodirbek.receiptdelivery.data

import android.content.SharedPreferences
import androidx.core.content.edit

data class AuthSnapshot(
    val isAuthenticated: Boolean,
    val phone: String,
    val name: String
)

private const val KEY_AUTHENTICATED = "is_authenticated"
private const val KEY_PHONE = "user_phone"
private const val KEY_NAME = "user_name"

fun SharedPreferences.saveAuth(snapshot: AuthSnapshot) {
    edit {
        putBoolean(KEY_AUTHENTICATED, snapshot.isAuthenticated)
        putString(KEY_PHONE, snapshot.phone)
        putString(KEY_NAME, snapshot.name)
    }
}

fun SharedPreferences.loadAuth(): AuthSnapshot = AuthSnapshot(
    isAuthenticated = getBoolean(KEY_AUTHENTICATED, false),
    phone = getString(KEY_PHONE, "") ?: "",
    name = getString(KEY_NAME, "") ?: ""
)
