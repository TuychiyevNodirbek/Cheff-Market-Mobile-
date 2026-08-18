package uz.nodirbek.receiptdelivery.data

import com.russhwolf.settings.Settings

data class Order(
    val id: String,
    val recipeName: String,
    val itemsSummary: String,
    val totalLabel: String,
    val dateLabel: String,
    val statusLabel: String,
    val district: String,
    val lat: Double,
    val lon: Double
)

private const val KEY_ORDERS = "orders"
private const val FIELD_SEP = "|"
private const val ORDER_SEP = "\n"

fun Settings.saveOrders(orders: List<Order>) {
    putString(
        KEY_ORDERS,
        orders.joinToString(ORDER_SEP) { o ->
            listOf(o.id, o.recipeName, o.itemsSummary, o.totalLabel, o.dateLabel, o.statusLabel, o.district, o.lat, o.lon)
                .joinToString(FIELD_SEP)
        }
    )
}

fun Settings.loadOrders(): List<Order> {
    val raw = getString(KEY_ORDERS, "")
    if (raw.isBlank()) return emptyList()
    return raw.split(ORDER_SEP).filter { it.isNotBlank() }.mapNotNull { line ->
        val parts = line.split(FIELD_SEP)
        if (parts.size != 9) return@mapNotNull null
        val lat = parts[7].toDoubleOrNull() ?: return@mapNotNull null
        val lon = parts[8].toDoubleOrNull() ?: return@mapNotNull null
        Order(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], lat, lon)
    }
}
