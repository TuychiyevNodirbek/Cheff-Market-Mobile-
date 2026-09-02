package uz.nodirbek.receiptdelivery.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.nodirbek.receiptdelivery.androidAppContext

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

@Composable
actual fun rememberLocationPermissionState(onResult: (granted: Boolean) -> Unit): LocationPermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasLocationPermission(context)) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val isGranted = results.values.any { it }
        granted = isGranted
        onResult(isGranted)
    }
    return remember(granted) {
        LocationPermissionState(granted) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}

actual suspend fun reverseGeocodeAddress(latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val results = Geocoder(androidAppContext, java.util.Locale("ru")).getFromLocation(latitude, longitude, 1)
            results?.firstOrNull()?.let { addr ->
                addr.getAddressLine(0)
                    ?: listOfNotNull(addr.thoroughfare, addr.subThoroughfare, addr.locality)
                        .joinToString(", ")
                        .ifBlank { null }
            }
        } catch (_: Exception) {
            null
        }
    }
