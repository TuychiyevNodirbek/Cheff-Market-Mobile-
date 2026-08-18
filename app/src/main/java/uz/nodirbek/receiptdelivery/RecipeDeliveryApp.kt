package uz.nodirbek.receiptdelivery

import android.app.Application
import com.yandex.mapkit.MapKitFactory

private const val YANDEX_MAPKIT_API_KEY = "a8459245-4fdc-4c94-988f-a6a4911af25e"

class RecipeDeliveryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(YANDEX_MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}
