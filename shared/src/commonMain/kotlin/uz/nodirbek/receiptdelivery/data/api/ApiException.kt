package uz.nodirbek.receiptdelivery.data.api

import kotlinx.serialization.json.JsonElement

/** Thrown for any non-2xx response, parsed from the API's `{"error": {...}}` envelope. */
class ApiException(
    val code: Int,
    override val message: String,
    val details: JsonElement? = null
) : Exception(message)
