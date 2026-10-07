package uz.nodirbek.receiptdelivery.data

object ApiConfig {
    const val BASE_URL = "https://web-production-887d1.up.railway.app"
    const val MOBILE_API_URL = "$BASE_URL/api/mobile/"
}

/** Resolves an API-supplied image path to a loadable URL, or null when there's nothing to load
 *  (blank/missing image) so the caller can fall back to a placeholder instead of requesting it.
 *
 *  The backend sometimes emits absolute media URLs with an `http://` scheme (its reverse proxy
 *  doesn't set the header Django needs to know the original request was HTTPS), even though the
 *  host only actually serves TLS. Android blocks cleartext HTTP by default, so those URLs silently
 *  fail to load - upgrade them to `https://` since it's the same TLS-terminated host either way. */
fun resolveImageUrl(path: String): String? {
    if (path.isBlank()) return null
    return when {
        path.startsWith("https://") -> path
        path.startsWith("http://") -> "https://" + path.removePrefix("http://")
        else -> ApiConfig.BASE_URL + (if (path.startsWith("/")) path else "/$path")
    }
}
