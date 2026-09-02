package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random
import uz.nodirbek.receiptdelivery.data.DISTRICT_COORDS
import uz.nodirbek.receiptdelivery.data.OFF_ZONE_DISTRICT
import uz.nodirbek.receiptdelivery.data.SavedAddress
import uz.nodirbek.receiptdelivery.geo.GeoPoint
import uz.nodirbek.receiptdelivery.geo.TASHKENT_CENTER

/** District/address selection and the location-picker flow. Expressed purely in terms of
 *  GeoPoint (lat/lon), not any specific map SDK's point type - the conversion to/from a map
 *  SDK's own type happens only at the UI boundary (see ui/components/YandexMap.kt on Android).
 *  Methods that resolve to a screen transition (selectDistrict/selectSavedAddress) return the
 *  target Screen instead of setting it directly - the root AppState performs the actual
 *  navigation. */
class LocationState {
    var selectedDistrict by mutableStateOf<String?>(null)
    var deliveryPoint by mutableStateOf<GeoPoint?>(null)
    val savedAddresses = mutableStateListOf<SavedAddress>()
    var activeAddressId by mutableStateOf<String?>(null)
    var lastGpsPoint by mutableStateOf<GeoPoint?>(null)

    /** Where the location picker's back arrow / system back should return to. */
    var pickerBackTarget by mutableStateOf(Screen.ONB2)

    /** Where confirming a location in the picker should navigate to. */
    var pickerConfirmTarget by mutableStateOf(Screen.HOME)

    /** True when the picker was opened to add a brand-new address rather than update the active one. */
    var pickerAddNew by mutableStateOf(false)

    /** Where the saved-addresses list's back arrow / address selection should return to. */
    var addressesReturnTarget by mutableStateOf(Screen.HOME)

    fun beginPicker(backTarget: Screen, confirmTarget: Screen, addNew: Boolean) {
        pickerBackTarget = backTarget
        pickerConfirmTarget = confirmTarget
        pickerAddNew = addNew
    }

    fun beginAddressList(returnTarget: Screen) {
        addressesReturnTarget = returnTarget
    }

    /** Resolves the district, saves/updates the address if applicable, and returns where to navigate. */
    fun selectDistrict(name: String, point: GeoPoint?, fullAddress: String): Screen {
        if (name == OFF_ZONE_DISTRICT) return Screen.OFFZONE
        val resolved = point ?: DISTRICT_COORDS[name]?.let { GeoPoint(it.first, it.second) }
        selectedDistrict = name
        resolved?.let { p ->
            deliveryPoint = p
            upsertAddress(name, p, fullAddress, pickerAddNew)
        }
        return pickerConfirmTarget
    }

    private fun upsertAddress(district: String, point: GeoPoint, fullAddress: String, addNew: Boolean) {
        val existing = if (addNew) null else savedAddresses.find { it.district == district }
        if (existing != null) {
            savedAddresses[savedAddresses.indexOf(existing)] =
                existing.copy(lat = point.latitude, lon = point.longitude, fullAddress = fullAddress)
            activeAddressId = existing.id
        } else {
            val address = SavedAddress(
                id = "addr-${Random.nextLong()}",
                district = district,
                lat = point.latitude,
                lon = point.longitude,
                fullAddress = fullAddress
            )
            savedAddresses.add(address)
            activeAddressId = address.id
        }
    }

    fun selectSavedAddress(address: SavedAddress): Screen {
        activeAddressId = address.id
        selectedDistrict = address.district
        deliveryPoint = GeoPoint(address.lat, address.lon)
        return addressesReturnTarget
    }

    fun removeAddress(address: SavedAddress) {
        savedAddresses.remove(address)
        if (activeAddressId == address.id) {
            activeAddressId = savedAddresses.firstOrNull()?.id
        }
    }

    fun deliveryDisplayPoint(): GeoPoint =
        deliveryPoint ?: DISTRICT_COORDS[selectedDistrict]?.let { GeoPoint(it.first, it.second) } ?: TASHKENT_CENTER

    fun activeAddress(): SavedAddress? = savedAddresses.find { it.id == activeAddressId }

    /** Full address text for display (street/house typed by the user), falling back to the district name. */
    fun deliveryAddressLabel(): String =
        activeAddress()?.let { it.fullAddress.ifBlank { it.district } } ?: (selectedDistrict ?: "Выберите адрес")

    fun applySavedAddresses(list: List<SavedAddress>, activeId: String?) {
        savedAddresses.clear()
        savedAddresses.addAll(list)
        activeAddressId = activeId ?: list.firstOrNull()?.id
        savedAddresses.find { it.id == activeAddressId }?.let { addr ->
            selectedDistrict = addr.district
            deliveryPoint = GeoPoint(addr.lat, addr.lon)
        }
    }
}
