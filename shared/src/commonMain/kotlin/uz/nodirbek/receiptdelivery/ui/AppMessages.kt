package uz.nodirbek.receiptdelivery.ui

/** Strings needed inside plain (non-`@Composable`) classes - `AuthState`/`CartState`/`OrderState`/
 *  `LocationState`/`CookingState` set these on error/fallback paths reached from suspend functions,
 *  where `stringResource` can't be called directly. Resolved once via `stringResource` in
 *  RecipeApp.kt and handed to [AppState] at construction - since a language change recreates the
 *  whole Activity (see [ApplyAppLanguage]), that single resolution is always current. */
data class AppMessages(
    val guest: String,
    val selectAddress: String,
    val timerWord: String,
    val minutesShort: String,
    val inStock: String,
    val errorSendCode: String,
    val errorVerifyCode: String,
    val errorLoadRecipes: String,
    val errorLoadRecipe: String,
    val errorLoadOrders: String,
    val errorLoadOrder: String,
    val errorPlaceOrder: String,
    val errorSyncCart: String,
    val errorSyncAddress: String,
    val errorSelectSlot: String,
    val errorSelectAddressFirst: String,
    val errorDeliveryZoneNotFoundPrefix: String,
    val errorDeliveryZoneNotFoundSuffix: String
) {
    companion object {
        /** Used only until RecipeApp.kt's real, resource-backed instance is assigned - see
         *  [AppState.messages]. Never shown to a user; the real instance is set before any
         *  screen that could trigger these messages composes. */
        val PLACEHOLDER = AppMessages(
            guest = "", selectAddress = "", timerWord = "", minutesShort = "", inStock = "",
            errorSendCode = "", errorVerifyCode = "", errorLoadRecipes = "", errorLoadRecipe = "",
            errorLoadOrders = "", errorLoadOrder = "", errorPlaceOrder = "", errorSyncCart = "",
            errorSyncAddress = "", errorSelectSlot = "", errorSelectAddressFirst = "",
            errorDeliveryZoneNotFoundPrefix = "", errorDeliveryZoneNotFoundSuffix = ""
        )
    }
}
