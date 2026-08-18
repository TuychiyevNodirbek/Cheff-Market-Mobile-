package uz.nodirbek.receiptdelivery.data

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

/** In-memory Settings round-trip tests for the snapshot encode/decode logic - these are the
 *  functions that used to be direct SharedPreferences extensions before the KMP migration. */
class StorageSnapshotTest {
    @Test
    fun auth_snapshot_round_trips() {
        val settings = MapSettings()
        val snapshot = AuthSnapshot(isAuthenticated = true, phone = "+998901234567", name = "Nodir")
        settings.saveAuth(snapshot)
        assertEquals(snapshot, settings.loadAuth())
    }

    @Test
    fun auth_snapshot_defaults_when_nothing_saved() {
        val settings = MapSettings()
        assertEquals(AuthSnapshot(false, "", ""), settings.loadAuth())
    }

    @Test
    fun settings_snapshot_round_trips() {
        val settings = MapSettings()
        val snapshot = SettingsSnapshot(
            notificationsEnabled = false,
            language = "uz",
            payment = "click",
            dietaryPrefs = setOf("Вегетарианское", "Острое")
        )
        settings.saveSettings(snapshot)
        assertEquals(snapshot, settings.loadSettings())
    }

    @Test
    fun cart_snapshot_round_trips() {
        val settings = MapSettings()
        val snapshot = CartSnapshot(
            recipeId = "plov",
            portions = 6,
            cartQty = mapOf("rice" to 2, "beef2" to 1),
            cartRemoved = mapOf("oil" to true),
            cartSubbed = mapOf("onion2" to false),
            onlyMissing = true
        )
        settings.saveCartSnapshot(snapshot)
        assertEquals(snapshot, settings.loadCartSnapshot())
    }

    @Test
    fun cart_snapshot_missing_is_null() {
        val settings = MapSettings()
        assertEquals(null, settings.loadCartSnapshot())
    }

    @Test
    fun addresses_round_trip_including_active_id() {
        val settings = MapSettings()
        val addresses = listOf(
            SavedAddress(id = "addr-1", district = "Юнусабад", lat = 41.35, lon = 69.28, fullAddress = "ул. Пушкина, 1"),
            SavedAddress(id = "addr-2", district = "Мирабад", lat = 41.29, lon = 69.28)
        )
        settings.saveAddresses(addresses, "addr-2")
        val (loaded, activeId) = settings.loadAddresses()
        assertEquals(addresses, loaded)
        assertEquals("addr-2", activeId)
    }

    @Test
    fun orders_round_trip() {
        val settings = MapSettings()
        val orders = listOf(
            Order(
                id = "2481", recipeName = "Лагман домашний", itemsSummary = "5 позиции",
                totalLabel = "68 000", dateLabel = "18 авг, 12:00", statusLabel = "Принят",
                district = "Юнусабад", lat = 41.356, lon = 69.288
            )
        )
        settings.saveOrders(orders)
        assertEquals(orders, settings.loadOrders())
    }
}
