package uz.nodirbek.receiptdelivery.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import uz.nodirbek.receiptdelivery.data.ApiConfig

val apiJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    // The backend sends explicit `null` for optional string fields (e.g. a recipe with no photo
    // yet has `"image": null`) rather than omitting them; without this, kotlinx.serialization
    // throws on that null instead of falling back to the field's "" default, failing the whole
    // list decode.
    coerceInputValues = true
}

/** Android: OkHttp wired to Chucker (debug) so requests show up in its notification/UI. iOS: Darwin. */
expect fun platformHttpClientEngine(): HttpClientEngine

fun createHttpClient(): HttpClient = HttpClient(platformHttpClientEngine()) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(apiJson)
    }
    install(Logging) {
        level = LogLevel.INFO
    }
    defaultRequest {
        url(ApiConfig.MOBILE_API_URL)
    }
}
