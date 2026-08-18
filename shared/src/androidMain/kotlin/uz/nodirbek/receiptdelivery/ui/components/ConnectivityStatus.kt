package uz.nodirbek.receiptdelivery.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

private fun ConnectivityManager.hasInternet(): Boolean {
    val network = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/** Tracks live internet availability, updating as the device connects/disconnects. */
@Composable
fun connectivityState(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = context.isInternetAvailable(), context) {
        val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return@produceState
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = connectivityManager.hasInternet()
            }

            override fun onLost(network: Network) {
                value = connectivityManager.hasInternet()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                value = connectivityManager.hasInternet()
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        awaitDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>() ?: return false
    return connectivityManager.hasInternet()
}
