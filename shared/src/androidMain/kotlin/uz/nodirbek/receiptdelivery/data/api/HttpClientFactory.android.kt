package uz.nodirbek.receiptdelivery.data.api

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import uz.nodirbek.receiptdelivery.androidAppContext

actual fun platformHttpClientEngine(): HttpClientEngine = OkHttp.create {
    addInterceptor(
        ChuckerInterceptor.Builder(androidAppContext)
            .collector(ChuckerCollector(androidAppContext))
            .maxContentLength(250_000L)
            .build()
    )
}
