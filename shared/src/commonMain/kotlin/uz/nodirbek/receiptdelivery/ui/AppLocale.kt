package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.Composable

/** Applies `languageCode` ("ru"/"uz") as the process locale so compose-resources' `stringResource`
 *  picks the matching `composeResources/values-<code>/strings.xml` folder, then forces whatever
 *  screens are already composed to re-read it. `stringResource` isn't reactive to a locale change
 *  by itself (see [org.jetbrains.compose.resources]'s `Locale.current` read, which isn't
 *  snapshot-backed), so the only reliable way to refresh already-composed text is to rebuild the
 *  composition - Android does that via [android.app.Activity.recreate]. Skipped on the very first
 *  call for a given `languageCode` (see call site in RecipeApp.kt) since [MainActivity] already
 *  applies the persisted language before the first composition. */
@Composable
expect fun ApplyAppLanguage(languageCode: String)
