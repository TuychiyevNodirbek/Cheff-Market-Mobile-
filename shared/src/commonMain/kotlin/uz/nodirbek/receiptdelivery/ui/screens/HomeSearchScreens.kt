package uz.nodirbek.receiptdelivery.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.RecipeCard
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.OrangeTint
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

private val CHIP_LABELS = listOf("Узбекская кухня", "До 30 мин", "Бюджетно", "Вегетарианское", "Популярное")

private fun heroBrush(colors: Pair<Long, Long>) = Brush.linearGradient(
    listOf(Color(colors.first), Color(colors.second))
)

@Composable
private fun DishHero(
    r: RecipeCard,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    imagePadding: Dp = 16.dp
) {
    Box(
        modifier
            .background(heroBrush(r.heroColors), shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(r.imageRes),
            contentDescription = r.name,
            modifier = Modifier.fillMaxSize().padding(imagePadding),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun HomeScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .background(CardWhite, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { state.go(Screen.ADDRESSES) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍", fontSize = 16.sp)
                Column(Modifier.weight(1f)) {
                    Text("Доставка", fontSize = 11.sp, color = TextMuted)
                    Text(
                        state.selectedDistrict ?: "Выбрать адрес",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }
                Text("▾", fontSize = 14.sp, color = TextMuted)
            }
            Box(
                Modifier
                    .size(44.dp)
                    .background(CardWhite, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("☷", fontSize = 16.sp, color = TextDark)
            }
        }
        LazyRow(
            Modifier.padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CHIP_LABELS) { label ->
                val active = state.activeChips[label] == true
                Box(
                    Modifier
                        .background(if (active) OrangeTint else CardWhite, RoundedCornerShape(24.dp))
                        .then(Modifier)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { state.activeChips[label] = !active }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (active) Orange else TextDark)
                }
            }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp)
        ) {
            item {
                Text("Ужин за 20 минут", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(vertical = 12.dp))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                    items(state.collectionCards()) { r ->
                        CollectionCard(r, onClick = { state.selectRecipe(r.id) })
                    }
                }
            }
            item {
                Text("Рекомендуем сегодня", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 12.dp))
            }
            items(state.allRecipeCards()) { r ->
                FeedCard(
                    r,
                    onClick = { state.selectRecipe(r.id) },
                    onFav = { state.toggleFav(r.id) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CollectionCard(r: RecipeCard, onClick: () -> Unit) {
    Column(
        Modifier
            .width(140.dp)
            .background(CardWhite, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        DishHero(r, Modifier.fillMaxWidth().height(90.dp), imagePadding = 12.dp)
        Column(Modifier.padding(10.dp)) {
            Text(r.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 2)
            Text("${r.time} мин", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun FeedCard(r: RecipeCard, onClick: () -> Unit, onFav: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            DishHero(r, Modifier.fillMaxSize(), imagePadding = 32.dp)
            Box(
                Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onFav),
                contentAlignment = Alignment.Center
            ) {
                Text(if (r.isFav) "♥" else "♡", fontSize = 15.sp)
            }
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
            Text(r.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 6.dp))
            Text("⏱ ${r.time} мин  ·  🍽 ${r.baseServings} порции", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(bottom = 8.dp))
            Text("от ${r.priceLabel} сум", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
        }
    }
}

@Composable
fun SearchScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface)) {
        Box(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { state.searchQuery = it },
                placeholder = { Text("Название или ингредиент") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Orange,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.searchResults()) { r ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { state.selectRecipe(r.id) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DishHero(r, Modifier.size(56.dp), shape = RoundedCornerShape(10.dp), imagePadding = 6.dp)
                    Column(Modifier.weight(1f)) {
                        Text(r.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
                        Text("⏱ ${r.time} мин · от ${r.priceLabel} сум", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun AddressesScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface)) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(state.addressesReturnTarget) })
            Text("Мои адреса", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.savedAddresses.isEmpty()) {
                item {
                    Text(
                        "Сохранённых адресов пока нет",
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }
            items(state.savedAddresses, key = { it.id }) { addr ->
                val active = addr.id == state.activeAddressId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { state.selectSavedAddress(addr) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (active) "📍" else "🏠", fontSize = 16.sp)
                        Text(
                            addr.fullAddress.ifBlank { addr.district },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Orange else TextDark
                        )
                    }
                    Text(
                        "Удалить",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.clickable { state.removeAddress(addr) }
                    )
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { state.openLocationPicker(Screen.ADDRESSES, Screen.ADDRESSES, addNew = true) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Orange)
                    Text("Добавить адрес", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Orange)
                }
            }
        }
    }
}
