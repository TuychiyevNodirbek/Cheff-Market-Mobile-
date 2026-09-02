package uz.nodirbek.receiptdelivery.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uz.nodirbek.receiptdelivery.geo.GeoPoint
import kotlinx.coroutines.delay
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

@Composable
fun RecipeApp() {
    val isOnline by connectivityState()
    if (!isOnline) {
        NoInternetScreen(onRetry = {})
        return
    }

    val cartPrefs = rememberPlatformSettings()
    // Read once, synchronously, before the nav graph is built - decides the start destination
    // below without ever flashing the onboarding screen for a returning, authenticated user.
    val loadedAuth = remember { cartPrefs.loadAuth() }

    val navController = rememberNavController()
    val state = remember {
        AppState(navController).apply {
            cartPrefs.loadCartSnapshot()?.let { applyCartSnapshot(it) }
            orders.addAll(cartPrefs.loadOrders())
            val (addresses, activeId) = cartPrefs.loadAddresses()
            if (addresses.isNotEmpty()) applySavedAddresses(addresses, activeId)
            cartPrefs.loadLastGpsPoint()?.let { (lat, lon) -> lastGpsPoint = GeoPoint(lat, lon) }
            applyAuthSnapshot(loadedAuth)
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

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route?.let { Screen.valueOf(it) }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (currentScreen == Screen.COOKING) CookingBg else Surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        NavHost(navController = navController, startDestination = if (loadedAuth.isAuthenticated) Screen.HOME.name else Screen.ONB1.name) {
            composable(Screen.ONB1.name) { Onboarding1Screen(state) }
            composable(Screen.ONB2.name) { Onboarding2Screen(state) }
            composable(Screen.AUTH_PHONE.name) { AuthPhoneScreen(state) }
            composable(Screen.AUTH_OTP.name) { AuthOtpScreen(state) }
            composable(Screen.AUTH_PROFILE.name) { AuthProfileScreen(state) }
            composable(Screen.DISTRICT.name) { DistrictSelectScreen(state) }
            composable(Screen.OFFZONE.name) { OffZoneScreen(state) }
            composable(Screen.HOME.name) { HomeScreen(state) }
            composable(Screen.SEARCH.name) { SearchScreen(state) }
            composable(Screen.RECIPE.name) { RecipeDetailScreen(state) }
            composable(Screen.CART.name) { CartScreen(state) }
            composable(Screen.CHECKOUT.name) { CheckoutScreen(state) }
            composable(Screen.TRACKING.name) { TrackingScreen(state) }
            composable(Screen.COOKING.name) { CookingScreen(state) }
            composable(Screen.PROFILE.name) { ProfileScreen(state) }
            composable(Screen.ORDER_HISTORY.name) { OrderHistoryScreen(state) }
            composable(Screen.ADDRESSES.name) { AddressesScreen(state) }
            composable(Screen.SETTINGS.name) { SettingsScreen(state) }
        }
        if (currentScreen != null && currentScreen in SHOW_TAB_BAR_SCREENS) {
            BottomTabBar(state, currentScreen, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun BottomTabBar(state: AppState, currentScreen: Screen?, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(top = 8.dp, bottom = 22.dp)
    ) {
        TAB_DEFS.forEach { (key, icon, name) ->
            val screen = TAB_SCREENS.getValue(key)
            val active = currentScreen == screen
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
