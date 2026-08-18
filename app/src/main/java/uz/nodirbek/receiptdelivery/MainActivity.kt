package uz.nodirbek.receiptdelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uz.nodirbek.receiptdelivery.ui.RecipeApp
import uz.nodirbek.receiptdelivery.ui.theme.ReceipeDeliveryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceipeDeliveryTheme {
                RecipeApp()
            }
        }
    }
}
