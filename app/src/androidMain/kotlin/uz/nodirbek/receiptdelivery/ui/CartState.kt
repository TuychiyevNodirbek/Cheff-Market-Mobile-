package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.RECIPES
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.StockStatus
import uz.nodirbek.receiptdelivery.data.imageResFor
import uz.nodirbek.receiptdelivery.data.money
import uz.nodirbek.receiptdelivery.ui.theme.Amber
import uz.nodirbek.receiptdelivery.ui.theme.Green

/** Recipe selection, portion scaling, favorites/search, and cart contents/pricing.
 *  Stays androidMain (not commonMain) because RecipeCard/ScaledIngredient carry an
 *  androidx.compose.ui.graphics.Color, and makeCard() resolves an Android drawable id. */
class CartState {
    var portions by mutableStateOf(4)
    var recipeId by mutableStateOf("lagman")
    val favs = mutableStateMapOf<String, Boolean>()
    val activeChips = mutableStateMapOf<String, Boolean>()
    var searchQuery by mutableStateOf("")
    var stepsOpen by mutableStateOf(false)
    val cartQty = mutableStateMapOf<String, Int>()
    val cartRemoved = mutableStateMapOf<String, Boolean>()
    val cartSubbed = mutableStateMapOf("pepper" to true, "pepper2" to true)
    var onlyMissing by mutableStateOf(false)
    var slot by mutableStateOf("2")
    var comment by mutableStateOf("")

    val recipe: Recipe get() = RECIPES.getValue(recipeId)

    fun scaleFactor(): Double = portions.toDouble() / recipe.baseServings

    fun selectRecipe(id: String) {
        recipeId = id
        portions = RECIPES.getValue(id).baseServings
        stepsOpen = false
    }

    fun toggleFav(id: String) {
        favs[id] = !(favs[id] ?: false)
    }

    fun scaledIngredients(): List<ScaledIngredient> {
        val factor = scaleFactor()
        return recipe.ingredients.map { ing ->
            val qty = Math.round(ing.baseQty * factor).toInt()
            val (label, color) = when (ing.status) {
                StockStatus.OK -> "В наличии" to Green
                StockStatus.LOW -> "Осталось мало" to Amber
                StockStatus.SUBSTITUTED -> "Заменено: ${ing.note}" to Amber
            }
            ScaledIngredient(ing.key, ing.name, ing.name.take(1), "$qty ${ing.unit}", label, color)
        }
    }

    fun cartTotalLabel(): String {
        val factor = scaleFactor()
        val total = recipe.ingredients.sumOf { it.pricePerBase * factor }
        return money(total)
    }

    fun buildCartRows(): List<CartRow> {
        val factor = scaleFactor()
        return recipe.ingredients
            .filter { cartRemoved[it.key] != true }
            .map { ing ->
                val count = cartQty[ing.key] ?: 1
                val qty = Math.round(ing.baseQty * factor * count).toInt()
                val price = Math.round(ing.pricePerBase * factor * count).toInt()
                val substituted = (cartSubbed[ing.key] == true) && ing.status == StockStatus.SUBSTITUTED
                CartRow(
                    key = ing.key,
                    name = ing.name,
                    initial = ing.name.take(1),
                    qtyLabel = "$qty ${ing.unit}",
                    packLabel = "упаковка, \"Korzinka\"",
                    priceLabel = money(price),
                    price = price,
                    count = count,
                    substituted = substituted,
                    subNote = ing.note
                )
            }
    }

    fun incCartQty(key: String) { cartQty[key] = (cartQty[key] ?: 1) + 1 }
    fun decCartQty(key: String) { cartQty[key] = maxOf(1, (cartQty[key] ?: 1) - 1) }
    fun removeCartItem(key: String) { cartRemoved[key] = true }
    fun undoSub(key: String) { cartSubbed[key] = false }

    fun cartSubtotal(): Int = buildCartRows().sumOf { it.price }
    fun deliveryFee(): Int = 12000
    fun cartGrandTotal(): Int = cartSubtotal() + deliveryFee()

    fun makeCard(r: Recipe): RecipeCard = RecipeCard(
        id = r.id, name = r.name, time = r.timeMinutes, baseServings = r.baseServings,
        priceLabel = money(r.basePrice), isFav = favs[r.id] ?: false, heroColors = r.heroColors,
        imageRes = imageResFor(r.imageKey)
    )

    fun allRecipeCards(): List<RecipeCard> = RECIPES.values.map { makeCard(it) }
    fun collectionCards(): List<RecipeCard> = RECIPES.values.take(3).map { makeCard(it) }
    fun searchResults(): List<RecipeCard> {
        val q = searchQuery.trim().lowercase()
        return RECIPES.values.filter { q.isEmpty() || it.name.lowercase().contains(q) }.map { makeCard(it) }
    }

    fun snapshot(): CartSnapshot = CartSnapshot(
        recipeId = recipeId,
        portions = portions,
        cartQty = cartQty.toMap(),
        cartRemoved = cartRemoved.toMap(),
        cartSubbed = cartSubbed.toMap(),
        onlyMissing = onlyMissing
    )

    fun applySnapshot(s: CartSnapshot) {
        if (!RECIPES.containsKey(s.recipeId)) return
        recipeId = s.recipeId
        portions = if (s.portions in 1..6) s.portions else RECIPES.getValue(s.recipeId).baseServings
        cartQty.clear(); cartQty.putAll(s.cartQty)
        cartRemoved.clear(); cartRemoved.putAll(s.cartRemoved)
        cartSubbed.clear(); cartSubbed.putAll(s.cartSubbed)
        onlyMissing = s.onlyMissing
    }
}
