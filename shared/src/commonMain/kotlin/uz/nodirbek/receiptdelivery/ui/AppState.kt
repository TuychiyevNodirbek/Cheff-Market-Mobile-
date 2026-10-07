package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.dietary_gluten_free
import uz.nodirbek.receiptdelivery.shared.resources.dietary_lactose_free
import uz.nodirbek.receiptdelivery.shared.resources.dietary_spicy
import uz.nodirbek.receiptdelivery.shared.resources.dietary_vegetarian
import uz.nodirbek.receiptdelivery.shared.resources.payment_cash
import uz.nodirbek.receiptdelivery.shared.resources.slot_today_1
import uz.nodirbek.receiptdelivery.shared.resources.slot_today_2
import uz.nodirbek.receiptdelivery.shared.resources.slot_tomorrow_evening
import uz.nodirbek.receiptdelivery.shared.resources.slot_tomorrow_morning
import uz.nodirbek.receiptdelivery.shared.resources.nav_home
import uz.nodirbek.receiptdelivery.shared.resources.nav_search
import uz.nodirbek.receiptdelivery.shared.resources.nav_tracking
import uz.nodirbek.receiptdelivery.shared.resources.nav_profile
import uz.nodirbek.receiptdelivery.data.AuthSnapshot
import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.SavedAddress
import uz.nodirbek.receiptdelivery.data.SettingsSnapshot
import uz.nodirbek.receiptdelivery.data.Step
import uz.nodirbek.receiptdelivery.data.api.AddressRequest
import uz.nodirbek.receiptdelivery.data.api.ApiDeliveryZone
import uz.nodirbek.receiptdelivery.data.api.ApiException
import uz.nodirbek.receiptdelivery.data.api.ApiOrderDetail
import uz.nodirbek.receiptdelivery.data.api.MobileApiService
import uz.nodirbek.receiptdelivery.geo.GeoPoint
import kotlin.math.roundToInt

val LANGUAGE_OPTIONS = listOf("ru" to "Русский", "uz" to "O'zbekcha")

/** Stable ids for dietary prefs - [SettingsState.dietaryPrefs] stores these, not the (localized)
 *  display label, so a language switch never invalidates what the user already picked. */
val DIETARY_OPTION_IDS = listOf("vegetarian", "gluten_free", "lactose_free", "spicy")

@Composable
fun dietaryOptionLabel(id: String): String = when (id) {
    "vegetarian" -> stringResource(Res.string.dietary_vegetarian)
    "gluten_free" -> stringResource(Res.string.dietary_gluten_free)
    "lactose_free" -> stringResource(Res.string.dietary_lactose_free)
    else -> stringResource(Res.string.dietary_spicy)
}

/** Stable slot ids match the backend's placeholder slot ids (see the doc comment further below,
 *  next to the old `SLOT_OPTIONS` constant this replaced) - only the display label is localized. */
@Composable
fun slotOptions(): List<Pair<String, String>> = listOf(
    "1" to stringResource(Res.string.slot_today_1),
    "2" to stringResource(Res.string.slot_today_2),
    "3" to stringResource(Res.string.slot_tomorrow_morning),
    "4" to stringResource(Res.string.slot_tomorrow_evening)
)

@Composable
fun paymentOptions(): List<Pair<String, String>> = listOf("cash" to stringResource(Res.string.payment_cash))

@Composable
fun tabDefs(): List<Triple<String, String, String>> = listOf(
    Triple("home", "🏠", stringResource(Res.string.nav_home)),
    Triple("search", "🔍", stringResource(Res.string.nav_search)),
    Triple("tracking", "🧾", stringResource(Res.string.nav_tracking)),
    Triple("profile", "👤", stringResource(Res.string.nav_profile))
)

data class ScaledIngredient(
    val key: String,
    val name: String,
    val initial: String,
    val qtyLabel: String,
    val statusLabel: String,
    val statusColor: androidx.compose.ui.graphics.Color
)

data class CartRow(
    val key: String,
    val name: String,
    val initial: String,
    val qtyLabel: String,
    val packLabel: String,
    val priceLabel: String,
    val price: Int,
    val count: Int,
    val substituted: Boolean,
    val subNote: String
)

data class RecipeCard(
    val id: String,
    val name: String,
    val time: Int,
    val baseServings: Int,
    val isFav: Boolean,
    val heroColors: Pair<Long, Long>,
    val imageRes: DrawableResource,
    val imageUrl: String? = null
)

// The mobile API doc has no `GET` endpoint to list real DeliverySlot rows/time windows (only that
// `POST /orders/create/` takes a slot id from the address's zone), so these ids/labels are a
// placeholder guess, not fetched from the backend - the id sent to createOrder may not resolve to
// a real slot outside a dev seed that happens to number them 1-4. Needs a slots-listing endpoint.
// See [slotOptions] above for the (localized) display labels.

// Online payment providers aren't wired up on the backend yet - `POST /orders/create/` only
// accepts "cash" and 400s on anything else, so that's the only real option here. See
// [paymentOptions] above for the (localized) display label.

/** Bottom-tab destinations always behave as a fresh stack root: back exits the app from any of
 *  them, and re-visiting one never grows the stack. Since all screen state lives in AppState
 *  (not nav-entry-scoped view models), clearing the back stack loses no in-progress UI state. */
private val TAB_ROOTS = setOf(Screen.HOME, Screen.SEARCH, Screen.TRACKING, Screen.PROFILE)

/**
 * Root coordinator: composes the per-feature state holders (auth/cart/order/cooking/settings/location)
 * and owns cross-cutting screen navigation via a real androidx.navigation back stack. Exposes the same
 * flat property/method surface the screens already use (via delegation), so splitting the old
 * god-object into focused classes - and later replacing the hand-rolled Screen-enum navigation with a
 * real NavController - didn't require touching any screen file.
 */
class AppState(private val navController: NavHostController, private val scope: CoroutineScope) {
    private val api = MobileApiService()
    private val auth = AuthState()
    private val cart = CartState()
    private val order = OrderState()
    private val cooking = CookingState()
    private val settings = SettingsState()
    private val location = LocationState()

    // Loaded lazily on first address sync and cached for the session - the zone list rarely
    // changes and every sync needs it to resolve `district name -> backend zone id`.
    private var deliveryZones: List<ApiDeliveryZone> = emptyList()
    var addressSyncError by mutableStateOf<String?>(null)
        private set

    // Resolved once from strings.xml by RecipeApp.kt right after construction (see AppMessages) -
    // needed by the suspend/error-handling paths below, which can't call stringResource directly.
    var messages: AppMessages = AppMessages.PLACEHOLDER

    // --- Auth -------------------------------------------------------------
    var isAuthenticated by auth::isAuthenticated
    var userPhone by auth::userPhone
    var userName by auth::userName
    var otpError by auth::otpError
    var authLoading by auth::isLoading
    var authErrorMessage by auth::errorMessage

    /** Calls `auth/request-otp/` on the backend, then advances to the code-entry screen on success. */
    fun submitPhone(phone: String) {
        auth.userPhone = phone
        auth.otpError = false
        auth.errorMessage = null
        auth.isLoading = true
        scope.launch {
            try {
                api.requestOtp(phone)
                go(Screen.AUTH_OTP)
            } catch (e: ApiException) {
                auth.errorMessage = e.message
            } catch (e: Exception) {
                auth.errorMessage = messages.errorSendCode
            } finally {
                auth.isLoading = false
            }
        }
    }

    /** Calls `auth/verify-otp/`; on success stores the returned token and proceeds to profile setup. */
    fun verifyOtp(code: String) {
        if (code.length != 4) {
            auth.otpError = true
            return
        }
        auth.otpError = false
        auth.errorMessage = null
        auth.isLoading = true
        scope.launch {
            try {
                val response = api.verifyOtp(auth.userPhone, code)
                auth.authToken = response.token
                api.authToken = response.token
                if (response.user.fullName.isNotBlank()) auth.userName = response.user.fullName
                go(Screen.AUTH_PROFILE)
            } catch (e: ApiException) {
                auth.otpError = true
                auth.errorMessage = e.message
            } catch (e: Exception) {
                auth.otpError = true
                auth.errorMessage = messages.errorVerifyCode
            } finally {
                auth.isLoading = false
            }
        }
    }

    fun completeAuth(name: String) {
        auth.userName = name.ifBlank { messages.guest }
        auth.isAuthenticated = true
        loadOrders()
        openLocationPicker(Screen.AUTH_PROFILE, Screen.HOME)
    }

    /** Clears the entire back stack so logging out can't be undone with the system back button. */
    fun logout() {
        auth.isAuthenticated = false
        auth.userName = ""
        auth.userPhone = ""
        auth.authToken = ""
        api.authToken = null
        order.clear()
        resetStackTo(Screen.ONB1)
    }

    fun authSnapshot(): AuthSnapshot = auth.snapshot()

    /** Pure field restore only - no navigation. The initial screen for a returning, already-authenticated
     *  user is chosen once as NavHost's startDestination in RecipeApp.kt, before this is even called. */
    fun applyAuthSnapshot(s: AuthSnapshot) {
        auth.isAuthenticated = s.isAuthenticated
        auth.userPhone = s.phone
        auth.userName = s.name
        auth.authToken = s.token
        api.authToken = s.token.ifBlank { null }
    }

    // --- Cart / recipe browsing --------------------------------------------
    var portions by cart::portions
    var recipeId by cart::recipeId
    val favs get() = cart.favs
    val activeChips get() = cart.activeChips
    var searchQuery by cart::searchQuery
    var stepsOpen by cart::stepsOpen
    val cartQty get() = cart.cartQty
    val cartRemoved get() = cart.cartRemoved
    val cartSubbed get() = cart.cartSubbed
    var onlyMissing by cart::onlyMissing
    var slot by cart::slot
    var comment by cart::comment
    val recipesLoading get() = cart.recipesLoading
    val recipesRefreshing get() = cart.recipesRefreshing
    val recipesError get() = cart.recipesError
    val recipeDetailLoading get() = cart.recipeDetailLoading
    val cartSyncError get() = cart.cartSyncError

    val recipe: Recipe get() = cart.recipe

    /** Fetches `GET /recipes/` once per session; safe to call from every screen that lists recipes. */
    fun loadRecipes() {
        scope.launch { cart.loadRecipes(api, messages) }
    }

    /** Re-fetches `GET /recipes/` regardless of cache, for pull-to-refresh gestures. */
    fun refreshRecipes() {
        scope.launch { cart.refreshRecipes(api, messages) }
    }

    fun selectRecipe(id: String) {
        scope.launch { cart.selectRecipe(api, id, messages) }
        go(Screen.RECIPE)
    }

    /** Adds the currently viewed recipe (at the current [portions]) to the server cart, then opens it. */
    fun goToCart() {
        scope.launch { cart.syncToServerCart(api, messages) }
        go(Screen.CART)
    }

    fun toggleFav(id: String) = cart.toggleFav(id)
    fun scaledIngredients() = cart.scaledIngredients(messages.inStock)
    fun cartTotalLabel(): String = cart.cartTotalLabel()
    fun buildCartRows() = cart.buildCartRows()
    fun incCartQty(key: String) = cart.incCartQty(key)
    fun decCartQty(key: String) = cart.decCartQty(key)
    fun removeCartItem(key: String) = cart.removeCartItem(key)
    fun undoSub(key: String) = cart.undoSub(key)
    fun cartSubtotal(): Int = cart.cartSubtotal()
    fun deliveryFee(): Int = cart.deliveryFee()
    fun cartGrandTotal(): Int = cart.cartGrandTotal()
    fun allRecipeCards() = cart.allRecipeCards()
    fun collectionCards() = cart.collectionCards()
    fun searchResults() = cart.searchResults()
    fun cartSnapshot(): CartSnapshot = cart.snapshot()
    fun applyCartSnapshot(s: CartSnapshot) = cart.applySnapshot(s)

    /** Best-effort real delivery fee for the selected district, from `GET /delivery-zones/`
     *  (cached alongside the address-sync lookup). Falls back silently on failure - the cart
     *  screen just keeps showing the placeholder fee. */
    fun refreshDeliveryFee() {
        scope.launch {
            try {
                if (deliveryZones.isEmpty()) deliveryZones = api.deliveryZones().results
                cart.deliveryFeeOverride = deliveryZones
                    .find { it.name == location.selectedDistrict }
                    ?.deliveryFee?.toDoubleOrNull()?.roundToInt()
            } catch (e: Exception) {
                // keep the placeholder fee
            }
        }
    }

    // --- Checkout / payment (payment itself is owned by SettingsState - it's also the remembered default) ---
    var payment by settings::payment

    // --- Orders / tracking ---------------------------------------------------
    val orders get() = order.orders
    val ordersLoading get() = order.ordersLoading
    val ordersError get() = order.ordersError
    val currentOrder: ApiOrderDetail? get() = order.currentOrder
    val currentOrderLoading get() = order.currentOrderLoading
    val currentOrderError get() = order.currentOrderError
    val checkoutError get() = order.checkoutError
    val placingOrder get() = order.placingOrder

    fun loadOrders() {
        scope.launch { order.loadOrders(api, messages) }
    }

    fun loadOrderDetail(id: Int) {
        scope.launch { order.loadOrderDetail(api, id, messages) }
    }

    /** Syncs the active address (so it has a server id), then calls `POST /orders/create/` for the
     *  real cart the server already has (from [goToCart]'s sync). No client-invented order data -
     *  the created `ApiOrderDetail` (id, number, status, items) comes straight from the response. */
    fun placeOrder() {
        val slotId = slot.toIntOrNull()
        if (slotId == null) {
            order.failCheckout(messages.errorSelectSlot)
            return
        }
        scope.launch {
            val addressId = syncActiveAddressToServer()
            if (addressId == null) {
                order.failCheckout(addressSyncError ?: messages.errorSelectAddressFirst)
                return@launch
            }
            val orderId = order.placeOrder(api, addressId, slotId, payment, comment, messages)
            if (orderId != null) go(Screen.TRACKING)
        }
    }

    // --- Cooking mode ----------------------------------------------------
    var cookingStepIdx by cooking::cookingStepIdx
    var timerRunning by cooking::timerRunning
    var timerSeconds by cooking::timerSeconds
    var rating by cooking::rating

    fun startCooking() {
        go(Screen.COOKING)
        cooking.cookingStepIdx = 0
        cooking.timerRunning = false
        cooking.timerSeconds = 0
        cooking.rating = 0
    }

    fun cookingDone(): Boolean = cooking.cookingDone(recipe)
    fun cookingDisplayIndex(): Int = cooking.cookingDisplayIndex(recipe)
    fun currentCookingStep(): Step? = cooking.currentCookingStep(recipe)
    fun timerLabel(): String = cooking.timerLabel(recipe, messages)
    fun startTimer() = cooking.startTimer(recipe)
    fun cookPrev() = cooking.cookPrev()
    fun cookNext() = cooking.cookNext()
    fun tickTimer() = cooking.tickTimer()

    // --- Settings ----------------------------------------------------------
    var notificationsEnabled by settings::notificationsEnabled
    var language by settings::language
    val dietaryPrefs get() = settings.dietaryPrefs

    fun toggleDietaryPref(pref: String) = settings.toggleDietaryPref(pref)
    fun settingsSnapshot(): SettingsSnapshot = settings.snapshot()
    fun applySettingsSnapshot(s: SettingsSnapshot) = settings.apply(s)

    // --- Location / addresses ------------------------------------------------
    var selectedDistrict by location::selectedDistrict
    var deliveryPoint by location::deliveryPoint
    val savedAddresses get() = location.savedAddresses
    var activeAddressId by location::activeAddressId
    var lastGpsPoint by location::lastGpsPoint
    var pickerBackTarget by location::pickerBackTarget
    var pickerConfirmTarget by location::pickerConfirmTarget
    var pickerAddNew by location::pickerAddNew
    var addressesReturnTarget by location::addressesReturnTarget

    // --- Navigation primitives ----------------------------------------------
    // All screen state lives in AppState rather than nav-entry-scoped view models, so a screen's
    // content is always driven by current AppState fields regardless of whether its back-stack
    // entry is a fresh push or a resumed one - that's what makes popOrPush() safe.

    /** Ordinary forward navigation (push a new back-stack entry). */
    private fun push(target: Screen) = navController.navigate(target.name)

    /** Clears the whole back stack and lands on `target` as the sole entry, so the system back
     *  button exits the app from there. Used for the bottom tabs and for auth/logout transitions
     *  that must not be reachable again via back. */
    private fun resetStackTo(target: Screen) {
        navController.navigate(target.name) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Returns to `target` if it's already on the back stack (e.g. confirming a picker opened
     *  from an existing screen), otherwise pushes it fresh. */
    private fun popOrPush(target: Screen) {
        val onStack = navController.currentBackStack.value.any { it.destination.route == target.name }
        if (onStack) navController.popBackStack(target.name, inclusive = false) else push(target)
    }

    fun go(s: Screen) {
        if (s in TAB_ROOTS) resetStackTo(s) else popOrPush(s)
    }

    fun openLocationPicker(backTarget: Screen, confirmTarget: Screen, addNew: Boolean = false) {
        location.beginPicker(backTarget, confirmTarget, addNew)
        push(Screen.DISTRICT)
    }

    /** Opens the saved-addresses list (backed by the cached addresses) so the user can pick one. */
    fun openAddressList(returnTarget: Screen) {
        location.beginAddressList(returnTarget)
        push(Screen.ADDRESSES)
    }

    fun selectDistrict(name: String, point: GeoPoint? = null, fullAddress: String = "", house: String = "") {
        val target = location.selectDistrict(name, point, fullAddress, house)
        go(target)
        // OFFZONE means selectDistrict left savedAddresses untouched (see LocationState.selectDistrict) -
        // syncing here would just re-PATCH whatever unrelated address was active before this pick.
        if (target != Screen.OFFZONE) scope.launch { syncActiveAddressToServer() }
    }

    /** Persists the active address to `POST/PATCH /api/mobile/addresses/` and returns its server id
     *  (null on failure - surfaced via [addressSyncError]). Runs best-effort when called from
     *  navigation (the picker has already moved on), but [placeOrder] awaits it directly since an
     *  order can't be created without a real address id. */
    private suspend fun syncActiveAddressToServer(): Int? {
        val address = location.activeAddress() ?: return null
        if (auth.authToken.isBlank()) return null
        return try {
            addressSyncError = null
            if (deliveryZones.isEmpty()) deliveryZones = api.deliveryZones().results
            val zoneId = deliveryZones.find { it.name == address.district }?.id
                ?: throw IllegalStateException(
                    "${messages.errorDeliveryZoneNotFoundPrefix}${address.district}${messages.errorDeliveryZoneNotFoundSuffix}"
                )
            val request = AddressRequest(
                street = address.fullAddress.ifBlank { address.district },
                house = address.house.ifBlank { "-" },
                zone = zoneId,
                latitude = address.lat.toString(),
                longitude = address.lon.toString(),
                isDefault = true
            )
            val saved = address.serverId?.let { api.updateAddress(it, request) } ?: api.createAddress(request)
            location.markAddressSynced(address.id, saved.id)
            saved.id
        } catch (e: ApiException) {
            addressSyncError = e.message
            address.serverId
        } catch (e: Exception) {
            addressSyncError = messages.errorSyncAddress
            address.serverId
        }
    }

    fun selectSavedAddress(address: SavedAddress) {
        go(location.selectSavedAddress(address))
    }

    /** Removes the address locally and, if it was ever synced, on the server too (`DELETE
     *  /addresses/<id>/`) - best-effort, the local removal isn't rolled back on failure since the
     *  user already saw it disappear from the list. */
    fun removeAddress(address: SavedAddress) {
        location.removeAddress(address)
        address.serverId?.let { serverId ->
            scope.launch {
                try {
                    api.deleteAddress(serverId)
                } catch (e: Exception) {
                    // Best-effort: the address is already gone from the UI; nothing more to surface here.
                }
            }
        }
    }

    fun deliveryDisplayPoint(): GeoPoint = location.deliveryDisplayPoint()
    fun activeAddress(): SavedAddress? = location.activeAddress()
    fun deliveryAddressLabel(): String = location.deliveryAddressLabel(messages)
    fun applySavedAddresses(list: List<SavedAddress>, activeId: String?) = location.applySavedAddresses(list, activeId)

    /** Refreshes the profile from `GET /auth/me/` - e.g. if it was edited elsewhere (admin panel,
     *  another device). Best-effort and silent on failure; the locally-known name/phone still work. */
    fun refreshProfile() {
        if (auth.authToken.isBlank()) return
        scope.launch {
            try {
                val me = api.me()
                if (me.fullName.isNotBlank()) auth.userName = me.fullName
                auth.userPhone = me.phone
            } catch (e: Exception) {
                // keep whatever's locally known
            }
        }
    }
}
