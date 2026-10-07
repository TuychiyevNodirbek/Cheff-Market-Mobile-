package uz.nodirbek.receiptdelivery.ui

import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.Ingredient
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.Step
import uz.nodirbek.receiptdelivery.data.StockStatus
import uz.nodirbek.receiptdelivery.data.api.ApiRecipeListItem
import uz.nodirbek.receiptdelivery.ui.theme.Green
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** JVM-only (androidUnitTest, not commonTest): CartState carries androidx.compose.ui.graphics.Color
 *  (ScaledIngredient) and an Android drawable resId (RecipeCard.imageRes via imageResFor()), so it
 *  stays androidMain rather than commonMain - see CartState.kt's class doc.
 *
 *  The recipe catalog now comes from the backend (`GET /recipes/` / `GET /recipes/{slug}/`), so
 *  these tests seed CartState's internal caches directly ([CartState.recipeDetails]/
 *  [CartState.recipeSummaries]) instead of exercising real network calls. */
class CartStateTest {
    private val lagman = Recipe(
        id = "lagman",
        name = "Лагман домашний",
        cuisine = "boil",
        timeMinutes = 40,
        baseServings = 4,
        rating = "",
        reviews = 0,
        heroColors = 0xFFF0C9A0 to 0xFFE0A870,
        imageKey = "lagman",
        ingredients = listOf(
            Ingredient("beef", "Говядина", "г", 500, 32000, StockStatus.OK),
            Ingredient("noodles", "Лапша яичная", "г", 400, 12000, StockStatus.OK),
            Ingredient("carrot", "Морковь", "г", 300, 4000, StockStatus.OK)
        ),
        steps = listOf(Step("Шаг 1"), Step("Шаг 2", 10)),
        serverId = 1
    )
    private val plov = Recipe(
        id = "plov",
        name = "Плов с говядиной",
        cuisine = "fry",
        timeMinutes = 60,
        baseServings = 6,
        rating = "",
        reviews = 0,
        heroColors = 0xFFE8B870 to 0xFFD19040,
        imageKey = "plov",
        ingredients = listOf(Ingredient("rice", "Рис для плова", "г", 700, 14000, StockStatus.OK)),
        steps = listOf(Step("Шаг 1")),
        serverId = 2
    )

    private fun cartWith(vararg recipes: Recipe, selected: Recipe = recipes.first()): CartState {
        val cart = CartState()
        recipes.forEach { cart.recipeDetails[it.id] = it }
        cart.recipeId = selected.id
        cart.portions = selected.baseServings
        return cart
    }

    @Test
    fun scaleFactor_is_portions_over_base_servings() {
        val cart = cartWith(lagman).apply { portions = 8 }
        assertEquals(8.0 / lagman.baseServings, cart.scaleFactor())
    }

    @Test
    fun toggleFav_flips_and_defaults_to_false() {
        val cart = CartState()
        assertFalse(cart.favs["lagman"] ?: false)

        cart.toggleFav("lagman")
        assertTrue(cart.favs.getValue("lagman"))

        cart.toggleFav("lagman")
        assertFalse(cart.favs.getValue("lagman"))
    }

    @Test
    fun scaledIngredients_at_base_servings_uses_recipe_quantities_as_is() {
        val cart = cartWith(lagman) // portions == baseServings -> factor 1.0
        val scaled = cart.scaledIngredients("В наличии").associateBy { it.key }

        val beef = lagman.ingredients.single { it.key == "beef" }
        assertEquals("${beef.baseQty} ${beef.unit}", scaled.getValue("beef").qtyLabel)
    }

    @Test
    fun scaledIngredients_always_reads_as_available() {
        // The recipe endpoint carries no live stock data, so every line shows as available -
        // see CartState.scaledIngredients().
        val cart = cartWith(lagman)
        val scaled = cart.scaledIngredients("В наличии").associateBy { it.key }

        lagman.ingredients.forEach { ing ->
            assertEquals("В наличии" to Green, scaled.getValue(ing.key).let { it.statusLabel to it.statusColor })
        }
    }

    @Test
    fun scaledIngredients_scales_quantities_with_portions() {
        val cart = cartWith(lagman).apply { portions = lagman.baseServings * 2 }
        val beef = lagman.ingredients.single { it.key == "beef" }

        val scaledQty = cart.scaledIngredients("В наличии").single { it.key == "beef" }.qtyLabel
        assertEquals("${beef.baseQty * 2} ${beef.unit}", scaledQty)
    }

    @Test
    fun buildCartRows_excludes_removed_items() {
        val cart = cartWith(lagman)
        val keyToRemove = lagman.ingredients.first().key

        cart.removeCartItem(keyToRemove)

        assertFalse(cart.buildCartRows().any { it.key == keyToRemove })
        assertEquals(lagman.ingredients.size - 1, cart.buildCartRows().size)
    }

    @Test
    fun buildCartRows_defaults_count_to_one_and_multiplies_price() {
        val cart = cartWith(lagman)
        val ingredient = lagman.ingredients.first()

        val rowBefore = cart.buildCartRows().single { it.key == ingredient.key }
        assertEquals(1, rowBefore.count)

        cart.incCartQty(ingredient.key)
        val rowAfter = cart.buildCartRows().single { it.key == ingredient.key }
        assertEquals(2, rowAfter.count)
        assertEquals(rowBefore.price * 2, rowAfter.price)
    }

    @Test
    fun incCartQty_and_decCartQty_never_go_below_one() {
        val cart = cartWith(lagman)
        val key = lagman.ingredients.first().key

        cart.decCartQty(key)
        assertEquals(1, cart.cartQty[key])

        cart.incCartQty(key)
        cart.incCartQty(key)
        assertEquals(3, cart.cartQty[key])

        cart.decCartQty(key)
        assertEquals(2, cart.cartQty[key])
    }

    @Test
    fun cartSubtotal_and_deliveryFee_and_grandTotal() {
        val cart = cartWith(lagman)
        assertEquals(12000, cart.deliveryFee())
        assertEquals(cart.buildCartRows().sumOf { it.price }, cart.cartSubtotal())
        assertEquals(cart.cartSubtotal() + 12000, cart.cartGrandTotal())
    }

    @Test
    fun deliveryFeeOverride_takes_priority_over_the_placeholder() {
        val cart = cartWith(lagman).apply { deliveryFeeOverride = 5000 }
        assertEquals(5000, cart.deliveryFee())
    }

    @Test
    fun makeCard_maps_list_item_fields_and_reflects_favorite_state() {
        val item = ApiRecipeListItem(
            id = 1, title = "Лагман домашний", slug = "lagman", difficulty = "medium",
            cookingMethod = "boil", baseServings = 4, prepTimeMin = 15, cookTimeMin = 25, yieldWeightG = 1000
        )
        val cart = CartState()
        val cardBefore = cart.makeCard(item)
        assertFalse(cardBefore.isFav)
        assertEquals(item.slug, cardBefore.id)
        assertEquals(item.title, cardBefore.name)
        assertEquals(item.prepTimeMin + item.cookTimeMin, cardBefore.time)
        assertEquals(item.baseServings, cardBefore.baseServings)

        cart.toggleFav(item.slug)
        assertTrue(cart.makeCard(item).isFav)
    }

    @Test
    fun allRecipeCards_and_collectionCards_and_searchResults_reflect_the_loaded_catalog() {
        val items = listOf(
            ApiRecipeListItem(1, "Лагман домашний", "lagman", difficulty = "medium", cookingMethod = "boil", baseServings = 4, prepTimeMin = 15, cookTimeMin = 25, yieldWeightG = 1000),
            ApiRecipeListItem(2, "Плов с говядиной", "plov", difficulty = "medium", cookingMethod = "fry", baseServings = 6, prepTimeMin = 20, cookTimeMin = 40, yieldWeightG = 1500),
            ApiRecipeListItem(3, "Шакшука", "shakshuka", difficulty = "easy", cookingMethod = "fry", baseServings = 2, prepTimeMin = 10, cookTimeMin = 10, yieldWeightG = 400),
            ApiRecipeListItem(4, "Манты классические", "manty", difficulty = "hard", cookingMethod = "steam", baseServings = 4, prepTimeMin = 30, cookTimeMin = 60, yieldWeightG = 1200)
        )
        val cart = CartState()
        cart.recipeSummaries.addAll(items)

        assertEquals(items.size, cart.allRecipeCards().size)
        assertEquals(3, cart.collectionCards().size)
        assertEquals(items.size, cart.searchResults().size)

        cart.searchQuery = "лагман"
        assertEquals(listOf("lagman"), cart.searchResults().map { it.id })
    }

    @Test
    fun snapshot_and_applySnapshot_round_trip() {
        val cart = cartWith(lagman, plov, selected = plov).apply {
            incCartQty("rice")
        }
        val snapshot = cart.snapshot()

        val restored = CartState()
        restored.applySnapshot(snapshot)

        assertEquals(snapshot, restored.snapshot())
    }

    @Test
    fun applySnapshot_ignores_non_positive_portions() {
        val cart = CartState()
        cart.applySnapshot(CartSnapshot("plov", 0, emptyMap(), emptyMap(), emptyMap(), false))
        assertEquals(4, cart.portions) // CartState's own default, left untouched since 0 is out of range

        cart.applySnapshot(CartSnapshot("plov", 6, emptyMap(), emptyMap(), emptyMap(), false))
        assertEquals(6, cart.portions)
    }
}
