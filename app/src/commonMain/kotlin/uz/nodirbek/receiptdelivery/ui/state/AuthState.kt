package uz.nodirbek.receiptdelivery.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.nodirbek.receiptdelivery.data.AuthSnapshot

/** Auth fields only. Screen transitions (OTP step, post-auth navigation) stay in the root AppState orchestration. */
class AuthState {
    var isAuthenticated by mutableStateOf(false)
    var userPhone by mutableStateOf("")
    var userName by mutableStateOf("")
    var otpError by mutableStateOf(false)

    fun snapshot(): AuthSnapshot = AuthSnapshot(isAuthenticated, userPhone, userName)
}
