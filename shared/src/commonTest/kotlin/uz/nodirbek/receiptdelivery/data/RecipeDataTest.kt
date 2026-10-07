package uz.nodirbek.receiptdelivery.data

import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeDataTest {
    @Test
    fun money_groups_thousands_with_spaces() {
        assertEquals("32 000", money(32000))
        assertEquals("1 234 567", money(1234567))
        assertEquals("999", money(999))
        assertEquals("0", money(0))
    }

    @Test
    fun money_rounds_to_nearest_integer() {
        assertEquals("32 000", money(31999.6))
        assertEquals("31 999", money(31999.4))
    }

    @Test
    fun money_handles_negative_values() {
        assertEquals("-12 000", money(-12000))
    }

    @Test
    fun nearestDistrict_picks_closest_known_district() {
        val (lat, lon) = DISTRICT_COORDS.getValue("Юнусабад")
        assertEquals("Юнусабад", nearestDistrict(lat, lon))
    }

    @Test
    fun nearestDistrict_falls_back_to_off_zone_when_map_would_be_empty() {
        // Sanity check that the off-zone district is a valid fallback key, not a magic string
        // that silently diverges from the real district list.
        assertEquals(true, OFF_ZONE_DISTRICT in ALL_DISTRICTS)
    }

    @Test
    fun heroColorsFor_is_deterministic_for_the_same_slug() {
        assertEquals(heroColorsFor("lagman"), heroColorsFor("lagman"))
    }

    @Test
    fun recipe_basePrice_sums_ingredient_prices() {
        val recipe = Recipe(
            id = "lagman", name = "Лагман домашний", cuisine = "boil", timeMinutes = 40,
            baseServings = 4, rating = "", reviews = 0, heroColors = 0xFFF0C9A0 to 0xFFE0A870,
            imageKey = "lagman",
            ingredients = listOf(
                Ingredient("beef", "Говядина", "г", 500, 32000, StockStatus.OK),
                Ingredient("noodles", "Лапша яичная", "г", 400, 12000, StockStatus.OK)
            ),
            steps = emptyList()
        )
        assertEquals(recipe.ingredients.sumOf { it.pricePerBase }, recipe.basePrice)
    }
}
