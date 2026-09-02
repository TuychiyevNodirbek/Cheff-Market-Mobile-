package uz.nodirbek.receiptdelivery.geo

/**
 * Platform-agnostic latitude/longitude pair.
 *
 * Business/state logic (district selection, delivery address, order tracking) only ever
 * needs a coordinate pair - it never calls into a map SDK directly. Keeping that logic in
 * terms of GeoPoint instead of a specific map library's point type (Yandex MapKit's
 * com.yandex.mapkit.geometry.Point on Android today) is what lets AppState's location/order
 * state live in commonMain: the conversion to/from a map SDK's own point type happens only
 * at the UI boundary, inside that SDK's own map composable (see ui/components/YandexMap.kt).
 */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** Tashkent city center - default map/location fallback when nothing better is known yet. */
val TASHKENT_CENTER = GeoPoint(41.311081, 69.240562)
