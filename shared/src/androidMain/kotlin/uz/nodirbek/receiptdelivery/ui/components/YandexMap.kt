package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference
import uz.nodirbek.receiptdelivery.geo.GeoPoint
import uz.nodirbek.receiptdelivery.geo.TASHKENT_CENTER

private fun GeoPoint.toYandexPoint() = Point(latitude, longitude)
private fun Point.toGeoPoint() = GeoPoint(latitude, longitude)

/**
 * A Yandex MapKit map view wired into Compose lifecycle.
 * Only one instance is ever visible at a time in this app (single-screen navigation),
 * so tying MapKitFactory's start/stop to this composable's own presence in composition
 * mirrors the standard Activity onStart/onStop pairing MapKit expects.
 *
 * Takes/returns GeoPoint (not Yandex's own Point) at its public boundary - callers and the
 * shared state layer stay map-SDK-agnostic; the Yandex-specific conversion lives only here.
 */
@Composable
fun YandexMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint = TASHKENT_CENTER,
    zoom: Float = 12f,
    markerAt: GeoPoint? = null
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    DisposableEffect(mapView) {
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        onDispose {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { view ->
            view.map.move(CameraPosition(center.toYandexPoint(), zoom, 0f, 0f))
            view.map.mapObjects.clear()
            markerAt?.let { point -> view.map.mapObjects.addPlacemark(point.toYandexPoint()) }
        }
    )
}

/**
 * A full-screen "pin drop" location picker backed by Yandex MapKit's own
 * [com.yandex.mapkit.user_location.UserLocationLayer] for GPS (rather than raw
 * [android.location.LocationManager]), so the blue dot / accuracy circle matches what
 * MapKit itself renders. The pin stays screen-centered while the map pans underneath it;
 * the caller renders that pin as a Compose overlay and reads the resolved point from
 * [onCameraTargetChanged].
 *
 * Runtime location permission must already be granted before [locateMeRequest] is
 * incremented — this composable does not request permissions itself.
 */
@Composable
fun YandexLocationPickerMap(
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint = TASHKENT_CENTER,
    initialZoom: Float = 13f,
    locationPermissionGranted: Boolean,
    locateMeRequest: Int,
    onCameraTargetChanged: (GeoPoint) -> Unit,
    onUserLocationFound: (GeoPoint) -> Unit = {},
    onUserLocationUnavailable: () -> Unit = {}
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val userLocationLayer = remember { MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow) }
    val cameraListenerRef = remember {
        WeakReference(CameraListener { _, position, _, _ -> onCameraTargetChanged(position.target.toGeoPoint()) })
    }

    DisposableEffect(mapView) {
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        mapView.map.move(CameraPosition(initialCenter.toYandexPoint(), initialZoom, 0f, 0f))
        mapView.map.addCameraListener(cameraListenerRef)
        onDispose {
            mapView.map.removeCameraListener(cameraListenerRef)
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        userLocationLayer.isVisible = locationPermissionGranted
    }

    LaunchedEffect(locateMeRequest) {
        if (locateMeRequest > 0 && locationPermissionGranted) {
            userLocationLayer.isVisible = true
            var position: CameraPosition? = null
            var attempts = 0
            while (position == null && attempts < 15) {
                position = userLocationLayer.cameraPosition()
                if (position == null) {
                    delay(300)
                    attempts++
                }
            }
            if (position != null) {
                mapView.map.move(
                    CameraPosition(position.target, 16f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0.6f),
                    null
                )
                onUserLocationFound(position.target.toGeoPoint())
            } else {
                onUserLocationUnavailable()
            }
        }
    }

    AndroidView(modifier = modifier.fillMaxSize(), factory = { mapView })
}
