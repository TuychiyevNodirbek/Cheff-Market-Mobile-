package uz.nodirbek.receiptdelivery.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russhwolf.settings.SharedPreferencesSettings
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.delay
import uz.nodirbek.receiptdelivery.data.cartPrefsName
import uz.nodirbek.receiptdelivery.data.loadAddresses
import uz.nodirbek.receiptdelivery.data.loadAuth
import uz.nodirbek.receiptdelivery.data.loadCartSnapshot
import uz.nodirbek.receiptdelivery.data.loadLastGpsPoint
import uz.nodirbek.receiptdelivery.data.loadOrders
import uz.nodirbek.receiptdelivery.data.loadSettings
import uz.nodirbek.receiptdelivery.data.saveAddresses
import uz.nodirbek.receiptdelivery.data.saveAuth
import uz.nodirbek.receiptdelivery.data.saveCartSnapshot
import uz.nodirbek.receiptdelivery.data.saveLastGpsPoint
import uz.nodirbek.receiptdelivery.data.saveOrders
import uz.nodirbek.receiptdelivery.data.saveSettings
import uz.nodirbek.receiptdelivery.ui.components.NoInternetScreen
import uz.nodirbek.receiptdelivery.ui.components.connectivityState
import uz.nodirbek.receiptdelivery.ui.screens.AddressesScreen
import uz.nodirbek.receiptdelivery.ui.screens.AuthOtpScreen
import uz.nodirbek.receiptdelivery.ui.screens.AuthPhoneScreen
import uz.nodirbek.receiptdelivery.ui.screens.AuthProfileScreen
import uz.nodirbek.receiptdelivery.ui.screens.CartScreen
import uz.nodirbek.receiptdelivery.ui.screens.CheckoutScreen
import uz.nodirbek.receiptdelivery.ui.screens.CookingScreen
import uz.nodirbek.receiptdelivery.ui.screens.DistrictSelectScreen
import uz.nodirbek.receiptdelivery.ui.screens.HomeScreen
import uz.nodirbek.receiptdelivery.ui.screens.OffZoneScreen
import uz.nodirbek.receiptdelivery.ui.screens.Onboarding1Screen
import uz.nodirbek.receiptdelivery.ui.screens.Onboarding2Screen
import uz.nodirbek.receiptdelivery.ui.screens.OrderHistoryScreen
import uz.nodirbek.receiptdelivery.ui.screens.ProfileScreen
import uz.nodirbek.receiptdelivery.ui.screens.RecipeDetailScreen
import uz.nodirbek.receiptdelivery.ui.screens.SearchScreen
import uz.nodirbek.receiptdelivery.ui.screens.SettingsScreen
import uz.nodirbek.receiptdelivery.ui.screens.TrackingScreen
import uz.nodirbek.receiptdelivery.ui.theme.CookingBg
import uz.nodirbek.receiptdelivery.ui.theme.Orange
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

private val TAB_SCREENS = mapOf(
    "home" to Screen.HOME,
    "search" to Screen.SEARCH,
    "tracking" to Screen.TRACKING,
    "profile" to Screen.PROFILE
)

private val SHOW_TAB_BAR_SCREENS = setOf(Screen.HOME, Screen.SEARCH, Screen.TRACKING, Screen.PROFILE)

/** Mirrors each screen's on-screen back arrow. Null means the system default (exit app) applies. */
private fun previousScreen(state: AppState): Screen? = when (state.screen) {
    Screen.ONB2 -> Screen.ONB1
    Screen.AUTH_PHONE -> Screen.ONB2
    Screen.AUTH_OTP -> Screen.AUTH_PHONE
    Screen.AUTH_PROFILE -> Screen.AUTH_OTP
    Screen.DISTRICT -> state.pickerBackTarget
    Screen.OFFZONE -> Screen.DISTRICT
    Screen.SEARCH -> Screen.HOME
    Screen.RECIPE -> Screen.HOME
    Screen.CART -> Screen.RECIPE
    Screen.CHECKOUT -> Screen.CART
    Screen.COOKING -> Screen.RECIPE
    Screen.ORDER_HISTORY -> Screen.PROFILE
    Screen.ADDRESSES -> state.addressesReturnTarget
    Screen.SETTINGS -> Screen.PROFILE
    Screen.ONB1, Screen.HOME, Screen.TRACKING, Screen.PROFILE -> null
}

@Composable
fun RecipeApp() {
    val isOnline by connectivityState()
    if (!isOnline) {
        NoInternetScreen(onRetry = {})
        return
    }

    val context = LocalContext.current
    val cartPrefs = remember {
        SharedPreferencesSettings(context.getSharedPreferences(cartPrefsName(), android.content.Context.MODE_PRIVATE))
    }
    val state = remember {
        AppState().apply {
            cartPrefs.loadCartSnapshot()?.let { applyCartSnapshot(it) }
            orders.addAll(cartPrefs.loadOrders())
            val (addresses, activeId) = cartPrefs.loadAddresses()
            if (addresses.isNotEmpty()) applySavedAddresses(addresses, activeId)
            cartPrefs.loadLastGpsPoint()?.let { (lat, lon) -> lastGpsPoint = Point(lat, lon) }
            applyAuthSnapshot(cartPrefs.loadAuth())
            applySettingsSnapshot(cartPrefs.loadSettings())
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.cartSnapshot() }.collect { snapshot ->
            cartPrefs.saveCartSnapshot(snapshot)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.orders.toList() }.collect { orders ->
            cartPrefs.saveOrders(orders)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.savedAddresses.toList() to state.activeAddressId }.collect { (addresses, activeId) ->
            cartPrefs.saveAddresses(addresses, activeId)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.lastGpsPoint }.collect { point ->
            point?.let { cartPrefs.saveLastGpsPoint(it.latitude, it.longitude) }
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.authSnapshot() }.collect { snapshot ->
            cartPrefs.saveAuth(snapshot)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.settingsSnapshot() }.collect { snapshot ->
            cartPrefs.saveSettings(snapshot)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            state.tickTimer()
        }
    }

    val target = previousScreen(state)
    BackHandler(enabled = target != null) {
        target?.let { state.go(it) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (state.screen == Screen.COOKING) CookingBg else Surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (state.screen) {
            Screen.ONB1 -> Onboarding1Screen(state)
            Screen.ONB2 -> Onboarding2Screen(state)
            Screen.AUTH_PHONE -> AuthPhoneScreen(state)
            Screen.AUTH_OTP -> AuthOtpScreen(state)
            Screen.AUTH_PROFILE -> AuthProfileScreen(state)
            Screen.DISTRICT -> DistrictSelectScreen(state)
            Screen.OFFZONE -> OffZoneScreen(state)
            Screen.HOME -> HomeScreen(state)
            Screen.SEARCH -> SearchScreen(state)
            Screen.RECIPE -> RecipeDetailScreen(state)
            Screen.CART -> CartScreen(state)
            Screen.CHECKOUT -> CheckoutScreen(state)
            Screen.TRACKING -> TrackingScreen(state)
            Screen.COOKING -> CookingScreen(state)
            Screen.PROFILE -> ProfileScreen(state)
            Screen.ORDER_HISTORY -> OrderHistoryScreen(state)
            Screen.ADDRESSES -> AddressesScreen(state)
            Screen.SETTINGS -> SettingsScreen(state)
        }
        if (state.screen in SHOW_TAB_BAR_SCREENS) {
            BottomTabBar(state, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun BottomTabBar(state: AppState, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(top = 8.dp, bottom = 22.dp)
    ) {
        TAB_DEFS.forEach { (key, icon, name) ->
            val screen = TAB_SCREENS.getValue(key)
            val active = state.screen == screen
            val interactionSource = remember { MutableInteractionSource() }
            Column(
                Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { state.go(screen) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(icon, fontSize = 20.sp)
                Text(
                    name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) Orange else TextMuted,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}
