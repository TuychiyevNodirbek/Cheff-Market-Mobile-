package uz.nodirbek.receiptdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.components.GhostTextButton
import uz.nodirbek.receiptdelivery.ui.components.PlaceholderBlock
import uz.nodirbek.receiptdelivery.ui.components.PrimaryButton
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

@Composable
fun Onboarding1Screen(state: AppState) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            PlaceholderBlock(
                modifier = Modifier.aspectRatio(1f).padding(20.dp),
                label = "иллюстрация:\nблюдо + корзина",
                color1 = uz.nodirbek.receiptdelivery.ui.theme.Border,
                color2 = uz.nodirbek.receiptdelivery.ui.theme.OrangeTint
            )
        }
        Text(
            "Выбери блюдо — получи именно то, что нужно",
            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextDark,
            lineHeight = 32.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "Никаких лишних покупок. Мы соберём точный набор ингредиентов под рецепт и привезём его домой.",
            fontSize = 15.sp, color = TextMuted, lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 28.dp)
        )
        Box(Modifier.fillMaxWidth().padding(bottom = 24.dp), contentAlignment = Alignment.Center) {
            DotsIndicator(active = 0)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GhostTextButton("Пропустить", onClick = { state.go(Screen.HOME) })
            PrimaryButton("Далее", onClick = { state.go(Screen.ONB2) }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
fun Onboarding2Screen(state: AppState) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            PlaceholderBlock(
                modifier = Modifier.aspectRatio(1f).padding(20.dp),
                label = "иллюстрация:\nкурьер с пакетами",
                color1 = uz.nodirbek.receiptdelivery.ui.theme.Border,
                color2 = uz.nodirbek.receiptdelivery.ui.theme.Green.copy(alpha = 0.25f)
            )
        }
        Text(
            "Если чего-то нет — мы сразу скажем",
            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextDark,
            lineHeight = 32.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "Прозрачные замены и остатки на складе — никаких сюрпризов при оплате.",
            fontSize = 15.sp, color = TextMuted, lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 28.dp)
        )
        Box(Modifier.fillMaxWidth().padding(bottom = 24.dp), contentAlignment = Alignment.Center) {
            DotsIndicator(active = 1)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GhostTextButton("Назад", onClick = { state.go(Screen.ONB1) })
            PrimaryButton("Продолжить", onClick = { state.go(Screen.AUTH_PHONE) }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun DotsIndicator(active: Int, count: Int = 2) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0 until count) {
            val isActive = i == active
            Box(
                Modifier
                    .size(width = if (isActive) 20.dp else 6.dp, height = 6.dp)
                    .background(if (isActive) Orange else Border, RoundedCornerShape(3.dp))
            )
        }
    }
}
