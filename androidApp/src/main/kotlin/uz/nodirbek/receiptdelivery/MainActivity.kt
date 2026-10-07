package uz.nodirbek.receiptdelivery

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import uz.nodirbek.receiptdelivery.ui.RecipeApp
import uz.nodirbek.receiptdelivery.ui.persistedLanguage
import uz.nodirbek.receiptdelivery.ui.theme.ReceipeDeliveryTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must run before setContent - stringResource() picks its language from
        // Locale.getDefault() (see ui/AppLocale.android.kt), so the persisted choice has to be
        // applied before the first composition, not reacted to afterwards.
        applyPersistedLocale()
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            ReceipeDeliveryTheme {
                RecipeApp()
            }
        }
    }

    private fun applyPersistedLocale() {
        Locale.setDefault(Locale(persistedLanguage(this)))
    }

    // Android 13+ requires this at runtime, otherwise Chucker's request-log notification never appears.
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
