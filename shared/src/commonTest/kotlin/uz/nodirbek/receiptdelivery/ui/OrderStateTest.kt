package uz.nodirbek.receiptdelivery.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** OrderState's data now all comes from the backend (`GET/POST /orders/...`), so there's no local
 *  simulation logic left to unit test without mocking the HTTP client - these tests cover only the
 *  synchronous, non-network behavior: initial state, [OrderState.failCheckout] and [OrderState.clear]. */
class OrderStateTest {
    @Test
    fun starts_with_no_orders_and_no_current_order() {
        val order = OrderState()
        assertTrue(order.orders.isEmpty())
        assertNull(order.currentOrder)
        assertFalse(order.ordersLoading)
        assertFalse(order.placingOrder)
    }

    @Test
    fun failCheckout_sets_checkoutError() {
        val order = OrderState()
        order.failCheckout("Сначала выберите адрес доставки.")
        assertEquals("Сначала выберите адрес доставки.", order.checkoutError)
    }

    @Test
    fun clear_resets_orders_and_errors() {
        val order = OrderState().apply {
            failCheckout("some error")
        }

        order.clear()

        assertTrue(order.orders.isEmpty())
        assertNull(order.currentOrder)
        assertNull(order.ordersError)
        assertNull(order.currentOrderError)
        assertNull(order.checkoutError)
    }
}

// orderStatusLabel itself is now @Composable (it resolves its text via stringResource - see
// OrderStatus.kt), so it's exercised by the app's screens rather than a plain JVM unit test here.
class OrderStatusTest {
    @Test
    fun orderStatusIsFinal_true_only_for_delivered_or_cancelled() {
        assertTrue(orderStatusIsFinal("delivered"))
        assertTrue(orderStatusIsFinal("cancelled"))
        assertFalse(orderStatusIsFinal("created"))
        assertFalse(orderStatusIsFinal("picking"))
    }
}
