package uz.nodirbek.receiptdelivery.ui

data class OrderStatusStep(val label: String, val active: Boolean, val index: Int)

val STATUS_LABELS = listOf("Принят", "Собирается", "В пути", "Доставлен")
