package uz.nodirbek.receiptdelivery.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mapkit.geometry.Point
import uz.nodirbek.receiptdelivery.data.Order
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.ui.OrderStatusStep
import uz.nodirbek.receiptdelivery.ui.STATUS_LABELS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Order history and delivery-tracking status. Stays androidMain: currentOrderPoint() and
 *  placeOrder() work in terms of Yandex MapKit's Point (map integration is a later phase). */
class OrderState {
    var orderStatusIdx by mutableStateOf(0)
    var orderCardOpen by mutableStateOf(true)
    val orders = mutableStateListOf<Order>()

    fun orderStatuses(): List<OrderStatusStep> = STATUS_LABELS.mapIndexed { i, label ->
        OrderStatusStep(label, i <= orderStatusIdx, i)
    }

    fun mapLabel(): String =
        if (orderStatusIdx >= 2) "карта: курьер в пути" else "карта появится, когда курьер выедет"

    fun advanceOrderStatus() {
        orderStatusIdx = minOf(STATUS_LABELS.size - 1, orderStatusIdx + 1)
        if (orders.isNotEmpty()) {
            orders[0] = orders[0].copy(statusLabel = STATUS_LABELS[orderStatusIdx])
        }
    }

    fun currentOrderId(): String = orders.firstOrNull()?.id ?: "—"

    /** The point to show on the tracking map: the order's own saved location, not whatever the live selected address is now. */
    fun currentOrderPoint(fallback: Point): Point = orders.firstOrNull()?.let { Point(it.lat, it.lon) } ?: fallback

    fun placeOrder(recipe: Recipe, itemCount: Int, totalLabel: String, district: String, point: Point) {
        val order = Order(
            id = (2481 + orders.size).toString(),
            recipeName = recipe.name,
            itemsSummary = "$itemCount позиции",
            totalLabel = totalLabel,
            dateLabel = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date()),
            statusLabel = STATUS_LABELS[0],
            district = district,
            lat = point.latitude,
            lon = point.longitude
        )
        orders.add(0, order)
        orderStatusIdx = 0
        orderCardOpen = true
    }
}
