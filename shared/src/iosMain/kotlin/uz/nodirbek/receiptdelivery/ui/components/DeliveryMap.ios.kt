package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import uz.nodirbek.receiptdelivery.geo.GeoPoint

/**
 * Deliberate placeholder, not a real map - see docs/ios-phase-plan.md §4. No iOS map SDK
 * (MapLibre Compose Multiplatform, Apple MapKit, ...) has been wired up: writing fake
 * cinterop/native map code with no way to compile or run it on this machine would be worse
 * than an honest "not implemented yet" box. Replace this file once a real map is chosen.
 */
@Composable
actual fun DeliveryMap(modifier: Modifier, center: GeoPoint, zoom: Float, markerAt: GeoPoint?) {
    Box(modifier.fillMaxSize().background(Color(0xFFE0DAD2)), contentAlignment = Alignment.Center) {
        Text("Карта (iOS): пока не подключена")
    }
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
    // Reports the initial center once so callers relying on onCameraTargetChanged (e.g. district
    // resolution) still have a point to work with, even though the map itself is a placeholder.
    LaunchedEffect(initialCenter) { onCameraTargetChanged(initialCenter) }
    Box(modifier.fillMaxSize().background(Color(0xFFE0DAD2)), contentAlignment = Alignment.Center) {
        Text("Карта (iOS): пока не подключена")
    }
}
