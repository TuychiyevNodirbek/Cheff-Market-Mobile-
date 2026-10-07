package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uz.nodirbek.receiptdelivery.shared.resources.Res
import uz.nodirbek.receiptdelivery.shared.resources.no_internet_body
import uz.nodirbek.receiptdelivery.shared.resources.no_internet_title
import uz.nodirbek.receiptdelivery.shared.resources.retry
import uz.nodirbek.receiptdelivery.ui.theme.Surface
import uz.nodirbek.receiptdelivery.ui.theme.TextDark
import uz.nodirbek.receiptdelivery.ui.theme.TextMuted

/** Blocking screen shown whenever the device has no internet connection. */
@Composable
fun NoInternetScreen(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(Surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📡", fontSize = 56.sp)
        Text(
            stringResource(Res.string.no_internet_title),
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            stringResource(Res.string.no_internet_body),
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        PrimaryButton(
            text = stringResource(Res.string.retry),
            onClick = onRetry,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
