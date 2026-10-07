package uz.nodirbek.receiptdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.call_support
import uz.nodirbek.receiptdelivery.shared.resources.default_payment_section
import uz.nodirbek.receiptdelivery.shared.resources.dietary_prefs_section
import uz.nodirbek.receiptdelivery.shared.resources.language_section
import uz.nodirbek.receiptdelivery.shared.resources.logout
import uz.nodirbek.receiptdelivery.shared.resources.notifications_section
import uz.nodirbek.receiptdelivery.shared.resources.order_status_notif
import uz.nodirbek.receiptdelivery.shared.resources.settings_title
import uz.nodirbek.receiptdelivery.shared.resources.support_section
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.components.rememberPhoneDialer
import uz.nodirbek.receiptdelivery.ui.DIETARY_OPTION_IDS
import uz.nodirbek.receiptdelivery.ui.LANGUAGE_OPTIONS
import uz.nodirbek.receiptdelivery.ui.dietaryOptionLabel
import uz.nodirbek.receiptdelivery.ui.paymentOptions
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.Green
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.OrangeTint
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

private const val SUPPORT_PHONE = "+998781500000"

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(14.dp))
            .padding(horizontal = 4.dp),
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(state: AppState) {
    val dialPhone = rememberPhoneDialer()

    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(Screen.PROFILE) })
            Text(stringResource(Res.string.settings_title), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    SectionTitle(stringResource(Res.string.notifications_section))
                    SettingsCard {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(Res.string.order_status_notif), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = { state.notificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Green, uncheckedTrackColor = Border, checkedThumbColor = CardWhite)
                            )
                        }
                    }
                }
            }
            item {
                Column {
                    SectionTitle(stringResource(Res.string.default_payment_section))
                    SettingsCard {
                        val payment = paymentOptions()
                        payment.forEachIndexed { i, (id, label) ->
                            val selected = state.payment == id
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { state.payment = id }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                if (selected) Text("✓", color = Orange, fontWeight = FontWeight.Bold)
                            }
                            if (i != payment.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(color = Border)
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    SectionTitle(stringResource(Res.string.language_section))
                    SettingsCard {
                        LANGUAGE_OPTIONS.forEachIndexed { i, (code, label) ->
                            val selected = state.language == code
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { state.language = code }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                if (selected) Text("✓", color = Orange, fontWeight = FontWeight.Bold)
                            }
                            if (i != LANGUAGE_OPTIONS.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(color = Border)
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    SectionTitle(stringResource(Res.string.dietary_prefs_section))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DIETARY_OPTION_IDS.forEach { id ->
                            val active = state.dietaryPrefs.contains(id)
                            androidx.compose.foundation.layout.Box(
                                Modifier
                                    .background(if (active) OrangeTint else CardWhite, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { state.toggleDietaryPref(id) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(dietaryOptionLabel(id), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (active) Orange else TextDark)
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    SectionTitle(stringResource(Res.string.support_section))
                    SettingsCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dialPhone(SUPPORT_PHONE)
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stringResource(Res.string.call_support), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                Text(SUPPORT_PHONE, fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                            }
                            Text("📞", fontSize = 16.sp)
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(Res.string.logout),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Orange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { state.logout() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}
