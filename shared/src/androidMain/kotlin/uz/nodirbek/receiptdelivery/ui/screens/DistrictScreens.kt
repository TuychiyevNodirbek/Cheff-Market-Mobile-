package uz.nodirbek.receiptdelivery.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import uz.nodirbek.receiptdelivery.data.DISTRICTS
import uz.nodirbek.receiptdelivery.data.nearestDistrict
import uz.nodirbek.receiptdelivery.ui.AppState
import uz.nodirbek.receiptdelivery.ui.Screen
import uz.nodirbek.receiptdelivery.ui.components.BackButton
import uz.nodirbek.receiptdelivery.ui.components.PlaceholderBlock
import uz.nodirbek.receiptdelivery.ui.components.PrimaryButton
import uz.nodirbek.receiptdelivery.geo.TASHKENT_CENTER
import uz.nodirbek.receiptdelivery.ui.components.YandexLocationPickerMap
import uz.nodirbek.receiptdelivery.ui.theme.Border
import uz.nodirbek.receiptdelivery.ui.theme.CardWhite
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

/** Reverse-geocodes a point into a human-readable address line, or null if unavailable. */
private suspend fun reverseGeocodeAddress(context: android.content.Context, lat: Double, lon: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val results = Geocoder(context, java.util.Locale("ru")).getFromLocation(lat, lon, 1)
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

@Composable
fun DistrictSelectScreen(state: AppState) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var locateRequest by remember { mutableIntStateOf(0) }
    var locatingFailed by remember { mutableStateOf(false) }
    val startCenter = remember {
        state.deliveryPoint ?: state.lastGpsPoint ?: TASHKENT_CENTER
    }
    var cameraTarget by remember { mutableStateOf(startCenter) }
    var addressText by remember {
        mutableStateOf(if (state.pickerAddNew) "" else state.activeAddress()?.fullAddress ?: "")
    }
    var addressEditedByUser by remember { mutableStateOf(addressText.isNotBlank()) }
    var geocoding by remember { mutableStateOf(false) }

    // Auto-fill the address text from Yandex-backed reverse geocoding as the pin settles,
    // unless the user has already typed/edited it themselves.
    LaunchedEffect(cameraTarget) {
        delay(600)
        if (addressEditedByUser) return@LaunchedEffect
        geocoding = true
        val resolved = reverseGeocodeAddress(context, cameraTarget.latitude, cameraTarget.longitude)
        geocoding = false
        if (!addressEditedByUser && !resolved.isNullOrBlank()) {
            addressText = resolved
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) {
            hasPermission = true
            locatingFailed = false
            locateRequest++
        } else {
            locatingFailed = true
        }
    }

    fun locateMe() {
        if (hasPermission) {
            locatingFailed = false
            locateRequest++
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    val resolvedDistrict = nearestDistrict(cameraTarget.latitude, cameraTarget.longitude)

    Box(Modifier.fillMaxSize().background(Surface)) {
        YandexLocationPickerMap(
            modifier = Modifier.fillMaxSize(),
            initialCenter = startCenter,
            locationPermissionGranted = hasPermission,
            locateMeRequest = locateRequest,
            onCameraTargetChanged = { cameraTarget = it },
            onUserLocationFound = { point -> state.lastGpsPoint = point },
            onUserLocationUnavailable = { locatingFailed = true }
        )

        // Center-fixed pin: the map pans underneath it, matching typical "drop pin" pickers.
        Text(
            "📍",
            fontSize = 40.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp)
        )

        Row(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Surface.copy(alpha = 0.96f))
                .padding(start = 12.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { state.go(state.pickerBackTarget) })
            Column(Modifier.padding(start = 4.dp)) {
                Text("Где вам удобно получать заказ?", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text("Двигайте карту, чтобы указать точку доставки", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 96.dp, end = 20.dp)
                .size(48.dp)
                .background(CardWhite, CircleShape)
                .clip(CircleShape)
                .clickable { locateMe() },
            contentAlignment = Alignment.Center
        ) {
            Text("🎯", fontSize = 20.sp)
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp)
        ) {
            Text(
                if (geocoding) "Определяем адрес…" else "Адрес доставки",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = addressText,
                onValueChange = {
                    addressText = it
                    addressEditedByUser = true
                },
                placeholder = { Text("ул. Амира Темура 12, кв. 34") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Orange,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )
            if (locatingFailed) {
                Text("Не удалось определить геолокацию", fontSize = 12.sp, color = Color(0xFFB8431A), modifier = Modifier.padding(bottom = 8.dp))
            }
            PrimaryButton(
                "Подтвердить адрес",
                onClick = { state.selectDistrict(resolvedDistrict, cameraTarget, addressText.trim()) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OffZoneScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(Surface).padding(horizontal = 24.dp, vertical = 28.dp)) {
        TextButton(onClick = { state.go(Screen.DISTRICT) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Text("Назад", color = TextMuted, fontSize = 15.sp, modifier = Modifier.padding(start = 6.dp))
        }
        PlaceholderBlock(
            modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 16.dp, bottom = 20.dp),
            label = "иллюстрация: карта, зона",
            color1 = Border,
            color2 = uz.nodirbek.receiptdelivery.ui.theme.Amber.copy(alpha = 0.25f)
        )
        Text("Мы пока не доставляем сюда", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
        Text("Сейчас работаем в следующих районах:", fontSize = 14.sp, color = TextMuted, modifier = Modifier.padding(bottom = 16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
            DISTRICTS.forEach { n ->
                Box(Modifier.background(Border, RoundedCornerShape(24.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(n, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
            }
        }
        Text("Уведомить, когда запустимся", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark, modifier = Modifier.padding(bottom = 10.dp))
        var phone by remember { mutableStateOf("") }
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("Ваш номер телефона") },
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Border,
                focusedBorderColor = Orange,
                unfocusedContainerColor = uz.nodirbek.receiptdelivery.ui.theme.CardWhite,
                focusedContainerColor = uz.nodirbek.receiptdelivery.ui.theme.CardWhite
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        PrimaryButton("Уведомить меня", onClick = { state.go(Screen.HOME) }, modifier = Modifier.fillMaxWidth())
    }
}
