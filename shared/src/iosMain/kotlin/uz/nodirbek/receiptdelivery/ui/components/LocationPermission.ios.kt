package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable

/**
 * Deliberate placeholder - see docs/ios-phase-plan.md §3.3. Real location permission on iOS
 * needs CLLocationManager + its delegate protocol, which involves async callback wiring that
 * can't be written blind with any confidence on a machine with no Kotlin/Native iOS compiler.
 * Always reports "not granted" and request() does nothing; the "locate me" button on the
 * district picker will be inert on iOS until this is replaced on a Mac.
 */
@Composable
actual fun rememberLocationPermissionState(onResult: (granted: Boolean) -> Unit): LocationPermissionState =
    LocationPermissionState(granted = false, request = {})

/** Deliberate placeholder - see docs/ios-phase-plan.md §3.3. Real reverse geocoding on iOS
 *  needs CLGeocoder, same caveat as rememberLocationPermissionState above. */
actual suspend fun reverseGeocodeAddress(latitude: Double, longitude: Double): String? = null
