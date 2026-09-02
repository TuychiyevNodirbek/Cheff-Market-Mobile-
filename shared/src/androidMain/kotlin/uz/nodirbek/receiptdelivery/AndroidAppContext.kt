package uz.nodirbek.receiptdelivery

import android.content.Context

/**
 * Application context for the rare bit of shared androidMain code that needs one outside a
 * @Composable (e.g. reverse geocoding). Must be set once, early, from RecipeDeliveryApp.onCreate()
 * in the :androidApp module - see that class.
 */
lateinit var androidAppContext: Context
    private set

fun initAndroidAppContext(context: Context) {
    androidAppContext = context.applicationContext
}
