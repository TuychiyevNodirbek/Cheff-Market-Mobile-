package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mapkit.geometry.Point
import uz.nodirbek.receiptdelivery.data.AuthSnapshot
import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.Order
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.SavedAddress
import uz.nodirbek.receiptdelivery.data.SettingsSnapshot
import uz.nodirbek.receiptdelivery.data.Step
import uz.nodirbek.receiptdelivery.data.money
import uz.nodirbek.receiptdelivery.ui.state.AuthState
import uz.nodirbek.receiptdelivery.ui.state.CartState
import uz.nodirbek.receiptdelivery.ui.state.CookingState
import uz.nodirbek.receiptdelivery.ui.state.LocationState
import uz.nodirbek.receiptdelivery.ui.state.OrderState
import uz.nodirbek.receiptdelivery.ui.state.SettingsState

enum class Screen {
    ONB1, ONB2, AUTH_PHONE, AUTH_OTP, AUTH_PROFILE, DISTRICT, OFFZONE, HOME, SEARCH, RECIPE, CART, CHECKOUT, TRACKING, COOKING, PROFILE, ORDER_HISTORY, ADDRESSES, SETTINGS
}

val LANGUAGE_OPTIONS = listOf("ru" to "Русский", "uz" to "O'zbekcha")

val DIETARY_OPTIONS = listOf("Вегетарианское", "Без глютена", "Без лактозы", "Острое")

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
    val priceLabel: String,
    val isFav: Boolean,
    val heroColors: Pair<Long, Long>,
    val imageRes: Int
)

data class OrderStatusStep(val label: String, val active: Boolean, val index: Int)

val TAB_DEFS = listOf(
    Triple("home", "🏠", "Рецепты"),
    Triple("search", "🔍", "Поиск"),
    Triple("tracking", "🧾", "Заказы"),
    Triple("profile", "👤", "Профиль")
)

val SLOT_OPTIONS = listOf(
    "1" to "Сегодня 18:00–20:00",
    "2" to "Сегодня 20:00–22:00",
    "3" to "Завтра утро",
    "4" to "Завтра вечер"
)

val PAYMENT_OPTIONS = listOf(
    "payme" to "Payme",
    "click" to "Click",
    "uzcard" to "Uzcard/Humo",
    "cash" to "Наличные"
)

val STATUS_LABELS = listOf("Принят", "Собирается", "В пути", "Доставлен")

/**
 * Root coordinator: composes the per-feature state holders (auth/cart/order/cooking/settings/location)
 * and owns cross-cutting screen navigation. Exposes the same flat property/method surface the screens
 * already use (via delegation), so splitting the old god-object into focused classes didn't require
 * touching any screen file.
 */
class AppState {
    var screen by mutableStateOf(Screen.ONB1)

    private val auth = AuthState()
    private val cart = CartState()
    private val order = OrderState()
    private val cooking = CookingState()
    private val settings = SettingsState()
    private val location = LocationState()

    // --- Auth -------------------------------------------------------------
    var isAuthenticated by auth::isAuthenticated
    var userPhone by auth::userPhone
    var userName by auth::userName
    var otpError by auth::otpError

    fun submitPhone(phone: String) {
        auth.userPhone = phone
        auth.otpError = false
        screen = Screen.AUTH_OTP
    }

    /** No real SMS backend — any 4-digit code is accepted, matching the "demo code" hint shown on screen. */
    fun verifyOtp(code: String) {
        if (code.length == 4) {
            auth.otpError = false
            screen = Screen.AUTH_PROFILE
        } else {
            auth.otpError = true
        }
    }

    fun completeAuth(name: String) {
        auth.userName = name.ifBlank { "Гость" }
        auth.isAuthenticated = true
        openLocationPicker(Screen.AUTH_PROFILE, Screen.HOME)
    }

    fun logout() {
        auth.isAuthenticated = false
        auth.userName = ""
        auth.userPhone = ""
        screen = Screen.ONB1
    }

    fun authSnapshot(): AuthSnapshot = auth.snapshot()

    fun applyAuthSnapshot(s: AuthSnapshot) {
        auth.isAuthenticated = s.isAuthenticated
        auth.userPhone = s.phone
        auth.userName = s.name
        if (auth.isAuthenticated) screen = Screen.HOME
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

    val recipe: Recipe get() = cart.recipe

    fun selectRecipe(id: String) {
        cart.selectRecipe(id)
        screen = Screen.RECIPE
    }

    fun toggleFav(id: String) = cart.toggleFav(id)
    fun scaledIngredients() = cart.scaledIngredients()
    fun cartTotalLabel(): String = cart.cartTotalLabel()
    fun buildCartRows() = cart.buildCartRows()
    fun incCartQty(key: String) = cart.incCartQty(key)
    fun decCartQty(key: String) = cart.decCartQty(key)
    fun removeCartItem(key: String) = cart.removeCartItem(key)
    fun undoSub(key: String) = cart.undoSub(key)
    fun cartSubtotal(): Int = cart.cartSubtotal()
    fun deliveryFee(): Int = cart.deliveryFee()
    fun cartGrandTotal(): Int = cart.cartGrandTotal()
    fun makeCard(r: Recipe): RecipeCard = cart.makeCard(r)
    fun allRecipeCards() = cart.allRecipeCards()
    fun collectionCards() = cart.collectionCards()
    fun searchResults() = cart.searchResults()
    fun cartSnapshot(): CartSnapshot = cart.snapshot()
    fun applyCartSnapshot(s: CartSnapshot) = cart.applySnapshot(s)

    // --- Checkout / payment (payment itself is owned by SettingsState - it's also the remembered default) ---
    var payment by settings::payment

    // --- Orders / tracking ---------------------------------------------------
    val orders get() = order.orders
    var orderStatusIdx by order::orderStatusIdx
    var orderCardOpen by order::orderCardOpen

    fun orderStatuses() = order.orderStatuses()
    fun mapLabel(): String = order.mapLabel()
    fun advanceOrderStatus() = order.advanceOrderStatus()
    fun currentOrderId(): String = order.currentOrderId()
    fun currentOrderPoint(): Point = order.currentOrderPoint(location.deliveryDisplayPoint())

    fun placeOrder() {
        val itemCount = cart.buildCartRows().size
        val point = location.deliveryDisplayPoint()
        order.placeOrder(
            recipe = cart.recipe,
            itemCount = itemCount,
            totalLabel = money(cart.cartGrandTotal()),
            district = location.selectedDistrict ?: "Юнусабад",
            point = point
        )
        screen = Screen.TRACKING
    }

    // --- Cooking mode ----------------------------------------------------
    var cookingStepIdx by cooking::cookingStepIdx
    var timerRunning by cooking::timerRunning
    var timerSeconds by cooking::timerSeconds
    var rating by cooking::rating

    fun startCooking() {
        screen = Screen.COOKING
        cooking.cookingStepIdx = 0
        cooking.timerRunning = false
        cooking.timerSeconds = 0
        cooking.rating = 0
    }

    fun cookingDone(): Boolean = cooking.cookingDone(recipe)
    fun cookingDisplayIndex(): Int = cooking.cookingDisplayIndex(recipe)
    fun currentCookingStep(): Step? = cooking.currentCookingStep(recipe)
    fun timerLabel(): String = cooking.timerLabel(recipe)
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

    fun go(s: Screen) { screen = s }

    fun openLocationPicker(backTarget: Screen, confirmTarget: Screen, addNew: Boolean = false) {
        location.beginPicker(backTarget, confirmTarget, addNew)
        screen = Screen.DISTRICT
    }

    /** Opens the saved-addresses list (backed by the cached addresses) so the user can pick one. */
    fun openAddressList(returnTarget: Screen) {
        location.beginAddressList(returnTarget)
        screen = Screen.ADDRESSES
    }

    fun selectDistrict(name: String, point: Point? = null, fullAddress: String = "") {
        screen = location.selectDistrict(name, point, fullAddress)
    }

    fun selectSavedAddress(address: SavedAddress) {
        screen = location.selectSavedAddress(address)
    }

    fun removeAddress(address: SavedAddress) = location.removeAddress(address)
    fun deliveryDisplayPoint(): Point = location.deliveryDisplayPoint()
    fun activeAddress(): SavedAddress? = location.activeAddress()
    fun deliveryAddressLabel(): String = location.deliveryAddressLabel()
    fun applySavedAddresses(list: List<SavedAddress>, activeId: String?) = location.applySavedAddresses(list, activeId)
}
