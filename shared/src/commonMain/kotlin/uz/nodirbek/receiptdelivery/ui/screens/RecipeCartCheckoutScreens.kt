package uz.nodirbek.receiptdelivery.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.back
import uz.nodirbek.receiptdelivery.shared.resources.cancel
import uz.nodirbek.receiptdelivery.shared.resources.cart_title
import uz.nodirbek.receiptdelivery.shared.resources.change
import uz.nodirbek.receiptdelivery.shared.resources.checkout_title
import uz.nodirbek.receiptdelivery.shared.resources.comment_placeholder
import uz.nodirbek.receiptdelivery.shared.resources.comment_to_courier
import uz.nodirbek.receiptdelivery.shared.resources.composition
import uz.nodirbek.receiptdelivery.shared.resources.cooking_steps_label
import uz.nodirbek.receiptdelivery.shared.resources.delivery
import uz.nodirbek.receiptdelivery.shared.resources.delivery_address_label
import uz.nodirbek.receiptdelivery.shared.resources.delivery_slot_label
import uz.nodirbek.receiptdelivery.shared.resources.ingredients_cost
import uz.nodirbek.receiptdelivery.shared.resources.items_label
import uz.nodirbek.receiptdelivery.shared.resources.minutes_short
import uz.nodirbek.receiptdelivery.shared.resources.order_ingredients
import uz.nodirbek.receiptdelivery.shared.resources.pay_and_order
import uz.nodirbek.receiptdelivery.shared.resources.payment_method_label
import uz.nodirbek.receiptdelivery.shared.resources.placing_order
import uz.nodirbek.receiptdelivery.shared.resources.place_order_label
import uz.nodirbek.receiptdelivery.shared.resources.portions
import uz.nodirbek.receiptdelivery.shared.resources.remove_have_at_home
import uz.nodirbek.receiptdelivery.shared.resources.show_only_missing
import uz.nodirbek.receiptdelivery.shared.resources.start_cooking_label
import uz.nodirbek.receiptdelivery.shared.resources.steps_count
import uz.nodirbek.receiptdelivery.shared.resources.substituted_note
import uz.nodirbek.receiptdelivery.shared.resources.sum
import uz.nodirbek.receiptdelivery.shared.resources.total_label
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.paymentOptions
import uz.nodirbek.receiptdelivery.ui.slotOptions
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.components.PrimaryButton
import uz.nodirbek.receiptdelivery.ui.components.DeliveryMap
import uz.nodirbek.receiptdelivery.ui.components.RecipeDetailSkeleton
import uz.nodirbek.receiptdelivery.data.imageResFor
import uz.nodirbek.receiptdelivery.data.money
import uz.nodirbek.receiptdelivery.ui.theme.Amber
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.Green
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.OrangeTint
import uz.nodirbek.receiptdelivery.ui.theme.PageBg
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

private fun heroBrush2(colors: Pair<Long, Long>) = Brush.linearGradient(
    listOf(Color(colors.first), Color(colors.second))
)

@Composable
fun RecipeDetailScreen(state: AppState) {
    val recipe = state.recipe
    if (recipe.name.isBlank()) {
        if (state.recipesError != null) {
            Box(Modifier.fillMaxSize().background(Surface), contentAlignment = Alignment.Center) {
                Text(state.recipesError!!, fontSize = 14.sp, color = TextMuted)
            }
        } else {
            Box(Modifier.fillMaxSize().background(Surface)) {
                RecipeDetailSkeleton()
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Surface)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(220.dp)) {
                    Box(Modifier.fillMaxSize().background(heroBrush2(recipe.heroColors)), contentAlignment = Alignment.Center) {
                        if (recipe.imageUrl != null) {
                            AsyncImage(
                                model = recipe.imageUrl,
                                contentDescription = recipe.name,
                                modifier = Modifier.fillMaxSize().padding(36.dp),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(imageResFor(recipe.imageKey)),
                                error = painterResource(imageResFor(recipe.imageKey))
                            )
                        } else {
                            Image(
                                painter = painterResource(imageResFor(recipe.imageKey)),
                                contentDescription = recipe.name,
                                modifier = Modifier.fillMaxSize().padding(36.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Box(
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clip(CircleShape)
                            .clickable { state.go(Screen.HOME) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Text(recipe.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                        val minutesShort = stringResource(Res.string.minutes_short)
                        listOf(recipe.cuisine, "⏱ ${recipe.timeMinutes} $minutesShort", "★ ${recipe.rating} (${recipe.reviews})").forEach {
                            Box(Modifier.background(Border, RoundedCornerShape(24.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text(it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.portions), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text("${state.cartTotalLabel()} ${stringResource(Res.string.sum)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepperButton("−") { state.portions = maxOf(1, state.portions - 1) }
                            Slider(
                                value = state.portions.toFloat(),
                                onValueChange = { state.portions = it.toInt() },
                                valueRange = 1f..6f,
                                steps = 4,
                                colors = SliderDefaults.colors(thumbColor = Orange, activeTrackColor = Orange, inactiveTrackColor = Border),
                                modifier = Modifier.weight(1f)
                            )
                            StepperButton("+") { state.portions = minOf(6, state.portions + 1) }
                            Text(state.portions.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.width(20.dp))
                        }
                    }
                    Text(stringResource(Res.string.composition), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(top = 20.dp, bottom = 12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.scaledIngredients().forEach { ing ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(CardWhite, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(34.dp).background(PageBg, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Text(ing.initial, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted) }
                                Column(Modifier.weight(1f)) {
                                    Text("${ing.name}, ${ing.qtyLabel}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                    Text(ing.statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ing.statusColor, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                    Text(stringResource(Res.string.cooking_steps_label), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(top = 20.dp, bottom = 12.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { state.stepsOpen = !state.stepsOpen }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.steps_count, recipe.steps.size), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text(if (state.stepsOpen) "▲" else "▼", fontSize = 14.sp, color = TextMuted)
                    }
                    if (state.stepsOpen) {
                        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recipe.steps.forEachIndexed { i, s ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(CardWhite, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text("${i + 1}. ${s.text}", fontSize = 13.sp, color = TextDark, lineHeight = 19.sp)
                                }
                            }
                        }
                    }
                    PrimaryButton(
                        stringResource(Res.string.start_cooking_label),
                        onClick = { state.startCooking() },
                        color = Green,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
            }
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CardWhite)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.ingredients_cost), fontSize = 13.sp, color = TextMuted)
                Text("${state.cartTotalLabel()} ${stringResource(Res.string.sum)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            PrimaryButton(
                stringResource(Res.string.order_ingredients),
                onClick = { state.goToCart() },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .background(CardWhite, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, color = TextDark)
    }
}

@Composable
fun CartScreen(state: AppState) {
    LaunchedEffect(state.selectedDistrict) { state.refreshDeliveryFee() }
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(Screen.RECIPE) })
            Text(stringResource(Res.string.cart_title, state.recipe.name), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        }
        state.cartSyncError?.let { error ->
            Text(
                error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Amber,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(Res.string.show_only_missing), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Switch(
                checked = state.onlyMissing,
                onCheckedChange = { state.onlyMissing = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Green, uncheckedTrackColor = Border, checkedThumbColor = CardWhite)
            )
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.buildCartRows(), key = { it.key }) { row ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            Modifier.size(36.dp).background(PageBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text(row.initial, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted) }
                        Column(Modifier.weight(1f)) {
                            Text("${row.name}, ${row.qtyLabel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(row.packLabel, fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                            if (row.substituted) {
                                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(stringResource(Res.string.substituted_note, row.subNote), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Amber)
                                    Text(stringResource(Res.string.cancel), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Orange, modifier = Modifier.clickable { state.undoSub(row.key) })
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${row.priceLabel} ${stringResource(Res.string.sum)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
                            Row(
                                Modifier.background(PageBg, RoundedCornerShape(16.dp)).padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("−", fontSize = 14.sp, color = TextDark, modifier = Modifier.clickable { state.decCartQty(row.key) }.padding(horizontal = 4.dp))
                                Text(row.count.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("+", fontSize = 14.sp, color = TextDark, modifier = Modifier.clickable { state.incCartQty(row.key) }.padding(horizontal = 4.dp))
                            }
                        }
                    }
                    Text(
                        stringResource(Res.string.remove_have_at_home),
                        fontSize = 12.sp, color = TextMuted,
                        modifier = Modifier.padding(top = 8.dp).clickable { state.removeCartItem(row.key) }
                    )
                }
            }
        }
        Column(Modifier.background(CardWhite).padding(horizontal = 20.dp, vertical = 16.dp)) {
            val sumLabel = stringResource(Res.string.sum)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.items_label), fontSize = 14.sp, color = TextMuted)
                Text("${money(state.cartSubtotal())} $sumLabel", fontSize = 14.sp, color = TextMuted)
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.delivery), fontSize = 14.sp, color = TextMuted)
                Text("${money(state.deliveryFee())} $sumLabel", fontSize = 14.sp, color = TextMuted)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.total_label), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text("${money(state.cartGrandTotal())} $sumLabel", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            }
            PrimaryButton(stringResource(Res.string.place_order_label), onClick = { state.go(Screen.CHECKOUT) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(Screen.CART) })
            Text(stringResource(Res.string.checkout_title), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    SectionLabel(stringResource(Res.string.delivery_address_label))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                state.deliveryAddressLabel(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                maxLines = 2
                            )
                        }
                        Text(
                            stringResource(Res.string.change),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Orange,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.clickable { state.openAddressList(Screen.CHECKOUT) }
                        )
                    }
                    val addressPoint = state.deliveryDisplayPoint()
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        DeliveryMap(
                            modifier = Modifier.fillMaxSize(),
                            center = addressPoint,
                            zoom = 15f,
                            markerAt = addressPoint
                        )
                    }
                }
            }
            item {
                Column {
                    SectionLabel(stringResource(Res.string.delivery_slot_label))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        slotOptions().forEach { (id, label) ->
                            val selected = state.slot == id
                            Box(
                                Modifier
                                    .background(if (selected) OrangeTint else CardWhite, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .then(Modifier)
                                    .padding(1.dp)
                                    .clickable { state.slot = id }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Orange else TextDark)
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    SectionLabel(stringResource(Res.string.payment_method_label))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        paymentOptions().forEach { (id, label) ->
                            val selected = state.payment == id
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(CardWhite, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { state.payment = id }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                Box(
                                    Modifier
                                        .size(18.dp)
                                        .background(if (selected) Orange else Color.Transparent, CircleShape)
                                        .then(Modifier)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    SectionLabel(stringResource(Res.string.comment_to_courier))
                    OutlinedTextField(
                        value = state.comment,
                        onValueChange = { state.comment = it },
                        placeholder = { Text(stringResource(Res.string.comment_placeholder)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Border,
                            focusedBorderColor = Orange,
                            unfocusedContainerColor = CardWhite,
                            focusedContainerColor = CardWhite
                        ),
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }
            }
        }
        Column(Modifier.background(CardWhite).padding(horizontal = 20.dp, vertical = 14.dp)) {
            state.checkoutError?.let { error ->
                Text(
                    error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Amber,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            PrimaryButton(
                if (state.placingOrder) stringResource(Res.string.placing_order) else stringResource(Res.string.pay_and_order, money(state.cartGrandTotal())),
                onClick = { if (!state.placingOrder) state.placeOrder() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
