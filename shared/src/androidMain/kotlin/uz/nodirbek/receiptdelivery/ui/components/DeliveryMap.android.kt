package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uz.nodirbek.receiptdelivery.geo.GeoPoint

@Composable
actual fun DeliveryMap(modifier: Modifier, center: GeoPoint, zoom: Float, markerAt: GeoPoint?) {
    YandexMapView(modifier = modifier, center = center, zoom = zoom, markerAt = markerAt)
}

@Composable
actual fun LocationPickerMap(
    modifier: Modifier,
    initialCenter: GeoPoint,
    initialZoom: Float,
    locationPermissionGranted: Boolean,
    locateMeRequest: Int,
    onCameraTargetChanged: (GeoPoint) -> Unit,
    onUserLocationFound: (GeoPoint) -> Unit,
    onUserLocationUnavailable: () -> Unit
) {
    YandexLocationPickerMap(
        modifier = modifier,
        initialCenter = initialCenter,
        initialZoom = initialZoom,
        locationPermissionGranted = locationPermissionGranted,
        locateMeRequest = locateMeRequest,
        onCameraTargetChanged = onCameraTargetChanged,
        onUserLocationFound = onUserLocationFound,
        onUserLocationUnavailable = onUserLocationUnavailable
    )
}
