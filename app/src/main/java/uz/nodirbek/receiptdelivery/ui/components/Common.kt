package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

/**
 * A round icon tap target used for every back/close affordance in the app.
 * Fixed 40dp size so the ripple is contained to a proper touch target
 * regardless of the glyph's own bounds (a bare Text+clickable ripples the
 * tiny glyph box instead, and reads as an inconsistent, hard-to-hit control).
 */
@Composable
fun IconTapButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextDark,
    background: Color = Color.Transparent,
    size: Dp = 40.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    Box(
        modifier
            .size(size)
            .background(background, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = fontSize, color = tint, fontWeight = FontWeight.Bold)
    }
}

/** Same round tap target as [IconTapButton], but for a real vector icon instead of a text glyph. */
@Composable
fun IconTapButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextDark,
    background: Color = Color.Transparent,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null
) {
    Box(
        modifier
            .size(size)
            .background(background, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = TextDark) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Назад",
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Orange
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        modifier = modifier.height(52.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GhostTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = TextMuted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PlaceholderBlock(
    modifier: Modifier = Modifier,
    label: String,
    color1: Color,
    color2: Color
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(color1, color2)), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = Color(0x998A7F76),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Border, RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = uz.nodirbek.receiptdelivery.ui.theme.TextDark)
    }
}

@Composable
fun CardSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .background(CardWhite, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
    ) {
        content()
    }
}
