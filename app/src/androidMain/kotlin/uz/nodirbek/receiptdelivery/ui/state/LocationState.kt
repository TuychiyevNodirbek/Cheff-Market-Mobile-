package uz.nodirbek.receiptdelivery.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mapkit.geometry.Point
import uz.nodirbek.receiptdelivery.data.DISTRICT_COORDS
import uz.nodirbek.receiptdelivery.data.OFF_ZONE_DISTRICT
import uz.nodirbek.receiptdelivery.data.SavedAddress
import uz.nodirbek.receiptdelivery.ui.Screen

/** District/address selection and the location-picker flow. Stays androidMain because it's expressed
 *  in terms of Yandex MapKit's Point (map integration is a later phase). Methods that resolve to a
 *  screen transition (selectDistrict/selectSavedAddress) return the target Screen instead of setting
 *  it directly - the root AppState performs the actual navigation. */
class LocationState {
    var selectedDistrict by mutableStateOf<String?>(null)
    var deliveryPoint by mutableStateOf<Point?>(null)
    val savedAddresses = mutableStateListOf<SavedAddress>()
    var activeAddressId by mutableStateOf<String?>(null)
    var lastGpsPoint by mutableStateOf<Point?>(null)

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
    fun selectDistrict(name: String, point: Point?, fullAddress: String): Screen {
        if (name == OFF_ZONE_DISTRICT) return Screen.OFFZONE
        val resolved = point ?: DISTRICT_COORDS[name]?.let { Point(it.first, it.second) }
        selectedDistrict = name
        resolved?.let { p ->
            deliveryPoint = p
            upsertAddress(name, p, fullAddress, pickerAddNew)
        }
        return pickerConfirmTarget
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

    fun selectSavedAddress(address: SavedAddress): Screen {
        activeAddressId = address.id
        selectedDistrict = address.district
        deliveryPoint = Point(address.lat, address.lon)
        return addressesReturnTarget
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

    fun applySavedAddresses(list: List<SavedAddress>, activeId: String?) {
        savedAddresses.clear()
        savedAddresses.addAll(list)
        activeAddressId = activeId ?: list.firstOrNull()?.id
        savedAddresses.find { it.id == activeAddressId }?.let { addr ->
            selectedDistrict = addr.district
            deliveryPoint = Point(addr.lat, addr.lon)
        }
    }
}
