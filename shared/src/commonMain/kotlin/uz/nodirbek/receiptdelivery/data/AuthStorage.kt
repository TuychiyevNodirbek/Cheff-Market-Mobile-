package uz.nodirbek.receiptdelivery.data

import com.russhwolf.settings.Settings

data class AuthSnapshot(
    val isAuthenticated: Boolean,
    val phone: String,
    val name: String,
    val token: String = ""
)

private const val KEY_AUTHENTICATED = "is_authenticated"
private const val KEY_PHONE = "user_phone"
private const val KEY_NAME = "user_name"
private const val KEY_TOKEN = "api_token"

fun Settings.saveAuth(snapshot: AuthSnapshot) {
    putBoolean(KEY_AUTHENTICATED, snapshot.isAuthenticated)
    putString(KEY_PHONE, snapshot.phone)
    putString(KEY_NAME, snapshot.name)
    putString(KEY_TOKEN, snapshot.token)
}

fun Settings.loadAuth(): AuthSnapshot = AuthSnapshot(
    isAuthenticated = getBoolean(KEY_AUTHENTICATED, false),
    phone = getString(KEY_PHONE, ""),
    name = getString(KEY_NAME, ""),
    token = getString(KEY_TOKEN, "")
)
