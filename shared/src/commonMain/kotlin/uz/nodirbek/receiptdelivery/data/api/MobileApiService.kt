package uz.nodirbek.receiptdelivery.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Thin wrapper over `/api/mobile/...`, per MOBILE_API.md. Endpoints under
 * "Корзина и заказы" and `auth/me/` require [authToken] to be set after a
 * successful [verifyOtp].
 */
class MobileApiService(private val client: HttpClient = createHttpClient()) {

    var authToken: String? = null

    private fun HttpRequestBuilder.withAuth() {
        authToken?.let { header(HttpHeaders.Authorization, "Token $it") }
    }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (status.isSuccess()) return body()
        val envelope = runCatching { body<ApiErrorEnvelope>() }.getOrNull()
        throw ApiException(
            code = envelope?.error?.code ?: status.value,
            message = envelope?.error?.message ?: "Request failed (${status.value})",
            details = envelope?.error?.details
        )
    }

    private suspend fun HttpResponse.throwIfNotSuccess() {
        if (!status.isSuccess()) {
            val envelope = runCatching { body<ApiErrorEnvelope>() }.getOrNull()
            throw ApiException(
                code = envelope?.error?.code ?: status.value,
                message = envelope?.error?.message ?: "Request failed (${status.value})",
                details = envelope?.error?.details
            )
        }
    }

    // --- Auth ---

    suspend fun requestOtp(phone: String): RequestOtpResponse =
        client.post("auth/request-otp/") {
            contentType(ContentType.Application.Json)
            setBody(RequestOtpRequest(phone))
        }.bodyOrThrow()

    suspend fun verifyOtp(phone: String, code: String): VerifyOtpResponse =
        client.post("auth/verify-otp/") {
            contentType(ContentType.Application.Json)
            setBody(VerifyOtpRequest(phone, code))
        }.bodyOrThrow()

    suspend fun me(): ApiUser =
        client.get("auth/me/") { withAuth() }.bodyOrThrow()

    // --- Catalog ---

    suspend fun categories(page: Int? = null): Paginated<ApiCategory> =
        client.get("catalog/categories/") { page?.let { parameter("page", it) } }.bodyOrThrow()

    suspend fun products(page: Int? = null): Paginated<ApiProduct> =
        client.get("catalog/products/") { page?.let { parameter("page", it) } }.bodyOrThrow()

    suspend fun product(slug: String): ApiProduct =
        client.get("catalog/products/$slug/").bodyOrThrow()

    // --- Recipes ---

    suspend fun recipes(page: Int? = null): Paginated<ApiRecipeListItem> =
        client.get("recipes/") { page?.let { parameter("page", it) } }.bodyOrThrow()

    suspend fun recipe(slug: String): ApiRecipeDetail =
        client.get("recipes/$slug/").bodyOrThrow()

    // --- Addresses (locations) ---

    suspend fun deliveryZones(): Paginated<ApiDeliveryZone> =
        client.get("delivery-zones/").bodyOrThrow()

    suspend fun addresses(): Paginated<ApiAddress> =
        client.get("addresses/") { withAuth() }.bodyOrThrow()

    suspend fun createAddress(request: AddressRequest): ApiAddress =
        client.post("addresses/") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyOrThrow()

    suspend fun updateAddress(id: Int, request: AddressRequest): ApiAddress =
        client.patch("addresses/$id/") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyOrThrow()

    suspend fun deleteAddress(id: Int) {
        client.delete("addresses/$id/") { withAuth() }.throwIfNotSuccess()
    }

    // --- Cart & orders (require authToken) ---

    suspend fun cart(): ApiCart =
        client.get("cart/") { withAuth() }.bodyOrThrow()

    suspend fun addRecipeToCart(recipe: Int, servings: Int, includePantry: Boolean = false): ApiCartRecipe =
        client.post("cart/recipes/") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(AddCartRecipeRequest(recipe, servings, includePantry))
        }.bodyOrThrow()

    suspend fun removeCartRecipe(cartRecipeId: Int) {
        client.delete("cart/recipes/$cartRecipeId/") { withAuth() }.throwIfNotSuccess()
    }

    suspend fun orders(page: Int? = null): Paginated<ApiOrderListItem> =
        client.get("orders/") {
            withAuth()
            page?.let { parameter("page", it) }
        }.bodyOrThrow()

    suspend fun order(id: Int): ApiOrderDetail =
        client.get("orders/$id/") { withAuth() }.bodyOrThrow()

    suspend fun createOrder(
        address: Int,
        slot: Int,
        paymentProvider: String = "cash",
        comment: String = ""
    ): ApiOrderDetail =
        client.post("orders/create/") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(CreateOrderRequest(address, slot, paymentProvider, comment))
        }.bodyOrThrow()
}
