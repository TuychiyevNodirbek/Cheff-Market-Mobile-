package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uz.nodirbek.receiptdelivery.data.Order
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.geo.GeoPoint

private val RU_MONTHS_SHORT = listOf(
    "янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
)

/** "18 авг, 12:00" - multiplatform-safe replacement for java.text.SimpleDateFormat,
 *  which doesn't exist outside the JVM (would break the iOS target). */
private fun currentOrderDateLabel(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val month = RU_MONTHS_SHORT[now.monthNumber - 1]
    val hh = now.hour.toString().padStart(2, '0')
    val mm = now.minute.toString().padStart(2, '0')
    return "${now.dayOfMonth} $month, $hh:$mm"
}

/** Order history and delivery-tracking status. */
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
    fun currentOrderPoint(fallback: GeoPoint): GeoPoint = orders.firstOrNull()?.let { GeoPoint(it.lat, it.lon) } ?: fallback

    fun placeOrder(recipe: Recipe, itemCount: Int, totalLabel: String, district: String, point: GeoPoint) {
        val order = Order(
            id = (2481 + orders.size).toString(),
            recipeName = recipe.name,
            itemsSummary = "$itemCount позиции",
            totalLabel = totalLabel,
            dateLabel = currentOrderDateLabel(),
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
