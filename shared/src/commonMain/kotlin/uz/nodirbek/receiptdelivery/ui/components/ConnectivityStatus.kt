package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/** Tracks live internet availability, updating as the device connects/disconnects. */
@Composable
expect fun connectivityState(): State<Boolean>
