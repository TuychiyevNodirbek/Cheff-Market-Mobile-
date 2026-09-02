package uz.nodirbek.receiptdelivery.data

import org.jetbrains.compose.resources.DrawableResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.dish_lagman
import uz.nodirbek.receiptdelivery.shared.resources.dish_manty
import uz.nodirbek.receiptdelivery.shared.resources.dish_plov
import uz.nodirbek.receiptdelivery.shared.resources.dish_shakshuka

private val IMAGE_RES_BY_KEY: Map<String, DrawableResource> = mapOf(
    "lagman" to Res.drawable.dish_lagman,
    "plov" to Res.drawable.dish_plov,
    "shakshuka" to Res.drawable.dish_shakshuka,
    "manty" to Res.drawable.dish_manty
)

/** Resolves a Recipe.imageKey to a Compose Multiplatform drawable resource - works identically
 *  on Android and iOS, unlike the old Int (Android resId) version this replaced. */
fun imageResFor(key: String): DrawableResource = IMAGE_RES_BY_KEY.getValue(key)
