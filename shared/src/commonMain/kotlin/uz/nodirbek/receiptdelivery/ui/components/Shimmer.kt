package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite

/**
 * Instagram/Facebook-style shimmering skeleton: a soft highlight band sweeps left-to-right
 * over a muted base color, looping indefinitely while [Modifier.shimmer] is applied.
 */
@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Border, CardWhite, Border),
        start = Offset(translate - 300f, 0f),
        end = Offset(translate, 300f)
    )
    return this.background(brush)
}

@Composable
private fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(modifier.clip(shape).shimmer())
}

/** Skeleton for the horizontal "collections" row on the home screen. */
@Composable
fun CollectionCardSkeleton() {
    Column(
        Modifier
            .width(140.dp)
            .background(CardWhite, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        ShimmerBlock(Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(0.dp))
        Column(Modifier.padding(10.dp)) {
            ShimmerBlock(Modifier.fillMaxWidth().height(13.dp))
            ShimmerBlock(Modifier.padding(top = 6.dp).width(48.dp).height(11.dp))
        }
    }
}

/** Skeleton for a feed recipe card on the home screen. */
@Composable
fun FeedCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        ShimmerBlock(Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(0.dp))
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
            ShimmerBlock(Modifier.fillMaxWidth(0.6f).height(16.dp))
            ShimmerBlock(Modifier.padding(top = 8.dp).fillMaxWidth(0.4f).height(12.dp))
        }
    }
}

/** Skeleton for a single ingredient row on the recipe detail screen. */
@Composable
private fun IngredientRowSkeleton() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBlock(Modifier.size(36.dp), shape = CircleShape)
        Column(Modifier.weight(1f)) {
            ShimmerBlock(Modifier.fillMaxWidth(0.5f).height(13.dp))
            ShimmerBlock(Modifier.padding(top = 6.dp).fillMaxWidth(0.3f).height(11.dp))
        }
    }
}

/** Full recipe-detail skeleton: hero image, title, tag chips, and a few ingredient rows. */
@Composable
fun RecipeDetailSkeleton() {
    Column(Modifier.fillMaxWidth().padding(bottom = 100.dp)) {
        ShimmerBlock(Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(0.dp))
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            ShimmerBlock(Modifier.fillMaxWidth(0.7f).height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)) {
                repeat(3) { ShimmerBlock(Modifier.width(72.dp).height(28.dp), shape = RoundedCornerShape(24.dp)) }
            }
            ShimmerBlock(Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(16.dp))
            ShimmerBlock(Modifier.padding(top = 20.dp, bottom = 12.dp).width(80.dp).height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { IngredientRowSkeleton() }
            }
        }
    }
}
