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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.back_arrow
import uz.nodirbek.receiptdelivery.shared.resources.delivery
import uz.nodirbek.receiptdelivery.shared.resources.delivery_address_label
import uz.nodirbek.receiptdelivery.shared.resources.done
import uz.nodirbek.receiptdelivery.shared.resources.done_appetite
import uz.nodirbek.receiptdelivery.shared.resources.favorite_recipes
import uz.nodirbek.receiptdelivery.shared.resources.guest
import uz.nodirbek.receiptdelivery.shared.resources.items_label
import uz.nodirbek.receiptdelivery.shared.resources.loading_order
import uz.nodirbek.receiptdelivery.shared.resources.loading_orders
import uz.nodirbek.receiptdelivery.shared.resources.my_addresses
import uz.nodirbek.receiptdelivery.shared.resources.next_arrow
import uz.nodirbek.receiptdelivery.shared.resources.no_orders_yet
import uz.nodirbek.receiptdelivery.shared.resources.order_composition
import uz.nodirbek.receiptdelivery.shared.resources.order_history_label
import uz.nodirbek.receiptdelivery.shared.resources.order_not_found
import uz.nodirbek.receiptdelivery.shared.resources.order_number
import uz.nodirbek.receiptdelivery.shared.resources.order_word
import uz.nodirbek.receiptdelivery.shared.resources.phone_not_specified
import uz.nodirbek.receiptdelivery.shared.resources.rate_dish
import uz.nodirbek.receiptdelivery.shared.resources.servings_abbrev
import uz.nodirbek.receiptdelivery.shared.resources.settings_label
import uz.nodirbek.receiptdelivery.shared.resources.step_of
import uz.nodirbek.receiptdelivery.shared.resources.sum
import uz.nodirbek.receiptdelivery.shared.resources.total_label
import uz.nodirbek.receiptdelivery.data.money
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.orderStatusIsFinal
import uz.nodirbek.receiptdelivery.ui.orderStatusLabel
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.components.IconTapButton
import uz.nodirbek.receiptdelivery.ui.components.PlaceholderBlock
import uz.nodirbek.receiptdelivery.ui.components.PrimaryButton
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
    val order = state.currentOrder
    LaunchedEffect(order?.id) {
        // Refresh from the server rather than trusting whatever's still in memory from checkout -
        // this is the one screen meant to show the order's real, current status.
        order?.id?.let { state.loadOrderDetail(it) }
    }
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (order != null) stringResource(Res.string.order_number, order.number) else stringResource(Res.string.order_word),
                fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark
            )
        }
        if (order == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    state.currentOrderError ?: if (state.currentOrderLoading) stringResource(Res.string.loading_order) else stringResource(Res.string.order_not_found),
                    fontSize = 14.sp, color = TextMuted
                )
            }
            return
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .background(if (orderStatusIsFinal(order.status)) Green.copy(alpha = 0.15f) else OrangeTint, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    orderStatusLabel(order.status), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (orderStatusIsFinal(order.status)) Green else Orange
                )
            }
        }
        PlaceholderBlock(
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp),
            label = order.addressText.ifBlank { stringResource(Res.string.delivery_address_label) },
            color1 = Border,
            color2 = OrangeTint
        )
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.order_composition), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            items(order.recipes, key = { "recipe-${it.id}" }) { recipe ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(CardWhite, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(recipe.recipeTitle, fontSize = 13.sp, color = TextDark)
                    Text("${recipe.servings} ${stringResource(Res.string.servings_abbrev)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            items(order.items, key = { "item-${it.id}" }) { orderItem ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(CardWhite, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${orderItem.productName}, ${orderItem.quantityOrdered}", fontSize = 13.sp, color = TextDark)
                    Text("${money(orderItem.unitPrice.toDoubleOrNull() ?: 0.0)} ${stringResource(Res.string.sum)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            item {
                val sumLabel = stringResource(Res.string.sum)
                Column(Modifier.padding(top = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(Res.string.items_label), fontSize = 13.sp, color = TextMuted)
                        Text("${money(order.amountProducts.toDoubleOrNull() ?: 0.0)} $sumLabel", fontSize = 13.sp, color = TextMuted)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(Res.string.delivery), fontSize = 13.sp, color = TextMuted)
                        Text("${money(order.amountDelivery.toDoubleOrNull() ?: 0.0)} $sumLabel", fontSize = 13.sp, color = TextMuted)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(Res.string.total_label), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("${money(order.amountAuthorized.toDoubleOrNull() ?: 0.0)} $sumLabel", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
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
                stringResource(Res.string.step_of, state.cookingDisplayIndex(), state.recipe.steps.size),
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
                PrimaryButton(stringResource(Res.string.back_arrow), onClick = { state.cookPrev() }, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f), modifier = Modifier.weight(1f))
                PrimaryButton(stringResource(Res.string.next_arrow), onClick = { state.cookNext() }, modifier = Modifier.weight(1f))
            }
        } else {
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 40.sp, modifier = Modifier.padding(bottom = 16.dp))
                Text(stringResource(Res.string.done_appetite), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(bottom = 8.dp))
                Text(stringResource(Res.string.rate_dish), fontSize = 14.sp, color = CookingMuted, modifier = Modifier.padding(bottom = 24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 28.dp)) {
                    for (n in 1..5) {
                        Text(
                            if (n <= state.rating) "★" else "☆",
                            fontSize = 28.sp,
                            modifier = Modifier.clickable { state.rating = n }
                        )
                    }
                }
                PrimaryButton(stringResource(Res.string.done), onClick = { state.go(Screen.RECIPE) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private data class ProfileItem(val label: String, val value: String, val onClick: (() -> Unit)?)

@Composable
fun ProfileScreen(state: AppState) {
    LaunchedEffect(Unit) {
        state.loadOrders()
        state.refreshProfile()
    }
    val favCount = state.favs.values.count { it }
    val profileItems = listOf(
        ProfileItem(stringResource(Res.string.my_addresses), state.savedAddresses.size.toString(), { state.openAddressList(Screen.PROFILE) }),
        ProfileItem(stringResource(Res.string.order_history_label), state.orders.size.toString(), { state.go(Screen.ORDER_HISTORY) }),
        ProfileItem(stringResource(Res.string.favorite_recipes), favCount.toString(), null),
        ProfileItem(stringResource(Res.string.settings_label), "", { state.go(Screen.SETTINGS) })
    )
    val displayName = state.userName.ifBlank { stringResource(Res.string.guest) }
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
                Text(state.userPhone.ifBlank { stringResource(Res.string.phone_not_specified) }, fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
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
    LaunchedEffect(Unit) { state.loadOrders() }
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(Screen.PROFILE) })
            Text(stringResource(Res.string.order_history_label), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        }
        when {
            state.ordersLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.loading_orders), fontSize = 14.sp, color = TextMuted)
            }
            state.ordersError != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(state.ordersError!!, fontSize = 14.sp, color = TextMuted)
            }
            state.orders.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.no_orders_yet), fontSize = 14.sp, color = TextMuted)
            }
            else -> LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.orders, key = { it.id }) { order ->
                    val isFinal = orderStatusIsFinal(order.status)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                state.loadOrderDetail(order.id)
                                state.go(Screen.TRACKING)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.order_number, order.number.toString()), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Box(
                                Modifier
                                    .background(if (isFinal) Green.copy(alpha = 0.15f) else OrangeTint, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(orderStatusLabel(order.status), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isFinal) Green else Orange)
                            }
                        }
                        Text(order.createdAt, fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 6.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sumLabel = stringResource(Res.string.sum)
                            Text("${stringResource(Res.string.items_label)}: ${money(order.amountProducts.toDoubleOrNull() ?: 0.0)} $sumLabel", fontSize = 12.sp, color = TextMuted)
                            Text("${money(order.amountAuthorized.toDoubleOrNull() ?: 0.0)} $sumLabel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}
