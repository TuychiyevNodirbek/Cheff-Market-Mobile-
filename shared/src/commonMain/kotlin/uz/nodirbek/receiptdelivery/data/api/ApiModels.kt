package uz.nodirbek.receiptdelivery.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Paginated<T>(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T> = emptyList()
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBody
)

@Serializable
data class ApiErrorBody(
    val code: Int,
    val message: String,
    val details: JsonElement? = null
)

@Serializable
data class ApiUser(
    val id: Int,
    val phone: String,
    @SerialName("full_name") val fullName: String = "",
    @SerialName("date_joined") val dateJoined: String = ""
)

@Serializable
data class RequestOtpRequest(val phone: String)

@Serializable
data class RequestOtpResponse(val detail: String)

@Serializable
data class VerifyOtpRequest(val phone: String, val code: String)

@Serializable
data class VerifyOtpResponse(val token: String, val user: ApiUser)

@Serializable
data class ApiCategory(
    val id: Int,
    val name: String,
    val slug: String,
    val parent: Int? = null,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ApiProduct(
    val id: Int,
    val sku: String,
    val name: String,
    val slug: String,
    val description: String = "",
    val image: String = "",
    val category: Int? = null,
    val brand: Int? = null,
    @SerialName("sale_mode") val saleMode: String,
    val price: String,
    @SerialName("price_per_base_unit") val pricePerBaseUnit: String? = null,
    @SerialName("base_unit") val baseUnit: String,
    @SerialName("package_amount") val packageAmount: String
)

@Serializable
data class ApiRecipeListItem(
    val id: Int,
    val title: String,
    val slug: String,
    val summary: String = "",
    val image: String = "",
    val difficulty: String,
    @SerialName("cooking_method") val cookingMethod: String,
    @SerialName("base_servings") val baseServings: Int,
    @SerialName("prep_time_min") val prepTimeMin: Int,
    @SerialName("cook_time_min") val cookTimeMin: Int,
    @SerialName("yield_weight_g") val yieldWeightG: Int
)

@Serializable
data class ApiRecipeIngredient(
    val id: Int,
    val ingredient: Int,
    @SerialName("ingredient_name") val ingredientName: String,
    val quantity: String,
    val unit: String,
    @SerialName("is_optional") val isOptional: Boolean = false,
    val group: String = "",
    val note: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ApiRecipeStep(
    val id: Int,
    val order: Int,
    val text: String,
    val image: String = "",
    val video: String = "",
    @SerialName("duration_min") val durationMin: Int? = null
)

@Serializable
data class ApiRecipeDetail(
    val id: Int,
    val title: String,
    val slug: String,
    val summary: String = "",
    val description: String = "",
    val image: String = "",
    val video: String = "",
    @SerialName("base_servings") val baseServings: Int,
    @SerialName("prep_time_min") val prepTimeMin: Int,
    @SerialName("cook_time_min") val cookTimeMin: Int,
    val difficulty: String,
    @SerialName("cooking_method") val cookingMethod: String,
    @SerialName("yield_weight_g") val yieldWeightG: Int,
    @SerialName("calories_kcal") val caloriesKcal: String? = null,
    @SerialName("protein_g") val proteinG: String? = null,
    @SerialName("fat_g") val fatG: String? = null,
    @SerialName("carbs_g") val carbsG: String? = null,
    val ingredients: List<ApiRecipeIngredient> = emptyList(),
    val steps: List<ApiRecipeStep> = emptyList()
)

@Serializable
data class ApiCartRecipe(
    val id: Int,
    val recipe: Int,
    @SerialName("recipe_title") val recipeTitle: String = "",
    val servings: Int,
    @SerialName("include_pantry") val includePantry: Boolean = false,
    @SerialName("added_at") val addedAt: String = ""
)

@Serializable
data class ApiCartLine(
    @SerialName("product_id") val productId: Int? = null,
    @SerialName("product_name") val productName: String = "",
    val ingredient: String = "",
    val quantity: String,
    val unit: String,
    @SerialName("amount_needed") val amountNeeded: String,
    @SerialName("amount_covered") val amountCovered: String,
    val leftover: String,
    @SerialName("is_substitute") val isSubstitute: Boolean = false,
    val problem: String = ""
)

@Serializable
data class ApiCart(
    @SerialName("cart_id") val cartId: Int,
    val recipes: List<ApiCartRecipe> = emptyList(),
    val lines: List<ApiCartLine> = emptyList()
)

@Serializable
data class AddCartRecipeRequest(
    val recipe: Int,
    val servings: Int,
    @SerialName("include_pantry") val includePantry: Boolean = false
)

@Serializable
data class ApiOrderListItem(
    val id: Int,
    val number: String,
    val status: String,
    @SerialName("amount_products") val amountProducts: String,
    @SerialName("amount_delivery") val amountDelivery: String,
    @SerialName("amount_authorized") val amountAuthorized: String,
    @SerialName("amount_captured") val amountCaptured: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class ApiOrderRecipe(
    val id: Int,
    val recipe: Int,
    @SerialName("recipe_title") val recipeTitle: String = "",
    val servings: Int
)

@Serializable
data class ApiOrderItem(
    val id: Int,
    val product: Int,
    @SerialName("product_name") val productName: String = "",
    @SerialName("product_sku") val productSku: String = "",
    @SerialName("unit_price") val unitPrice: String,
    @SerialName("sale_mode") val saleMode: String,
    @SerialName("quantity_ordered") val quantityOrdered: String,
    @SerialName("quantity_picked") val quantityPicked: String? = null
)

@Serializable
data class ApiOrderDetail(
    val id: Int,
    val number: String,
    val status: String,
    @SerialName("address_text") val addressText: String = "",
    val slot: Int? = null,
    @SerialName("amount_products") val amountProducts: String,
    @SerialName("amount_delivery") val amountDelivery: String,
    @SerialName("amount_authorized") val amountAuthorized: String,
    @SerialName("amount_captured") val amountCaptured: String? = null,
    @SerialName("payment_provider") val paymentProvider: String = "",
    val comment: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val recipes: List<ApiOrderRecipe> = emptyList(),
    val items: List<ApiOrderItem> = emptyList()
)

@Serializable
data class CreateOrderRequest(
    val address: Int,
    val slot: Int,
    @SerialName("payment_provider") val paymentProvider: String = "cash",
    val comment: String = ""
)

@Serializable
data class ApiDeliveryZone(
    val id: Int,
    val name: String,
    @SerialName("delivery_fee") val deliveryFee: String,
    @SerialName("free_delivery_from") val freeDeliveryFrom: String,
    @SerialName("min_order_amount") val minOrderAmount: String
)

@Serializable
data class ApiAddress(
    val id: Int,
    val label: String = "",
    val street: String = "",
    val house: String = "",
    val apartment: String = "",
    val entrance: String = "",
    val floor: String = "",
    val comment: String = "",
    val zone: Int,
    val latitude: String? = null,
    val longitude: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false
)

/** Also used for `PATCH /addresses/<id>/` - the backend treats every field as optional there,
 *  but since this app always resends the full picker state, one request shape covers both. */
@Serializable
data class AddressRequest(
    val label: String = "",
    val street: String,
    val house: String,
    val apartment: String = "",
    val entrance: String = "",
    val floor: String = "",
    val comment: String = "",
    val zone: Int,
    val latitude: String? = null,
    val longitude: String? = null,
    @SerialName("is_default") val isDefault: Boolean = true
)
