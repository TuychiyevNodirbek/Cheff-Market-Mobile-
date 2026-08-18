package uz.nodirbek.receiptdelivery.data

import uz.nodirbek.receiptdelivery.R

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
    val imageRes: Int,
    val ingredients: List<Ingredient>,
    val steps: List<Step>
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

val RECIPES: Map<String, Recipe> = listOf(
    Recipe(
        id = "lagman",
        name = "Лагман домашний",
        cuisine = "Узбекская кухня",
        timeMinutes = 40,
        baseServings = 4,
        rating = "4.8",
        reviews = 214,
        heroColors = 0xFFF0C9A0 to 0xFFE0A870,
        imageRes = R.drawable.dish_lagman,
        ingredients = listOf(
            Ingredient("beef", "Говядина", "г", 500, 32000, StockStatus.OK),
            Ingredient("noodles", "Лапша яичная", "г", 400, 12000, StockStatus.OK),
            Ingredient("carrot", "Морковь", "г", 300, 4000, StockStatus.LOW),
            Ingredient("pepper", "Болгарский перец", "г", 250, 6000, StockStatus.SUBSTITUTED, "нет жёлтого — заменено на красный"),
            Ingredient("onion", "Лук репчатый", "г", 200, 2000, StockStatus.OK)
        ),
        steps = listOf(
            Step("Нарежьте говядину тонкими полосками и обжарьте до золотистой корочки."),
            Step("Добавьте лук, морковь и перец, тушите на среднем огне 10 минут.", 10),
            Step("Влейте бульон, доведите до кипения и варите 20 минут.", 20),
            Step("Отдельно отварите лапшу до готовности, 8 минут.", 8),
            Step("Разложите лапшу по тарелкам, залейте лагманом. Приятного аппетита!")
        )
    ),
    Recipe(
        id = "plov",
        name = "Плов с говядиной",
        cuisine = "Узбекская кухня",
        timeMinutes = 60,
        baseServings = 6,
        rating = "4.9",
        reviews = 356,
        heroColors = 0xFFE8B870 to 0xFFD19040,
        imageRes = R.drawable.dish_plov,
        ingredients = listOf(
            Ingredient("rice", "Рис для плова", "г", 700, 14000, StockStatus.OK),
            Ingredient("beef2", "Говядина", "г", 600, 38000, StockStatus.OK),
            Ingredient("carrot2", "Морковь", "г", 400, 5000, StockStatus.OK),
            Ingredient("onion2", "Лук репчатый", "г", 200, 2000, StockStatus.LOW),
            Ingredient("oil", "Масло растительное", "мл", 150, 6000, StockStatus.OK)
        ),
        steps = listOf(
            Step("Обжарьте лук до золотистого цвета, добавьте говядину."),
            Step("Тушите мясо с морковью 15 минут.", 15),
            Step("Добавьте промытый рис и воду, накройте крышкой."),
            Step("Варите на медленном огне 25 минут, не открывая крышку.", 25)
        )
    ),
    Recipe(
        id = "shakshuka",
        name = "Шакшука",
        cuisine = "Средиземноморская",
        timeMinutes = 20,
        baseServings = 2,
        rating = "4.6",
        reviews = 98,
        heroColors = 0xFFE89060 to 0xFFD06838,
        imageRes = R.drawable.dish_shakshuka,
        ingredients = listOf(
            Ingredient("eggs", "Яйца", "шт", 6, 9000, StockStatus.OK),
            Ingredient("tomato", "Томаты", "г", 400, 6000, StockStatus.OK),
            Ingredient("pepper2", "Болгарский перец", "г", 200, 4000, StockStatus.SUBSTITUTED, "заменено на острый перец"),
            Ingredient("onion3", "Лук репчатый", "г", 100, 1500, StockStatus.OK)
        ),
        steps = listOf(
            Step("Обжарьте лук и перец до мягкости."),
            Step("Добавьте томаты, тушите 10 минут до соуса.", 10),
            Step("Сделайте углубления, вбейте яйца."),
            Step("Готовьте под крышкой 6 минут до желаемой готовности яиц.", 6)
        )
    ),
    Recipe(
        id = "manty",
        name = "Манты классические",
        cuisine = "Узбекская кухня",
        timeMinutes = 90,
        baseServings = 4,
        rating = "4.7",
        reviews = 152,
        heroColors = 0xFFE8D0A0 to 0xFFC9A868,
        imageRes = R.drawable.dish_manty,
        ingredients = listOf(
            Ingredient("flour", "Тесто (мука)", "г", 500, 4000, StockStatus.OK),
            Ingredient("lamb", "Говядина/баранина фарш", "г", 600, 34000, StockStatus.LOW),
            Ingredient("onion4", "Лук репчатый", "г", 300, 3000, StockStatus.OK),
            Ingredient("pumpkin", "Тыква", "г", 200, 2500, StockStatus.OK)
        ),
        steps = listOf(
            Step("Замесите тесто и оставьте отдохнуть 20 минут.", 20),
            Step("Смешайте фарш с луком и тыквой, приправьте."),
            Step("Раскатайте тесто, сформируйте манты с начинкой."),
            Step("Готовьте на пару 40 минут.", 40)
        )
    )
).associateBy { it.id }

fun money(n: Number): String {
    val rounded = Math.round(n.toDouble())
    return String.format(java.util.Locale.US, "%,d", rounded).replace(',', ' ')
}
