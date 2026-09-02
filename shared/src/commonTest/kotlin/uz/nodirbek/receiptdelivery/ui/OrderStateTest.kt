package uz.nodirbek.receiptdelivery.ui

import uz.nodirbek.receiptdelivery.data.RECIPES
import uz.nodirbek.receiptdelivery.geo.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrderStateTest {
    private val recipe = RECIPES.getValue("lagman")

    @Test
    fun orderStatuses_marks_steps_up_to_current_index_active() {
        val order = OrderState().apply { orderStatusIdx = 1 }
        val statuses = order.orderStatuses()

        assertEquals(STATUS_LABELS.size, statuses.size)
        assertEquals(listOf(true, true, false, false), statuses.map { it.active })
        assertEquals(listOf(0, 1, 2, 3), statuses.map { it.index })
        assertEquals(STATUS_LABELS, statuses.map { it.label })
    }

    @Test
    fun mapLabel_switches_once_courier_is_on_the_way() {
        val order = OrderState()
        assertEquals("карта появится, когда курьер выедет", order.mapLabel())

        order.orderStatusIdx = 2
        assertEquals("карта: курьер в пути", order.mapLabel())
    }

    @Test
    fun advanceOrderStatus_stops_at_last_status_and_does_nothing_more() {
        val order = OrderState()
        order.placeOrder(recipe, itemCount = 3, totalLabel = "10 000", district = "Юнусабад", point = GeoPoint(1.0, 1.0))

        repeat(STATUS_LABELS.size + 3) { order.advanceOrderStatus() }

        assertEquals(STATUS_LABELS.size - 1, order.orderStatusIdx)
        assertEquals(STATUS_LABELS.last(), order.orders.first().statusLabel)
    }

    @Test
    fun advanceOrderStatus_updates_only_the_first_orders_statusLabel() {
        val order = OrderState()
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = GeoPoint(1.0, 1.0))
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = GeoPoint(1.0, 1.0))
        val secondOrderOriginalStatus = order.orders[1].statusLabel

        order.advanceOrderStatus()

        assertEquals(STATUS_LABELS[1], order.orders[0].statusLabel)
        assertEquals(secondOrderOriginalStatus, order.orders[1].statusLabel, "advanceOrderStatus не должен трогать старые заказы")
    }

    @Test
    fun advanceOrderStatus_on_empty_orders_only_moves_the_index() {
        val order = OrderState()
        order.advanceOrderStatus()
        assertEquals(1, order.orderStatusIdx)
        assertTrue(order.orders.isEmpty())
    }

    @Test
    fun currentOrderId_is_placeholder_when_no_orders_yet() {
        assertEquals("—", OrderState().currentOrderId())
    }

    @Test
    fun currentOrderId_returns_the_most_recent_orders_id() {
        val order = OrderState()
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = GeoPoint(1.0, 1.0))
        assertEquals(order.orders.first().id, order.currentOrderId())
    }

    @Test
    fun currentOrderPoint_uses_fallback_when_no_orders() {
        val fallback = GeoPoint(41.0, 69.0)
        assertEquals(fallback, OrderState().currentOrderPoint(fallback))
    }

    @Test
    fun currentOrderPoint_uses_the_order_own_saved_location_not_the_fallback() {
        val order = OrderState()
        val orderPoint = GeoPoint(41.35, 69.28)
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = orderPoint)

        val result = order.currentOrderPoint(fallback = GeoPoint(0.0, 0.0))

        assertEquals(orderPoint, result)
    }

    @Test
    fun placeOrder_adds_to_the_front_and_resets_tracking_state() {
        val order = OrderState().apply {
            orderStatusIdx = 3
            orderCardOpen = false
        }

        order.placeOrder(recipe, itemCount = 5, totalLabel = "68 000", district = "Юнусабад", point = GeoPoint(41.3, 69.2))
        order.placeOrder(recipe, itemCount = 2, totalLabel = "20 000", district = "Мирабад", point = GeoPoint(41.29, 69.28))

        assertEquals(2, order.orders.size)
        assertEquals("Мирабад", order.orders.first().district, "новый заказ должен встать первым")
        assertEquals(0, order.orderStatusIdx)
        assertTrue(order.orderCardOpen)
    }

    @Test
    fun placeOrder_ids_increment_from_the_2481_base() {
        val order = OrderState()
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = GeoPoint(1.0, 1.0))
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = GeoPoint(1.0, 1.0))

        assertEquals("2481", order.orders[1].id) // первый заказ, добавлен раньше
        assertEquals("2482", order.orders[0].id) // второй заказ, сейчас на вершине списка
    }

    @Test
    fun placeOrder_fills_order_fields_from_arguments() {
        val order = OrderState()
        order.placeOrder(
            recipe = recipe,
            itemCount = 4,
            totalLabel = "44 000",
            district = "Чиланзар",
            point = GeoPoint(41.283, 69.204),
        )

        val placed = order.orders.single()
        assertEquals(recipe.name, placed.recipeName)
        assertEquals("4 позиции", placed.itemsSummary)
        assertEquals("44 000", placed.totalLabel)
        assertEquals("Чиланзар", placed.district)
        assertEquals(41.283, placed.lat)
        assertEquals(69.204, placed.lon)
        assertEquals(STATUS_LABELS[0], placed.statusLabel)
    }

    @Test
    fun placeOrder_dateLabel_matches_the_expected_format() {
        val order = OrderState()
        order.placeOrder(recipe, itemCount = 1, totalLabel = "1", district = "Юнусабад", point = GeoPoint(1.0, 1.0))

        // "18 авг, 12:00" - multiplatform-safe formatter, see OrderState.kt's currentOrderDateLabel().
        val dateLabelPattern = Regex("""^\d{1,2} [а-я]{3}, \d{2}:\d{2}$""")
        assertTrue(
            dateLabelPattern.matches(order.orders.single().dateLabel),
            "неожиданный формат dateLabel: ${order.orders.single().dateLabel}",
        )
    }
}
