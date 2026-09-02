package uz.nodirbek.receiptdelivery.ui

import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.RECIPES
import uz.nodirbek.receiptdelivery.data.StockStatus
import uz.nodirbek.receiptdelivery.ui.theme.Amber
import uz.nodirbek.receiptdelivery.ui.theme.Green
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** JVM-only (androidUnitTest, not commonTest): CartState carries androidx.compose.ui.graphics.Color
 *  (ScaledIngredient) and an Android drawable resId (RecipeCard.imageRes via imageResFor()), so it
 *  stays androidMain rather than commonMain - see CartState.kt's class doc. */
class CartStateTest {
    private val lagman = RECIPES.getValue("lagman")

    @Test
    fun recipe_defaults_to_lagman() {
        assertEquals("lagman", CartState().recipeId)
        assertEquals(lagman, CartState().recipe)
    }

    @Test
    fun scaleFactor_is_portions_over_base_servings() {
        val cart = CartState().apply { portions = 8 }
        assertEquals(8.0 / lagman.baseServings, cart.scaleFactor())
    }

    @Test
    fun selectRecipe_resets_portions_to_the_new_recipes_base_and_closes_steps() {
        val cart = CartState().apply {
            portions = 99
            stepsOpen = true
        }

        cart.selectRecipe("plov")

        assertEquals("plov", cart.recipeId)
        assertEquals(RECIPES.getValue("plov").baseServings, cart.portions)
        assertFalse(cart.stepsOpen)
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
        val cart = CartState() // recipeId=lagman, portions=4=baseServings -> factor 1.0
        val scaled = cart.scaledIngredients().associateBy { it.key }

        val beef = lagman.ingredients.single { it.key == "beef" }
        assertEquals("${beef.baseQty} ${beef.unit}", scaled.getValue("beef").qtyLabel)
    }

    @Test
    fun scaledIngredients_labels_and_colors_follow_stock_status() {
        val cart = CartState()
        val scaled = cart.scaledIngredients().associateBy { it.key }

        val okIngredient = lagman.ingredients.first { it.status == StockStatus.OK }
        val lowIngredient = lagman.ingredients.single { it.status == StockStatus.LOW }
        val substitutedIngredient = lagman.ingredients.single { it.status == StockStatus.SUBSTITUTED }

        assertEquals("В наличии" to Green, scaled.getValue(okIngredient.key).let { it.statusLabel to it.statusColor })
        assertEquals("Осталось мало" to Amber, scaled.getValue(lowIngredient.key).let { it.statusLabel to it.statusColor })
        assertEquals(
            "Заменено: ${substitutedIngredient.note}" to Amber,
            scaled.getValue(substitutedIngredient.key).let { it.statusLabel to it.statusColor },
        )
    }

    @Test
    fun scaledIngredients_scales_quantities_with_portions() {
        val cart = CartState().apply { portions = lagman.baseServings * 2 }
        val beef = lagman.ingredients.single { it.key == "beef" }

        val scaledQty = cart.scaledIngredients().single { it.key == "beef" }.qtyLabel
        assertEquals("${beef.baseQty * 2} ${beef.unit}", scaledQty)
    }

    @Test
    fun cartTotalLabel_at_base_servings_equals_recipe_basePrice() {
        val cart = CartState()
        assertEquals(uz.nodirbek.receiptdelivery.data.money(lagman.basePrice), cart.cartTotalLabel())
    }

    @Test
    fun buildCartRows_excludes_removed_items() {
        val cart = CartState()
        val keyToRemove = lagman.ingredients.first().key

        cart.removeCartItem(keyToRemove)

        assertFalse(cart.buildCartRows().any { it.key == keyToRemove })
        assertEquals(lagman.ingredients.size - 1, cart.buildCartRows().size)
    }

    @Test
    fun buildCartRows_defaults_count_to_one_and_multiplies_price() {
        val cart = CartState()
        val ingredient = lagman.ingredients.first()

        val rowBefore = cart.buildCartRows().single { it.key == ingredient.key }
        assertEquals(1, rowBefore.count)

        cart.incCartQty(ingredient.key)
        val rowAfter = cart.buildCartRows().single { it.key == ingredient.key }
        assertEquals(2, rowAfter.count)
        assertEquals(rowBefore.price * 2, rowAfter.price)
    }

    @Test
    fun buildCartRows_substituted_true_only_when_accepted_and_actually_substituted() {
        val cart = CartState() // default cartSubbed has "pepper" -> true, and pepper is StockStatus.SUBSTITUTED in lagman
        val pepperRow = cart.buildCartRows().single { it.key == "pepper" }
        assertTrue(pepperRow.substituted)

        cart.undoSub("pepper")
        val afterUndo = cart.buildCartRows().single { it.key == "pepper" }
        assertFalse(afterUndo.substituted)
    }

    @Test
    fun buildCartRows_substituted_false_for_non_substituted_ingredients_even_if_flag_set() {
        val cart = CartState()
        val okKey = lagman.ingredients.first { it.status == StockStatus.OK }.key

        cart.cartSubbed[okKey] = true // сам факт флага не должен помечать замену для не-SUBSTITUTED статуса

        assertFalse(cart.buildCartRows().single { it.key == okKey }.substituted)
    }

    @Test
    fun incCartQty_and_decCartQty_never_go_below_one() {
        val cart = CartState()
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
        val cart = CartState()
        assertEquals(12000, cart.deliveryFee())
        assertEquals(cart.buildCartRows().sumOf { it.price }, cart.cartSubtotal())
        assertEquals(cart.cartSubtotal() + 12000, cart.cartGrandTotal())
    }

    @Test
    fun makeCard_maps_recipe_fields_and_reflects_favorite_state() {
        val cart = CartState()
        val cardBefore = cart.makeCard(lagman)
        assertFalse(cardBefore.isFav)
        assertEquals(lagman.id, cardBefore.id)
        assertEquals(lagman.name, cardBefore.name)
        assertEquals(lagman.timeMinutes, cardBefore.time)
        assertEquals(lagman.baseServings, cardBefore.baseServings)
        assertEquals(lagman.heroColors, cardBefore.heroColors)
        assertEquals(uz.nodirbek.receiptdelivery.data.money(lagman.basePrice), cardBefore.priceLabel)

        cart.toggleFav(lagman.id)
        assertTrue(cart.makeCard(lagman).isFav)
    }

    @Test
    fun allRecipeCards_covers_the_whole_catalog() {
        assertEquals(RECIPES.size, CartState().allRecipeCards().size)
    }

    @Test
    fun collectionCards_is_capped_at_three() {
        assertEquals(minOf(3, RECIPES.size), CartState().collectionCards().size)
    }

    @Test
    fun searchResults_blank_query_returns_everything() {
        assertEquals(RECIPES.size, CartState().searchResults().size)
    }

    @Test
    fun searchResults_filters_case_insensitively_by_name_substring() {
        val cart = CartState().apply { searchQuery = lagman.name.take(4).uppercase() }
        val results = cart.searchResults()

        assertTrue(results.any { it.id == lagman.id })
        assertTrue(results.all { it.name.contains(lagman.name.take(4), ignoreCase = true) })
    }

    @Test
    fun snapshot_and_applySnapshot_round_trip() {
        val cart = CartState().apply {
            recipeId = "plov"
            portions = 6
            incCartQty("rice")
            removeCartItem("oil")
        }
        val snapshot = cart.snapshot()

        val restored = CartState()
        restored.applySnapshot(snapshot)

        assertEquals(snapshot, restored.snapshot())
    }

    @Test
    fun applySnapshot_ignores_unknown_recipeId() {
        val cart = CartState().apply { recipeId = "plov"; portions = 6 }

        cart.applySnapshot(CartSnapshot("does-not-exist", 2, emptyMap(), emptyMap(), emptyMap(), false))

        assertEquals("plov", cart.recipeId, "неизвестный recipeId в снапшоте должен быть проигнорирован целиком")
        assertEquals(6, cart.portions)
    }

    @Test
    fun applySnapshot_clamps_out_of_range_portions_to_recipe_base_servings() {
        val cart = CartState()
        cart.applySnapshot(CartSnapshot("plov", 0, emptyMap(), emptyMap(), emptyMap(), false))
        assertEquals(RECIPES.getValue("plov").baseServings, cart.portions)

        cart.applySnapshot(CartSnapshot("plov", 99, emptyMap(), emptyMap(), emptyMap(), false))
        assertEquals(RECIPES.getValue("plov").baseServings, cart.portions)
    }
}
