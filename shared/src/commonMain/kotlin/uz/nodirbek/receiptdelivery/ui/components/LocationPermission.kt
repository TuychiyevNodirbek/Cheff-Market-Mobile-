package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable

class LocationPermissionState(val granted: Boolean, val request: () -> Unit)

/** `onResult` fires once per request, with the outcome of that specific request - lets the
 *  caller distinguish "just granted, go locate me" from "denied, show an error" without polling. */
@Composable
expect fun rememberLocationPermissionState(onResult: (granted: Boolean) -> Unit): LocationPermissionState

/** Reverse-geocodes a point into a human-readable address line, or null if unavailable. */
expect suspend fun reverseGeocodeAddress(latitude: Double, longitude: Double): String?
