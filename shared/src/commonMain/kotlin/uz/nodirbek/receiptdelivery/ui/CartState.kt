package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.Ingredient
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.StockStatus
import uz.nodirbek.receiptdelivery.data.Step
import uz.nodirbek.receiptdelivery.data.api.ApiRecipeDetail
import uz.nodirbek.receiptdelivery.data.api.ApiRecipeListItem
import uz.nodirbek.receiptdelivery.data.api.MobileApiService
import uz.nodirbek.receiptdelivery.data.heroColorsFor
import uz.nodirbek.receiptdelivery.data.imageResFor
import uz.nodirbek.receiptdelivery.data.money
import uz.nodirbek.receiptdelivery.data.resolveImageUrl
import uz.nodirbek.receiptdelivery.ui.theme.Green
import kotlin.math.roundToInt

private val EMPTY_RECIPE = Recipe(
    id = "", name = "", cuisine = "", timeMinutes = 0, baseServings = 1,
    rating = "", reviews = 0, heroColors = 0xFFE0E0E0 to 0xFFC8C8C8,
    imageKey = "", ingredients = emptyList(), steps = emptyList(), serverId = 0
)

/** Recipe browsing (backed by `GET /recipes/` and `GET /recipes/{slug}/`), portion scaling,
 *  favorites/search, the ingredient shopping-list preview, and syncing the selected recipe to the
 *  server cart (`POST/DELETE /cart/recipes/`). */
class CartState {
    var portions by mutableStateOf(4)
    var recipeId by mutableStateOf("")
    val favs = mutableStateMapOf<String, Boolean>()
    val activeChips = mutableStateMapOf<String, Boolean>()
    var searchQuery by mutableStateOf("")
    var stepsOpen by mutableStateOf(false)
    val cartQty = mutableStateMapOf<String, Int>()
    val cartRemoved = mutableStateMapOf<String, Boolean>()
    val cartSubbed = mutableStateMapOf<String, Boolean>()
    var onlyMissing by mutableStateOf(false)
    var slot by mutableStateOf("2")
    var comment by mutableStateOf("")

    var recipesLoading by mutableStateOf(false)
        private set
    var recipesRefreshing by mutableStateOf(false)
        private set
    var recipesError by mutableStateOf<String?>(null)
        private set
    var recipeDetailLoading by mutableStateOf(false)
        private set
    // internal rather than private so tests can seed the catalog/detail cache directly instead of
    // exercising real network calls - see CartStateTest.
    internal val recipeSummaries = mutableStateListOf<ApiRecipeListItem>()
    internal val recipeDetails = mutableStateMapOf<String, Recipe>()

    /** Surfaced in the cart screen when the best-effort sync to the server cart fails - the local
     *  shopping-list preview still works, so this never blocks navigation. */
    var cartSyncError by mutableStateOf<String?>(null)
        private set
    // recipe slug -> the server's cart-recipe id, so re-syncing can replace rather than duplicate it.
    private val cartRecipeIds = mutableStateMapOf<String, Int>()

    val recipe: Recipe get() = recipeDetails[recipeId] ?: EMPTY_RECIPE

    fun scaleFactor(): Double = if (recipe.baseServings == 0) 1.0 else portions.toDouble() / recipe.baseServings

    /** Loads the recipe catalog once and caches it for the session. */
    suspend fun loadRecipes(api: MobileApiService, messages: AppMessages) {
        if (recipeSummaries.isNotEmpty() || recipesLoading) return
        recipesLoading = true
        recipesError = null
        try {
            val results = api.recipes().results
            recipeSummaries.clear()
            recipeSummaries.addAll(results)
        } catch (e: Exception) {
            recipesError = messages.errorLoadRecipes
        } finally {
            recipesLoading = false
        }
    }

    /** Re-fetches the recipe catalog regardless of cache, for pull-to-refresh. */
    suspend fun refreshRecipes(api: MobileApiService, messages: AppMessages) {
        if (recipesRefreshing || recipesLoading) return
        recipesRefreshing = true
        recipesError = null
        try {
            val results = api.recipes().results
            recipeSummaries.clear()
            recipeSummaries.addAll(results)
        } catch (e: Exception) {
            recipesError = messages.errorLoadRecipes
        } finally {
            recipesRefreshing = false
        }
    }

    /** Loads full ingredients/steps for one recipe (the list endpoint omits them) and selects it. */
    suspend fun selectRecipe(api: MobileApiService, slug: String, messages: AppMessages) {
        recipeId = slug
        stepsOpen = false
        recipeDetails[slug]?.let { portions = it.baseServings; return }
        recipeDetailLoading = true
        try {
            val detail = api.recipe(slug)
            recipeDetails[slug] = detail.toRecipe()
            portions = detail.baseServings
        } catch (e: Exception) {
            recipesError = messages.errorLoadRecipe
        } finally {
            recipeDetailLoading = false
        }
    }

    fun toggleFav(id: String) {
        favs[id] = !(favs[id] ?: false)
    }

    fun scaledIngredients(inStockLabel: String): List<ScaledIngredient> {
        val factor = scaleFactor()
        return recipe.ingredients.map { ing ->
            val qty = (ing.baseQty * factor).roundToInt()
            // The recipe endpoint doesn't report live stock - availability is only known once an
            // ingredient is actually added to the server cart, so every line reads as available here.
            ScaledIngredient(ing.key, ing.name, ing.name.take(1), "$qty ${ing.unit}", inStockLabel, Green)
        }
    }

    fun buildCartRows(): List<CartRow> {
        val factor = scaleFactor()
        return recipe.ingredients
            .filter { cartRemoved[it.key] != true }
            .map { ing ->
                val count = cartQty[ing.key] ?: 1
                val qty = (ing.baseQty * factor * count).roundToInt()
                val price = (ing.pricePerBase * factor * count).roundToInt()
                CartRow(
                    key = ing.key,
                    name = ing.name,
                    initial = ing.name.take(1),
                    qtyLabel = "$qty ${ing.unit}",
                    packLabel = "",
                    priceLabel = money(price),
                    price = price,
                    count = count,
                    substituted = false,
                    subNote = ""
                )
            }
    }

    fun incCartQty(key: String) { cartQty[key] = (cartQty[key] ?: 1) + 1 }
    fun decCartQty(key: String) { cartQty[key] = maxOf(1, (cartQty[key] ?: 1) - 1) }
    fun removeCartItem(key: String) { cartRemoved[key] = true }
    fun undoSub(key: String) { cartSubbed[key] = false }

    fun cartTotalLabel(): String {
        val factor = scaleFactor()
        val total = recipe.ingredients.sumOf { it.pricePerBase * factor }
        return money(total)
    }

    fun cartSubtotal(): Int = buildCartRows().sumOf { it.price }
    fun deliveryFee(): Int = deliveryFeeOverride ?: 12000
    fun cartGrandTotal(): Int = cartSubtotal() + deliveryFee()

    /** Set from the real `delivery-zones/` fee for the user's district once it's known
     *  (see [AppState.refreshDeliveryFee]); falls back to a placeholder until then. */
    var deliveryFeeOverride by mutableStateOf<Int?>(null)

    /** Registers the current recipe (at the current [portions]) in the server cart. Best-effort:
     *  a failure is surfaced via [cartSyncError] but never blocks navigation to the cart screen. */
    suspend fun syncToServerCart(api: MobileApiService, messages: AppMessages) {
        val serverId = recipe.serverId
        if (serverId == 0) return
        try {
            cartSyncError = null
            cartRecipeIds[recipeId]?.let { api.removeCartRecipe(it) }
            val added = api.addRecipeToCart(serverId, portions)
            cartRecipeIds[recipeId] = added.id
        } catch (e: Exception) {
            cartSyncError = messages.errorSyncCart
        }
    }

    fun makeCard(item: ApiRecipeListItem): RecipeCard = RecipeCard(
        id = item.slug,
        name = item.title,
        time = item.prepTimeMin + item.cookTimeMin,
        baseServings = item.baseServings,
        isFav = favs[item.slug] ?: false,
        heroColors = heroColorsFor(item.slug),
        imageRes = imageResFor(item.slug),
        imageUrl = resolveImageUrl(item.image)
    )

    fun allRecipeCards(): List<RecipeCard> = recipeSummaries.map { makeCard(it) }
    fun collectionCards(): List<RecipeCard> = recipeSummaries.take(3).map { makeCard(it) }
    fun searchResults(): List<RecipeCard> {
        val q = searchQuery.trim().lowercase()
        return recipeSummaries.filter { q.isEmpty() || it.title.lowercase().contains(q) }.map { makeCard(it) }
    }

    fun snapshot(): CartSnapshot = CartSnapshot(
        recipeId = recipeId,
        portions = portions,
        cartQty = cartQty.toMap(),
        cartRemoved = cartRemoved.toMap(),
        cartSubbed = cartSubbed.toMap(),
        onlyMissing = onlyMissing
    )

    /** Restores the last-picked recipe id/portions - the recipe itself is re-fetched by
     *  [selectRecipe] once the caller knows the API is reachable, since it's no longer bundled locally. */
    fun applySnapshot(s: CartSnapshot) {
        recipeId = s.recipeId
        if (s.portions > 0) portions = s.portions
        cartQty.clear(); cartQty.putAll(s.cartQty)
        cartRemoved.clear(); cartRemoved.putAll(s.cartRemoved)
        cartSubbed.clear(); cartSubbed.putAll(s.cartSubbed)
        onlyMissing = s.onlyMissing
    }
}

private fun ApiRecipeDetail.toRecipe(): Recipe = Recipe(
    id = slug,
    name = title,
    cuisine = cookingMethod,
    timeMinutes = prepTimeMin + cookTimeMin,
    baseServings = baseServings,
    rating = "",
    reviews = 0,
    heroColors = heroColorsFor(slug),
    imageKey = slug,
    imageUrl = resolveImageUrl(image),
    ingredients = ingredients.map { ing ->
        Ingredient(
            key = ing.id.toString(),
            name = ing.ingredientName,
            unit = ing.unit,
            baseQty = ing.quantity.toDoubleOrNull()?.roundToInt() ?: 0,
            // The recipe endpoint carries no pricing - real prices are only resolved from the
            // catalog once an ingredient is matched to a product in the server cart.
            pricePerBase = 0,
            status = StockStatus.OK,
            note = ing.note
        )
    },
    steps = steps.sortedBy { it.order }.map { Step(it.text, it.durationMin) },
    serverId = id
)
