package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.nodirbek.receiptdelivery.data.api.ApiException
import uz.nodirbek.receiptdelivery.data.api.ApiOrderDetail
import uz.nodirbek.receiptdelivery.data.api.ApiOrderListItem
import uz.nodirbek.receiptdelivery.data.api.MobileApiService

/** Order history and the currently tracked order - both sourced from the backend
 *  (`GET /orders/`, `GET /orders/<id>/`, `POST /orders/create/`). This app never invents order
 *  data locally; there's no local order id/status simulation here. */
class OrderState {
    val orders = mutableStateListOf<ApiOrderListItem>()
    var ordersLoading by mutableStateOf(false)
        private set
    var ordersError by mutableStateOf<String?>(null)
        private set

    var currentOrder by mutableStateOf<ApiOrderDetail?>(null)
        private set
    var currentOrderLoading by mutableStateOf(false)
        private set
    var currentOrderError by mutableStateOf<String?>(null)
        private set

    var checkoutError by mutableStateOf<String?>(null)
        private set
    var placingOrder by mutableStateOf(false)
        private set

    /** Drops whatever's cached, e.g. on logout - the next authenticated user's `GET /orders/` must
     *  never show through what was on screen for the previous one. */
    fun clear() {
        orders.clear()
        currentOrder = null
        ordersError = null
        currentOrderError = null
        checkoutError = null
    }

    suspend fun loadOrders(api: MobileApiService, messages: AppMessages) {
        ordersLoading = true
        ordersError = null
        try {
            val results = api.orders().results
            orders.clear()
            orders.addAll(results)
        } catch (e: Exception) {
            ordersError = messages.errorLoadOrders
        } finally {
            ordersLoading = false
        }
    }

    suspend fun loadOrderDetail(api: MobileApiService, id: Int, messages: AppMessages) {
        currentOrderLoading = true
        currentOrderError = null
        try {
            currentOrder = api.order(id)
        } catch (e: Exception) {
            currentOrderError = messages.errorLoadOrder
        } finally {
            currentOrderLoading = false
        }
    }

    /** Surfaces a checkout failure that happens before the API call itself (e.g. no saved address),
     *  so the checkout screen can show one consistent error regardless of where it came from. */
    fun failCheckout(message: String) {
        checkoutError = message
    }

    /** Returns the created order's id on success, or null on failure (see [checkoutError]). */
    suspend fun placeOrder(
        api: MobileApiService,
        address: Int,
        slot: Int,
        paymentProvider: String,
        comment: String,
        messages: AppMessages
    ): Int? {
        placingOrder = true
        checkoutError = null
        return try {
            val created = api.createOrder(address, slot, paymentProvider, comment)
            currentOrder = created
            created.id
        } catch (e: ApiException) {
            checkoutError = e.message
            null
        } catch (e: Exception) {
            checkoutError = messages.errorPlaceOrder
            null
        } finally {
            placingOrder = false
        }
    }
}
