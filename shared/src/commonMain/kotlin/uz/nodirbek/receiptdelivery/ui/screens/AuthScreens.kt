package uz.nodirbek.receiptdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.auth_otp_subtitle
import uz.nodirbek.receiptdelivery.shared.resources.auth_otp_title
import uz.nodirbek.receiptdelivery.shared.resources.auth_phone_subtitle
import uz.nodirbek.receiptdelivery.shared.resources.auth_phone_title
import uz.nodirbek.receiptdelivery.shared.resources.auth_profile_subtitle
import uz.nodirbek.receiptdelivery.shared.resources.auth_profile_title
import uz.nodirbek.receiptdelivery.shared.resources.change_number
import uz.nodirbek.receiptdelivery.shared.resources.checking
import uz.nodirbek.receiptdelivery.shared.resources.confirm
import uz.nodirbek.receiptdelivery.shared.resources.continue_label
import uz.nodirbek.receiptdelivery.shared.resources.get_code
import uz.nodirbek.receiptdelivery.shared.resources.name_placeholder
import uz.nodirbek.receiptdelivery.shared.resources.otp_wrong_code
import uz.nodirbek.receiptdelivery.shared.resources.sending_code
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.components.PrimaryButton
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

@Composable
private fun OtpCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 4
) {
    BasicTextField(
        value = code,
        onValueChange = { new -> if (new.length <= length && new.all { it.isDigit() }) onCodeChange(new) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(length) { i ->
                    val filled = i < code.length
                    val isCursor = i == code.length
                    Box(
                        Modifier
                            .size(56.dp)
                            .background(CardWhite, RoundedCornerShape(12.dp))
                            .border(if (filled || isCursor) 2.dp else 1.dp, if (filled || isCursor) Orange else Border, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            code.getOrNull(i)?.toString() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun AuthHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
        BackButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp))
        Text(
            title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Text(subtitle, fontSize = 14.sp, color = TextMuted, lineHeight = 20.sp)
    }
}

private val fieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Border,
    focusedBorderColor = Orange,
    unfocusedContainerColor = CardWhite,
    focusedContainerColor = CardWhite
)

private const val PHONE_PREFIX = "+998 "
private const val SUBSCRIBER_NUMBER_LENGTH = 9

@Composable
fun AuthPhoneScreen(state: AppState) {
    // Only the subscriber number (digits after the fixed "+998 " prefix) lives in state -
    // the prefix itself is never part of what the user can edit or delete.
    var subscriberNumber by remember {
        mutableStateOf(state.userPhone.removePrefix("+998").filter { it.isDigit() }.take(SUBSCRIBER_NUMBER_LENGTH))
    }

    Column(Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp, vertical = 32.dp)) {
        AuthHeader(
            title = stringResource(Res.string.auth_phone_title),
            subtitle = stringResource(Res.string.auth_phone_subtitle),
            onBack = { state.go(Screen.ONB2) }
        )
        OutlinedTextField(
            value = PHONE_PREFIX + subscriberNumber,
            onValueChange = { new ->
                // Any edit that doesn't keep the "+998 " prefix intact (e.g. backspacing into it)
                // is ignored - only digits typed after it ever change the state.
                if (new.startsWith(PHONE_PREFIX)) {
                    subscriberNumber = new.removePrefix(PHONE_PREFIX)
                        .filter { it.isDigit() }
                        .take(SUBSCRIBER_NUMBER_LENGTH)
                }
            },
            placeholder = { Text("+998 90 123 45 67") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        state.authErrorMessage?.let { message ->
            Text(
                message,
                fontSize = 12.sp,
                color = Orange,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        PrimaryButton(
            if (state.authLoading) stringResource(Res.string.sending_code) else stringResource(Res.string.get_code),
            onClick = { state.submitPhone("+998$subscriberNumber") },
            enabled = subscriberNumber.length == SUBSCRIBER_NUMBER_LENGTH && !state.authLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
    }
}

@Composable
fun AuthOtpScreen(state: AppState) {
    var code by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp, vertical = 32.dp)) {
        AuthHeader(
            title = stringResource(Res.string.auth_otp_title),
            subtitle = stringResource(Res.string.auth_otp_subtitle, state.userPhone),
            onBack = { state.go(Screen.AUTH_PHONE) }
        )
        OtpCodeField(
            code = code,
            onCodeChange = { code = it }
        )
        if (state.otpError) {
            Text(
                state.authErrorMessage ?: stringResource(Res.string.otp_wrong_code),
                fontSize = 12.sp,
                color = Orange,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        PrimaryButton(
            if (state.authLoading) stringResource(Res.string.checking) else stringResource(Res.string.confirm),
            onClick = { state.verifyOtp(code) },
            enabled = code.length == 4 && !state.authLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
        Row(Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                stringResource(Res.string.change_number),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.clickable { state.go(Screen.AUTH_PHONE) }
            )
        }
    }
}

@Composable
fun AuthProfileScreen(state: AppState) {
    var name by remember { mutableStateOf(state.userName) }

    Column(Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp, vertical = 32.dp)) {
        AuthHeader(
            title = stringResource(Res.string.auth_profile_title),
            subtitle = stringResource(Res.string.auth_profile_subtitle),
            onBack = { state.go(Screen.AUTH_OTP) }
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(stringResource(Res.string.name_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        PrimaryButton(
            stringResource(Res.string.continue_label),
            onClick = { state.completeAuth(name.trim()) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
    }
}
