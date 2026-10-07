package uz.nodirbek.receiptdelivery.data

import kotlin.math.abs
import kotlin.math.roundToLong

data class Step(
    val text: String,
    val timerMinutes: Int? = null
)

enum class StockStatus { OK, LOW, SUBSTITUTED }

data class Ingredient(
    val key: String,
    val name: String,
    val unit: String,
    val baseQty: Int,
    val pricePerBase: Int,
    val status: StockStatus,
    val note: String = ""
)

data class Recipe(
    val id: String,
    val name: String,
    val cuisine: String,
    val timeMinutes: Int,
    val baseServings: Int,
    val rating: String,
    val reviews: Int,
    val heroColors: Pair<Long, Long>,
    /** Platform-agnostic lookup key for the dish image; resolved to an actual drawable/asset per platform. */
    val imageKey: String,
    /** Absolute URL for the recipe's real photo, resolved via [uz.nodirbek.receiptdelivery.data.resolveImageUrl].
     *  Null when the backend sent no image, in which case the UI shows [imageKey]'s placeholder instead. */
    val imageUrl: String? = null,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    /** The backend's numeric recipe id (`ApiRecipeDetail.id`), needed to add this recipe to the
     *  server cart via `POST /cart/recipes/`. 0 for a recipe that hasn't been loaded from the API yet. */
    val serverId: Int = 0
) {
    val basePrice: Int get() = ingredients.sumOf { it.pricePerBase }
}

val DISTRICTS = listOf("Юнусабад", "Мирзо-Улугбек", "Чиланзар", "Яккасарай", "Мирабад")
const val OFF_ZONE_DISTRICT = "Сергели"
val ALL_DISTRICTS = DISTRICTS + OFF_ZONE_DISTRICT

val DISTRICT_COORDS: Map<String, Pair<Double, Double>> = mapOf(
    "Юнусабад" to (41.3560 to 69.2880),
    "Мирзо-Улугбек" to (41.3350 to 69.3230),
    "Чиланзар" to (41.2830 to 69.2040),
    "Яккасарай" to (41.2950 to 69.2560),
    "Мирабад" to (41.2950 to 69.2830),
    "Сергели" to (41.2270 to 69.2350)
)

/** Nearest known district to a lat/lon, by simple squared distance (fine at city scale). */
fun nearestDistrict(lat: Double, lon: Double): String {
    return DISTRICT_COORDS.minByOrNull { (_, coord) ->
        val (dLat, dLon) = coord.first - lat to coord.second - lon
        dLat * dLat + dLon * dLon
    }?.key ?: OFF_ZONE_DISTRICT
}

/** Recipes now come from `GET /recipes/` (see [uz.nodirbek.receiptdelivery.ui.CartState]) - the
 *  backend doesn't send hero-gradient colors, so cards/detail pick a deterministic one from here
 *  based on the recipe's slug instead. */
val RECIPE_HERO_PALETTE: List<Pair<Long, Long>> = listOf(
    0xFFF0C9A0 to 0xFFE0A870,
    0xFFE8B870 to 0xFFD19040,
    0xFFE89060 to 0xFFD06838,
    0xFFE8D0A0 to 0xFFC9A868,
    0xFFB8D8C8 to 0xFF88B8A0,
    0xFFD8C0E8 to 0xFFB090D0
)

fun heroColorsFor(slug: String): Pair<Long, Long> =
    RECIPE_HERO_PALETTE[abs(slug.hashCode()) % RECIPE_HERO_PALETTE.size]

/** Groups digits with a space every three places, e.g. 32000 -> "32 000". Multiplatform-safe (no java.text). */
fun money(n: Number): String {
    val rounded = n.toDouble().roundToLong()
    val digits = abs(rounded).toString()
    val sb = StringBuilder()
    for ((i, c) in digits.withIndex()) {
        val posFromEnd = digits.length - i
        if (i != 0 && posFromEnd % 3 == 0) sb.append(' ')
        sb.append(c)
    }
    return if (rounded < 0) "-$sb" else sb.toString()
}
