package uz.nodirbek.receiptdelivery.data

import com.russhwolf.settings.Settings

data class CartSnapshot(
    val recipeId: String,
    val portions: Int,
    val cartQty: Map<String, Int>,
    val cartRemoved: Map<String, Boolean>,
    val cartSubbed: Map<String, Boolean>,
    val onlyMissing: Boolean
)

private const val PREFS_NAME = "recipe_delivery_cart"
private const val KEY_RECIPE_ID = "recipeId"
private const val KEY_PORTIONS = "portions"
private const val KEY_CART_QTY = "cartQty"
private const val KEY_CART_REMOVED = "cartRemoved"
private const val KEY_CART_SUBBED = "cartSubbed"
private const val KEY_ONLY_MISSING = "onlyMissing"

fun cartPrefsName(): String = PREFS_NAME

fun Settings.saveCartSnapshot(snapshot: CartSnapshot) {
    putString(KEY_RECIPE_ID, snapshot.recipeId)
    putInt(KEY_PORTIONS, snapshot.portions)
    putString(KEY_CART_QTY, snapshot.cartQty.entries.joinToString(";") { "${it.key}:${it.value}" })
    putString(KEY_CART_REMOVED, snapshot.cartRemoved.filterValues { it }.keys.joinToString(","))
    putString(KEY_CART_SUBBED, snapshot.cartSubbed.entries.joinToString(";") { "${it.key}:${it.value}" })
    putBoolean(KEY_ONLY_MISSING, snapshot.onlyMissing)
}

fun Settings.loadCartSnapshot(): CartSnapshot? {
    val recipeId = getStringOrNull(KEY_RECIPE_ID) ?: return null
    val portions = getInt(KEY_PORTIONS, 0)
    val cartQty = getString(KEY_CART_QTY, "")
        .split(";").filter { it.isNotBlank() }
        .mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: return@mapNotNull null) else null
        }.toMap()
    val cartRemoved = getString(KEY_CART_REMOVED, "")
        .split(",").filter { it.isNotBlank() }
        .associateWith { true }
    val cartSubbed = getString(KEY_CART_SUBBED, "")
        .split(";").filter { it.isNotBlank() }
        .mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) parts[0] to parts[1].toBoolean() else null
        }.toMap()
    val onlyMissing = getBoolean(KEY_ONLY_MISSING, false)
    return CartSnapshot(recipeId, portions, cartQty, cartRemoved, cartSubbed, onlyMissing)
}
