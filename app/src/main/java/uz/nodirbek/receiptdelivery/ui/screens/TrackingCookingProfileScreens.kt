package uz.nodirbek.receiptdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.STATUS_LABELS
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.components.IconTapButton
import uz.nodirbek.receiptdelivery.ui.components.PlaceholderBlock
import uz.nodirbek.receiptdelivery.ui.components.PrimaryButton
import uz.nodirbek.receiptdelivery.ui.components.YandexMapView
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.CookingBg
import uz.nodirbek.receiptdelivery.ui.theme.CookingMuted
import uz.nodirbek.receiptdelivery.ui.theme.Green
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.OrangeTint
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

@Composable
fun TrackingScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Заказ №${state.currentOrderId()}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Box(
                Modifier
                    .background(Border, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { state.advanceOrderStatus() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Симулировать →", fontSize = 12.sp, color = TextMuted)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            state.orderStatuses().forEach { st ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(28.dp).background(if (st.active) Green else Border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (st.active) "✓" else "${st.index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (st.active) androidx.compose.ui.graphics.Color.White else TextMuted)
                    }
                    Text(st.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (st.active) TextDark else TextMuted, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        if (state.orderStatusIdx >= 2) {
            val point = state.currentOrderPoint()
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                YandexMapView(center = point, zoom = 13f, markerAt = point)
            }
        } else {
            PlaceholderBlock(
                modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp),
                label = state.mapLabel(),
                color1 = Border,
                color2 = OrangeTint
            )
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { state.orderCardOpen = !state.orderCardOpen }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${state.recipe.name} · ${state.buildCartRows().size} позиции", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(if (state.orderCardOpen) "▲" else "▼", color = TextMuted)
                }
            }
            if (state.orderCardOpen) {
                items(state.buildCartRows(), key = { it.key }) { row ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .background(CardWhite, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${row.name}, ${row.qtyLabel}", fontSize = 13.sp, color = TextDark)
                        Text(row.priceLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(CardWhite, RoundedCornerShape(24.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💬 Связаться с курьером", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
        }
    }
}

@Composable
fun CookingScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(CookingBg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTapButton(
                imageVector = Icons.Filled.Close,
                onClick = { state.go(Screen.RECIPE) },
                tint = androidx.compose.ui.graphics.Color.White
            )
            Text(
                "Шаг ${state.cookingDisplayIndex()} из ${state.recipe.steps.size}",
                fontSize = 13.sp, color = CookingMuted, fontWeight = FontWeight.SemiBold
            )
        }
        if (!state.cookingDone()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 20.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    state.currentCookingStep()?.text ?: "",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White,
                    lineHeight = 39.sp, modifier = Modifier.padding(bottom = 32.dp)
                )
                if (state.currentCookingStep()?.timerMinutes != null) {
                    Box(
                        Modifier
                            .background(Orange, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { state.startTimer() }
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text("⏱ ${state.timerLabel()}", color = androidx.compose.ui.graphics.Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton("← Назад", onClick = { state.cookPrev() }, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f), modifier = Modifier.weight(1f))
                PrimaryButton("Далее →", onClick = { state.cookNext() }, modifier = Modifier.weight(1f))
            }
        } else {
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 40.sp, modifier = Modifier.padding(bottom = 16.dp))
                Text("Готово! Приятного аппетита", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(bottom = 8.dp))
                Text("Оцените блюдо и поделитесь фото", fontSize = 14.sp, color = CookingMuted, modifier = Modifier.padding(bottom = 24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 28.dp)) {
                    for (n in 1..5) {
                        Text(
                            if (n <= state.rating) "★" else "☆",
                            fontSize = 28.sp,
                            modifier = Modifier.clickable { state.rating = n }
                        )
                    }
                }
                PrimaryButton("Готово", onClick = { state.go(Screen.RECIPE) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private data class ProfileItem(val label: String, val value: String, val onClick: (() -> Unit)?)

@Composable
fun ProfileScreen(state: AppState) {
    val favCount = state.favs.values.count { it }
    val profileItems = listOf(
        ProfileItem("Мои адреса", state.savedAddresses.size.toString(), { state.openAddressList(Screen.PROFILE) }),
        ProfileItem("История заказов", state.orders.size.toString(), { state.go(Screen.ORDER_HISTORY) }),
        ProfileItem("Избранные рецепты", favCount.toString(), null),
        ProfileItem("Настройки", "", { state.go(Screen.SETTINGS) })
    )
    val displayName = state.userName.ifBlank { "Гость" }
    val initials = displayName.trim().split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(56.dp).background(Border, CircleShape), contentAlignment = Alignment.Center) {
                Text(initials.ifBlank { "?" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
            Column {
                Text(displayName, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text(state.userPhone.ifBlank { "Номер не указан" }, fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            items(profileItems) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .let { m -> if (item.onClick != null) m.clickable(onClick = item.onClick) else m }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text(if (item.value.isEmpty()) "›" else "${item.value} ›", fontSize = 13.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(Screen.PROFILE) })
            Text("История заказов", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        }
        if (state.orders.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Заказов пока нет", fontSize = 14.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.orders, key = { it.id }) { order ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Заказ №${order.id}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            val isDelivered = order.statusLabel == STATUS_LABELS.last()
                            Box(
                                Modifier
                                    .background(if (isDelivered) Green.copy(alpha = 0.15f) else OrangeTint, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(order.statusLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDelivered) Green else Orange)
                            }
                        }
                        Text(order.recipeName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark, modifier = Modifier.padding(top = 6.dp))
                        Text("📍 ${order.district}", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${order.dateLabel} · ${order.itemsSummary}", fontSize = 12.sp, color = TextMuted)
                            Text("${order.totalLabel} сум", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}
