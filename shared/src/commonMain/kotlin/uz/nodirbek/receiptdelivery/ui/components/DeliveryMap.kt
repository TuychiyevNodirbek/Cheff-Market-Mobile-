package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uz.nodirbek.receiptdelivery.geo.GeoPoint

/**
 * Read-only map view: shows `center`, optionally with a pin at `markerAt`. Used for the small
 * "delivery address" preview on Checkout/Tracking - not the interactive picker (see
 * [LocationPickerMap]).
 *
 * Android actual wraps Yandex MapKit (ui/components/YandexMap.kt). No iOS map SDK is wired up
 * yet - see docs/ios-phase-plan.md §4 - the iOS actual is a placeholder, not a real map.
 */
@Composable
expect fun DeliveryMap(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Float = 12f,
    markerAt: GeoPoint? = null
)

/**
 * Full-screen "pin drop" location picker: the pin stays screen-centered while the map pans
 * underneath it, the caller reads the resolved point from [onCameraTargetChanged].
 *
 * Android actual wraps Yandex MapKit's own user-location layer (ui/components/YandexMap.kt).
 * iOS actual is a placeholder - see docs/ios-phase-plan.md §4.
 */
@Composable
expect fun LocationPickerMap(
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint,
    initialZoom: Float = 13f,
    locationPermissionGranted: Boolean,
    locateMeRequest: Int,
    onCameraTargetChanged: (GeoPoint) -> Unit,
    onUserLocationFound: (GeoPoint) -> Unit = {},
    onUserLocationUnavailable: () -> Unit = {}
)
