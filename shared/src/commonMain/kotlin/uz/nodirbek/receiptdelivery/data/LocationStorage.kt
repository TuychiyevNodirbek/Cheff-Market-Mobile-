package uz.nodirbek.receiptdelivery.data

import com.russhwolf.settings.Settings

data class SavedAddress(
    val id: String,
    val district: String,
    val lat: Double,
    val lon: Double,
    /** Full address text typed by the user (street, house, apartment). Falls back to district when blank. */
    val fullAddress: String = ""
)

private const val KEY_ADDRESSES = "saved_addresses"
private const val KEY_ACTIVE_ADDRESS = "active_address_id"
private const val KEY_GPS_LAT = "last_gps_lat"
private const val KEY_GPS_LON = "last_gps_lon"
private const val FIELD_SEP = "|"
private const val ADDR_SEP = "\n"

fun Settings.saveAddresses(addresses: List<SavedAddress>, activeId: String?) {
    putString(
        KEY_ADDRESSES,
        addresses.joinToString(ADDR_SEP) { a ->
            val safeFullAddress = a.fullAddress.replace(FIELD_SEP, " ").replace(ADDR_SEP, " ")
            listOf(a.id, a.district, a.lat, a.lon, safeFullAddress).joinToString(FIELD_SEP)
        }
    )
    if (activeId != null) putString(KEY_ACTIVE_ADDRESS, activeId) else remove(KEY_ACTIVE_ADDRESS)
}

fun Settings.loadAddresses(): Pair<List<SavedAddress>, String?> {
    val raw = getString(KEY_ADDRESSES, "")
    val addresses = raw.split(ADDR_SEP).filter { it.isNotBlank() }.mapNotNull { line ->
        val parts = line.split(FIELD_SEP)
        if (parts.size >= 4) {
            val lat = parts[2].toDoubleOrNull()
            val lon = parts[3].toDoubleOrNull()
            val fullAddress = parts.getOrNull(4) ?: ""
            if (lat != null && lon != null) SavedAddress(parts[0], parts[1], lat, lon, fullAddress) else null
        } else null
    }
    return addresses to getStringOrNull(KEY_ACTIVE_ADDRESS)
}

fun Settings.saveLastGpsPoint(lat: Double, lon: Double) {
    putString(KEY_GPS_LAT, lat.toString())
    putString(KEY_GPS_LON, lon.toString())
}

fun Settings.loadLastGpsPoint(): Pair<Double, Double>? {
    val lat = getStringOrNull(KEY_GPS_LAT)?.toDoubleOrNull() ?: return null
    val lon = getStringOrNull(KEY_GPS_LON)?.toDoubleOrNull() ?: return null
    return lat to lon
}
