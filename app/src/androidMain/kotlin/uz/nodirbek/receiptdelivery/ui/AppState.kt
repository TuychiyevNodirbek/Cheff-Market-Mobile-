package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mapkit.geometry.Point
import uz.nodirbek.receiptdelivery.data.AuthSnapshot
import uz.nodirbek.receiptdelivery.data.CartSnapshot
import uz.nodirbek.receiptdelivery.data.DISTRICT_COORDS
import uz.nodirbek.receiptdelivery.data.OFF_ZONE_DISTRICT
import uz.nodirbek.receiptdelivery.data.Order
import uz.nodirbek.receiptdelivery.data.RECIPES
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.SavedAddress
import uz.nodirbek.receiptdelivery.data.SettingsSnapshot
import uz.nodirbek.receiptdelivery.data.StockStatus
import uz.nodirbek.receiptdelivery.data.imageResFor
import uz.nodirbek.receiptdelivery.data.money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

class AppState {
    var screen by mutableStateOf(Screen.ONB1)
    var portions by mutableStateOf(4)
    var recipeId by mutableStateOf("lagman")
    val favs = mutableStateMapOf<String, Boolean>()
    val activeChips = mutableStateMapOf<String, Boolean>()
    var searchQuery by mutableStateOf("")
    var stepsOpen by mutableStateOf(false)
    val cartQty = mutableStateMapOf<String, Int>()
    val cartRemoved = mutableStateMapOf<String, Boolean>()
    val cartSubbed = mutableStateMapOf("pepper" to true, "pepper2" to true)
    var onlyMissing by mutableStateOf(false)
    var slot by mutableStateOf("2")
    var payment by mutableStateOf("payme")
    var comment by mutableStateOf("")
    var orderStatusIdx by mutableStateOf(0)
    var orderCardOpen by mutableStateOf(true)
    var cookingStepIdx by mutableStateOf(0)
    var timerRunning by mutableStateOf(false)
    var timerSeconds by mutableStateOf(0)
    var rating by mutableStateOf(0)
    var selectedDistrict by mutableStateOf<String?>(null)
    var deliveryPoint by mutableStateOf<Point?>(null)
    val orders = mutableStateListOf<Order>()
    val savedAddresses = mutableStateListOf<SavedAddress>()
    var activeAddressId by mutableStateOf<String?>(null)
    var lastGpsPoint by mutableStateOf<Point?>(null)
    var isAuthenticated by mutableStateOf(false)
    var userPhone by mutableStateOf("")
    var userName by mutableStateOf("")
    var otpError by mutableStateOf(false)
    var notificationsEnabled by mutableStateOf(true)
    var language by mutableStateOf("ru")
    val dietaryPrefs = mutableStateListOf<String>()

    /** Where the location picker's back arrow / system back should return to. */
    var pickerBackTarget by mutableStateOf(Screen.ONB2)

    /** Where confirming a location in the picker should navigate to. */
    var pickerConfirmTarget by mutableStateOf(Screen.HOME)

    /** True when the picker was opened to add a brand-new address rather than update the active one. */
    var pickerAddNew by mutableStateOf(false)

    /** Where the saved-addresses list's back arrow / address selection should return to. */
    var addressesReturnTarget by mutableStateOf(Screen.HOME)

    val recipe: Recipe get() = RECIPES.getValue(recipeId)

    fun scaleFactor(): Double = portions.toDouble() / recipe.baseServings

    fun go(s: Screen) { screen = s }

    fun submitPhone(phone: String) {
        userPhone = phone
        otpError = false
        screen = Screen.AUTH_OTP
    }

    /** No real SMS backend — any 4-digit code is accepted, matching the "demo code" hint shown on screen. */
    fun verifyOtp(code: String) {
        if (code.length == 4) {
            otpError = false
            screen = Screen.AUTH_PROFILE
        } else {
            otpError = true
        }
    }

    fun completeAuth(name: String) {
        userName = name.ifBlank { "Гость" }
        isAuthenticated = true
        openLocationPicker(Screen.AUTH_PROFILE, Screen.HOME)
    }

    fun toggleDietaryPref(pref: String) {
        if (dietaryPrefs.contains(pref)) dietaryPrefs.remove(pref) else dietaryPrefs.add(pref)
    }

    fun logout() {
        isAuthenticated = false
        userName = ""
        userPhone = ""
        screen = Screen.ONB1
    }

    fun selectRecipe(id: String) {
        recipeId = id
        portions = RECIPES.getValue(id).baseServings
        screen = Screen.RECIPE
        stepsOpen = false
    }

    fun openLocationPicker(backTarget: Screen, confirmTarget: Screen, addNew: Boolean = false) {
        pickerBackTarget = backTarget
        pickerConfirmTarget = confirmTarget
        pickerAddNew = addNew
        screen = Screen.DISTRICT
    }

    /** Opens the saved-addresses list (backed by the cached addresses) so the user can pick one. */
    fun openAddressList(returnTarget: Screen) {
        addressesReturnTarget = returnTarget
        screen = Screen.ADDRESSES
    }

    fun selectDistrict(name: String, point: Point? = null, fullAddress: String = "") {
        if (name == OFF_ZONE_DISTRICT) {
            screen = Screen.OFFZONE
            return
        }
        val resolved = point ?: DISTRICT_COORDS[name]?.let { Point(it.first, it.second) }
        selectedDistrict = name
        resolved?.let { p ->
            deliveryPoint = p
            upsertAddress(name, p, fullAddress, pickerAddNew)
        }
        screen = pickerConfirmTarget
    }

    private fun upsertAddress(district: String, point: Point, fullAddress: String, addNew: Boolean) {
        val existing = if (addNew) null else savedAddresses.find { it.district == district }
        if (existing != null) {
            savedAddresses[savedAddresses.indexOf(existing)] =
                existing.copy(lat = point.latitude, lon = point.longitude, fullAddress = fullAddress)
            activeAddressId = existing.id
        } else {
            val address = SavedAddress(
                id = "addr-${System.currentTimeMillis()}",
                district = district,
                lat = point.latitude,
                lon = point.longitude,
                fullAddress = fullAddress
            )
            savedAddresses.add(address)
            activeAddressId = address.id
        }
    }

    fun selectSavedAddress(address: SavedAddress) {
        activeAddressId = address.id
        selectedDistrict = address.district
        deliveryPoint = Point(address.lat, address.lon)
        screen = addressesReturnTarget
    }

    fun removeAddress(address: SavedAddress) {
        savedAddresses.remove(address)
        if (activeAddressId == address.id) {
            activeAddressId = savedAddresses.firstOrNull()?.id
        }
    }

    fun deliveryDisplayPoint(): Point =
        deliveryPoint ?: DISTRICT_COORDS[selectedDistrict]?.let { Point(it.first, it.second) } ?: Point(41.311081, 69.240562)

    fun activeAddress(): SavedAddress? = savedAddresses.find { it.id == activeAddressId }

    /** Full address text for display (street/house typed by the user), falling back to the district name. */
    fun deliveryAddressLabel(): String =
        activeAddress()?.let { it.fullAddress.ifBlank { it.district } } ?: (selectedDistrict ?: "Выберите адрес")

    fun toggleFav(id: String) {
        favs[id] = !(favs[id] ?: false)
    }

    fun scaledIngredients(): List<ScaledIngredient> {
        val factor = scaleFactor()
        return recipe.ingredients.map { ing ->
            val qty = Math.round(ing.baseQty * factor).toInt()
            val (label, color) = when (ing.status) {
                StockStatus.OK -> "В наличии" to uz.nodirbek.receiptdelivery.ui.theme.Green
                StockStatus.LOW -> "Осталось мало" to uz.nodirbek.receiptdelivery.ui.theme.Amber
                StockStatus.SUBSTITUTED -> "Заменено: ${ing.note}" to uz.nodirbek.receiptdelivery.ui.theme.Amber
            }
            ScaledIngredient(ing.key, ing.name, ing.name.take(1), "$qty ${ing.unit}", label, color)
        }
    }

    fun cartTotalLabel(): String {
        val factor = scaleFactor()
        val total = recipe.ingredients.sumOf { it.pricePerBase * factor }
        return money(total)
    }

    fun buildCartRows(): List<CartRow> {
        val factor = scaleFactor()
        return recipe.ingredients
            .filter { cartRemoved[it.key] != true }
            .map { ing ->
                val count = cartQty[ing.key] ?: 1
                val qty = Math.round(ing.baseQty * factor * count).toInt()
                val price = Math.round(ing.pricePerBase * factor * count).toInt()
                val substituted = (cartSubbed[ing.key] == true) && ing.status == StockStatus.SUBSTITUTED
                CartRow(
                    key = ing.key,
                    name = ing.name,
                    initial = ing.name.take(1),
                    qtyLabel = "$qty ${ing.unit}",
                    packLabel = "упаковка, \"Korzinka\"",
                    priceLabel = money(price),
                    price = price,
                    count = count,
                    substituted = substituted,
                    subNote = ing.note
                )
            }
    }

    fun incCartQty(key: String) { cartQty[key] = (cartQty[key] ?: 1) + 1 }
    fun decCartQty(key: String) { cartQty[key] = maxOf(1, (cartQty[key] ?: 1) - 1) }
    fun removeCartItem(key: String) { cartRemoved[key] = true }
    fun undoSub(key: String) { cartSubbed[key] = false }

    fun cartSubtotal(): Int = buildCartRows().sumOf { it.price }
    fun deliveryFee(): Int = 12000
    fun cartGrandTotal(): Int = cartSubtotal() + deliveryFee()

    fun makeCard(r: Recipe): RecipeCard = RecipeCard(
        id = r.id, name = r.name, time = r.timeMinutes, baseServings = r.baseServings,
        priceLabel = money(r.basePrice), isFav = favs[r.id] ?: false, heroColors = r.heroColors,
        imageRes = imageResFor(r.imageKey)
    )

    fun allRecipeCards(): List<RecipeCard> = RECIPES.values.map { makeCard(it) }
    fun collectionCards(): List<RecipeCard> = RECIPES.values.take(3).map { makeCard(it) }
    fun searchResults(): List<RecipeCard> {
        val q = searchQuery.trim().lowercase()
        return RECIPES.values.filter { q.isEmpty() || it.name.lowercase().contains(q) }.map { makeCard(it) }
    }

    fun orderStatuses(): List<OrderStatusStep> = STATUS_LABELS.mapIndexed { i, label ->
        OrderStatusStep(label, i <= orderStatusIdx, i)
    }

    fun mapLabel(): String =
        if (orderStatusIdx >= 2) "карта: курьер в пути" else "карта появится, когда курьер выедет"

    fun placeOrder() {
        val itemCount = buildCartRows().size
        val point = deliveryDisplayPoint()
        val order = Order(
            id = (2481 + orders.size).toString(),
            recipeName = recipe.name,
            itemsSummary = "$itemCount позиции",
            totalLabel = money(cartGrandTotal()),
            dateLabel = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date()),
            statusLabel = STATUS_LABELS[0],
            district = selectedDistrict ?: "Юнусабад",
            lat = point.latitude,
            lon = point.longitude
        )
        orders.add(0, order)
        orderStatusIdx = 0
        orderCardOpen = true
        screen = Screen.TRACKING
    }

    fun advanceOrderStatus() {
        orderStatusIdx = minOf(STATUS_LABELS.size - 1, orderStatusIdx + 1)
        if (orders.isNotEmpty()) {
            orders[0] = orders[0].copy(statusLabel = STATUS_LABELS[orderStatusIdx])
        }
    }

    fun currentOrderId(): String = orders.firstOrNull()?.id ?: "—"

    /** The point to show on the tracking map: the order's own saved location, not whatever the live selected address is now. */
    fun currentOrderPoint(): Point = orders.firstOrNull()?.let { Point(it.lat, it.lon) } ?: deliveryDisplayPoint()

    fun startCooking() {
        screen = Screen.COOKING
        cookingStepIdx = 0
        timerRunning = false
        timerSeconds = 0
        rating = 0
    }

    fun cookingDone(): Boolean = cookingStepIdx >= recipe.steps.size

    fun cookingDisplayIndex(): Int = minOf(cookingStepIdx + 1, recipe.steps.size)

    fun currentCookingStep() = recipe.steps.getOrNull(minOf(cookingStepIdx, recipe.steps.size - 1))

    fun timerLabel(): String {
        val step = currentCookingStep()
        return if (timerRunning) {
            val m = timerSeconds / 60
            val s = timerSeconds % 60
            "%d:%02d".format(m, s)
        } else if (step?.timerMinutes != null) {
            "Таймер ${step.timerMinutes} мин"
        } else ""
    }

    fun startTimer() {
        val step = currentCookingStep()
        timerRunning = true
        timerSeconds = (step?.timerMinutes ?: 0) * 60
    }

    fun cookPrev() {
        cookingStepIdx = maxOf(0, cookingStepIdx - 1)
        timerRunning = false
    }

    fun cookNext() {
        cookingStepIdx += 1
        timerRunning = false
    }

    fun tickTimer() {
        if (timerRunning && timerSeconds > 0) {
            timerSeconds -= 1
            if (timerSeconds <= 0) timerRunning = false
        }
    }

    fun cartSnapshot(): CartSnapshot = CartSnapshot(
        recipeId = recipeId,
        portions = portions,
        cartQty = cartQty.toMap(),
        cartRemoved = cartRemoved.toMap(),
        cartSubbed = cartSubbed.toMap(),
        onlyMissing = onlyMissing
    )

    fun applyCartSnapshot(s: CartSnapshot) {
        if (!RECIPES.containsKey(s.recipeId)) return
        recipeId = s.recipeId
        portions = if (s.portions in 1..6) s.portions else RECIPES.getValue(s.recipeId).baseServings
        cartQty.clear(); cartQty.putAll(s.cartQty)
        cartRemoved.clear(); cartRemoved.putAll(s.cartRemoved)
        cartSubbed.clear(); cartSubbed.putAll(s.cartSubbed)
        onlyMissing = s.onlyMissing
    }

    fun applySavedAddresses(list: List<SavedAddress>, activeId: String?) {
        savedAddresses.clear()
        savedAddresses.addAll(list)
        activeAddressId = activeId ?: list.firstOrNull()?.id
        savedAddresses.find { it.id == activeAddressId }?.let { addr ->
            selectedDistrict = addr.district
            deliveryPoint = Point(addr.lat, addr.lon)
        }
    }

    fun authSnapshot(): AuthSnapshot = AuthSnapshot(isAuthenticated, userPhone, userName)

    fun applyAuthSnapshot(s: AuthSnapshot) {
        isAuthenticated = s.isAuthenticated
        userPhone = s.phone
        userName = s.name
        if (isAuthenticated) screen = Screen.HOME
    }

    fun settingsSnapshot(): SettingsSnapshot = SettingsSnapshot(
        notificationsEnabled = notificationsEnabled,
        language = language,
        payment = payment,
        dietaryPrefs = dietaryPrefs.toSet()
    )

    fun applySettingsSnapshot(s: SettingsSnapshot) {
        notificationsEnabled = s.notificationsEnabled
        language = s.language
        payment = s.payment
        dietaryPrefs.clear()
        dietaryPrefs.addAll(s.dietaryPrefs)
    }
}
