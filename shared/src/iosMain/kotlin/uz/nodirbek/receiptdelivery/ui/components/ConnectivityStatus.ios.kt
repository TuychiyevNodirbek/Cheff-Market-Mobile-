package uz.nodirbek.receiptdelivery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Deliberate placeholder: always reports "online". A real implementation needs Network.framework's
 * NWPathMonitor via Kotlin/Native cinterop, which involves C function-pointer callbacks that can't
 * be written blind with any confidence on a machine with no Kotlin/Native iOS compiler - see
 * docs/ios-phase-plan.md §3.3. Replace once on a Mac.
 */
@Composable
actual fun connectivityState(): State<Boolean> = remember { mutableStateOf(true) }
