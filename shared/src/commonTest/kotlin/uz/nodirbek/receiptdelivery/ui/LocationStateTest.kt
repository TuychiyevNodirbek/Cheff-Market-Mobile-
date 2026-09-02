package uz.nodirbek.receiptdelivery.ui

import uz.nodirbek.receiptdelivery.data.OFF_ZONE_DISTRICT
import uz.nodirbek.receiptdelivery.data.SavedAddress
import uz.nodirbek.receiptdelivery.geo.GeoPoint
import uz.nodirbek.receiptdelivery.geo.TASHKENT_CENTER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationStateTest {
    @Test
    fun beginPicker_sets_back_confirm_and_addNew() {
        val location = LocationState()
        location.beginPicker(Screen.PROFILE, Screen.HOME, addNew = true)
        assertEquals(Screen.PROFILE, location.pickerBackTarget)
        assertEquals(Screen.HOME, location.pickerConfirmTarget)
        assertTrue(location.pickerAddNew)
    }

    @Test
    fun beginAddressList_sets_return_target() {
        val location = LocationState()
        location.beginAddressList(Screen.CHECKOUT)
        assertEquals(Screen.CHECKOUT, location.addressesReturnTarget)
    }

    @Test
    fun selectDistrict_offZone_returns_OFFZONE_and_does_not_touch_state() {
        val location = LocationState()
        val result = location.selectDistrict(OFF_ZONE_DISTRICT, point = null, fullAddress = "")
        assertEquals(Screen.OFFZONE, result)
        assertNull(location.selectedDistrict)
        assertNull(location.deliveryPoint)
        assertTrue(location.savedAddresses.isEmpty())
    }

    @Test
    fun selectDistrict_with_explicit_point_saves_address_and_returns_confirm_target() {
        val location = LocationState()
        location.beginPicker(Screen.ONB2, Screen.CHECKOUT, addNew = false)

        val point = GeoPoint(41.30, 69.25)
        val result = location.selectDistrict("Юнусабад", point, "ул. Пушкина, 1")

        assertEquals(Screen.CHECKOUT, result)
        assertEquals("Юнусабад", location.selectedDistrict)
        assertEquals(point, location.deliveryPoint)
        assertEquals(1, location.savedAddresses.size)
        assertEquals("ул. Пушкина, 1", location.savedAddresses.single().fullAddress)
        assertEquals(location.savedAddresses.single().id, location.activeAddressId)
    }

    @Test
    fun selectDistrict_without_point_falls_back_to_DISTRICT_COORDS() {
        val location = LocationState()
        location.selectDistrict("Чиланзар", point = null, fullAddress = "")
        assertNotNull(location.deliveryPoint)
        // Не пусто и не дефолтный центр Ташкента - реально подтянуло координаты района.
        assertTrue(location.deliveryPoint != TASHKENT_CENTER)
    }

    @Test
    fun selectDistrict_updates_existing_address_when_not_addNew() {
        val location = LocationState()
        location.beginPicker(Screen.ONB2, Screen.CHECKOUT, addNew = false)
        location.selectDistrict("Юнусабад", GeoPoint(41.30, 69.25), "старый адрес")
        val firstId = location.activeAddressId

        location.selectDistrict("Юнусабад", GeoPoint(41.31, 69.26), "новый адрес")

        assertEquals(1, location.savedAddresses.size, "тот же район без addNew должен обновить, а не задвоить адрес")
        assertEquals(firstId, location.activeAddressId)
        assertEquals("новый адрес", location.savedAddresses.single().fullAddress)
    }

    @Test
    fun selectDistrict_creates_new_address_when_addNew_even_for_same_district() {
        val location = LocationState()
        location.beginPicker(Screen.ADDRESSES, Screen.ADDRESSES, addNew = false)
        location.selectDistrict("Юнусабад", GeoPoint(41.30, 69.25), "первый адрес")

        location.beginPicker(Screen.ADDRESSES, Screen.ADDRESSES, addNew = true)
        location.selectDistrict("Юнусабад", GeoPoint(41.31, 69.26), "второй адрес")

        assertEquals(2, location.savedAddresses.size, "addNew=true должен всегда создавать новый адрес")
    }

    @Test
    fun selectSavedAddress_activates_it_and_returns_addressesReturnTarget() {
        val location = LocationState()
        location.beginAddressList(Screen.PROFILE)
        val address = SavedAddress(id = "addr-1", district = "Мирабад", lat = 41.29, lon = 69.28)

        val result = location.selectSavedAddress(address)

        assertEquals(Screen.PROFILE, result)
        assertEquals("addr-1", location.activeAddressId)
        assertEquals("Мирабад", location.selectedDistrict)
        assertEquals(GeoPoint(41.29, 69.28), location.deliveryPoint)
    }

    @Test
    fun removeAddress_reassigns_activeAddressId_when_active_one_removed() {
        val location = LocationState()
        val a1 = SavedAddress(id = "a1", district = "Юнусабад", lat = 1.0, lon = 1.0)
        val a2 = SavedAddress(id = "a2", district = "Мирабад", lat = 2.0, lon = 2.0)
        location.applySavedAddresses(listOf(a1, a2), activeId = "a1")

        location.removeAddress(a1)

        assertEquals(listOf(a2), location.savedAddresses.toList())
        assertEquals("a2", location.activeAddressId, "после удаления активного адреса активным должен стать первый оставшийся")
    }

    @Test
    fun removeAddress_keeps_activeAddressId_when_a_different_address_removed() {
        val location = LocationState()
        val a1 = SavedAddress(id = "a1", district = "Юнусабад", lat = 1.0, lon = 1.0)
        val a2 = SavedAddress(id = "a2", district = "Мирабад", lat = 2.0, lon = 2.0)
        location.applySavedAddresses(listOf(a1, a2), activeId = "a2")

        location.removeAddress(a1)

        assertEquals("a2", location.activeAddressId)
    }

    @Test
    fun deliveryDisplayPoint_prefers_explicit_deliveryPoint() {
        val location = LocationState()
        location.selectDistrict("Юнусабад", GeoPoint(10.0, 20.0), "")
        assertEquals(GeoPoint(10.0, 20.0), location.deliveryDisplayPoint())
    }

    @Test
    fun deliveryDisplayPoint_falls_back_to_TASHKENT_CENTER_when_nothing_known() {
        val location = LocationState()
        assertEquals(TASHKENT_CENTER, location.deliveryDisplayPoint())
    }

    @Test
    fun deliveryAddressLabel_prefers_fullAddress_then_district_then_placeholder() {
        val location = LocationState()
        assertEquals("Выберите адрес", location.deliveryAddressLabel())

        location.applySavedAddresses(
            listOf(SavedAddress(id = "a1", district = "Юнусабад", lat = 1.0, lon = 1.0, fullAddress = "")),
            activeId = "a1",
        )
        assertEquals("Юнусабад", location.deliveryAddressLabel(), "пустой fullAddress -> должно откатиться на название района")

        location.applySavedAddresses(
            listOf(SavedAddress(id = "a1", district = "Юнусабад", lat = 1.0, lon = 1.0, fullAddress = "ул. Амира Темура, 5")),
            activeId = "a1",
        )
        assertEquals("ул. Амира Темура, 5", location.deliveryAddressLabel())
    }

    @Test
    fun applySavedAddresses_defaults_active_to_first_when_activeId_missing() {
        val location = LocationState()
        val a1 = SavedAddress(id = "a1", district = "Юнусабад", lat = 1.0, lon = 1.0)
        val a2 = SavedAddress(id = "a2", district = "Мирабад", lat = 2.0, lon = 2.0)

        location.applySavedAddresses(listOf(a1, a2), activeId = null)

        assertEquals("a1", location.activeAddressId)
        assertEquals("Юнусабад", location.selectedDistrict)
        assertEquals(GeoPoint(1.0, 1.0), location.deliveryPoint)
    }

    @Test
    fun applySavedAddresses_replaces_previous_list_entirely() {
        val location = LocationState()
        location.applySavedAddresses(listOf(SavedAddress(id = "old", district = "Юнусабад", lat = 1.0, lon = 1.0)), "old")

        location.applySavedAddresses(listOf(SavedAddress(id = "new", district = "Мирабад", lat = 2.0, lon = 2.0)), "new")

        assertEquals(1, location.savedAddresses.size)
        assertEquals("new", location.savedAddresses.single().id)
    }

    @Test
    fun activeAddress_returns_null_when_nothing_selected() {
        val location = LocationState()
        assertNull(location.activeAddress())
        assertFalse(location.savedAddresses.isNotEmpty())
    }
}
