package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.status_cancelled
import uz.nodirbek.receiptdelivery.shared.resources.status_confirmed
import uz.nodirbek.receiptdelivery.shared.resources.status_created
import uz.nodirbek.receiptdelivery.shared.resources.status_delivered
import uz.nodirbek.receiptdelivery.shared.resources.status_delivering
import uz.nodirbek.receiptdelivery.shared.resources.status_packed
import uz.nodirbek.receiptdelivery.shared.resources.status_picking

/** The backend's `Order.status` values aren't formally enumerated in the mobile API doc, so unknown
 *  codes fall back to a capitalized version of the raw value instead of a hidden/blank label. */
@Composable
fun orderStatusLabel(status: String): String = when (status) {
    "created" -> stringResource(Res.string.status_created)
    "confirmed" -> stringResource(Res.string.status_confirmed)
    "picking" -> stringResource(Res.string.status_picking)
    "packed" -> stringResource(Res.string.status_packed)
    "delivering" -> stringResource(Res.string.status_delivering)
    "delivered" -> stringResource(Res.string.status_delivered)
    "cancelled" -> stringResource(Res.string.status_cancelled)
    else -> status.replaceFirstChar { it.uppercase() }
}

fun orderStatusIsFinal(status: String): Boolean = status == "delivered" || status == "cancelled"
