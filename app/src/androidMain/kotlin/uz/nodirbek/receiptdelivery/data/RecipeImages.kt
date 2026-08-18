package uz.nodirbek.receiptdelivery.data

import uz.nodirbek.receiptdelivery.R

private val IMAGE_RES_BY_KEY: Map<String, Int> = mapOf(
    "lagman" to R.drawable.dish_lagman,
    "plov" to R.drawable.dish_plov,
    "shakshuka" to R.drawable.dish_shakshuka,
    "manty" to R.drawable.dish_manty
)

/** Resolves a Recipe.imageKey (from the platform-agnostic commonMain catalog) to an Android drawable resource id. */
fun imageResFor(key: String): Int = IMAGE_RES_BY_KEY.getValue(key)
