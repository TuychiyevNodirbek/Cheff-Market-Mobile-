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

@Composable
fun AuthPhoneScreen(state: AppState) {
    var phone by remember { mutableStateOf(state.userPhone.ifBlank { "+998 " }) }

    Column(Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp, vertical = 32.dp)) {
        AuthHeader(
            title = "Введите номер телефона",
            subtitle = "Мы отправим код подтверждения по SMS",
            onBack = { state.go(Screen.ONB2) }
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("+998 90 123 45 67") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        val digitsOnly = phone.filter { it.isDigit() }
        PrimaryButton(
            "Получить код",
            onClick = { state.submitPhone(phone.trim()) },
            enabled = digitsOnly.length >= 9,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
    }
}

@Composable
fun AuthOtpScreen(state: AppState) {
    var code by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp, vertical = 32.dp)) {
        AuthHeader(
            title = "Введите код из SMS",
            subtitle = "Код отправлен на ${state.userPhone}",
            onBack = { state.go(Screen.AUTH_PHONE) }
        )
        OtpCodeField(
            code = code,
            onCodeChange = { code = it }
        )
        Text(
            "Демо-режим: подойдёт любой 4-значный код",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 10.dp)
        )
        if (state.otpError) {
            Text(
                "Код должен содержать 4 цифры",
                fontSize = 12.sp,
                color = Orange,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        PrimaryButton(
            "Подтвердить",
            onClick = { state.verifyOtp(code) },
            enabled = code.length == 4,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
        Row(Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "Изменить номер",
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
            title = "Как вас зовут?",
            subtitle = "Это имя увидит курьер при доставке",
            onBack = { state.go(Screen.AUTH_OTP) }
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Имя и фамилия") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        PrimaryButton(
            "Продолжить",
            onClick = { state.completeAuth(name.trim()) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
    }
}
