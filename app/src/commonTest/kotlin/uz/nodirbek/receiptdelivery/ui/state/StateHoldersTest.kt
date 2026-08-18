package uz.nodirbek.receiptdelivery.ui.state

import uz.nodirbek.receiptdelivery.data.AuthSnapshot
import uz.nodirbek.receiptdelivery.data.RECIPES
import uz.nodirbek.receiptdelivery.data.SettingsSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthStateTest {
    @Test
    fun snapshot_reflects_current_fields() {
        val auth = AuthState().apply {
            isAuthenticated = true
            userPhone = "+998901234567"
            userName = "Nodir"
        }
        assertEquals(AuthSnapshot(true, "+998901234567", "Nodir"), auth.snapshot())
    }
}

class SettingsStateTest {
    @Test
    fun toggleDietaryPref_adds_then_removes() {
        val settings = SettingsState()
        settings.toggleDietaryPref("Острое")
        assertTrue("Острое" in settings.dietaryPrefs)
        settings.toggleDietaryPref("Острое")
        assertFalse("Острое" in settings.dietaryPrefs)
    }

    @Test
    fun apply_snapshot_replaces_all_fields() {
        val settings = SettingsState()
        settings.apply(
            SettingsSnapshot(
                notificationsEnabled = false,
                language = "uz",
                payment = "cash",
                dietaryPrefs = setOf("Без лактозы")
            )
        )
        assertEquals(false, settings.notificationsEnabled)
        assertEquals("uz", settings.language)
        assertEquals("cash", settings.payment)
        assertEquals(setOf("Без лактозы"), settings.dietaryPrefs.toSet())
    }
}

class CookingStateTest {
    private val recipe = RECIPES.getValue("shakshuka") // 4 steps, has at least one timed step

    @Test
    fun cookingDone_true_once_past_last_step() {
        val cooking = CookingState()
        assertFalse(cooking.cookingDone(recipe))
        repeat(recipe.steps.size) { cooking.cookNext() }
        assertTrue(cooking.cookingDone(recipe))
    }

    @Test
    fun cookNext_and_cookPrev_stop_at_bounds() {
        val cooking = CookingState()
        cooking.cookPrev()
        assertEquals(0, cooking.cookingStepIdx)
        repeat(10) { cooking.cookNext() }
        assertEquals(10, cooking.cookingStepIdx)
        cooking.cookPrev()
        assertEquals(9, cooking.cookingStepIdx)
    }

    @Test
    fun startTimer_sets_seconds_from_current_step() {
        val cooking = CookingState()
        val step = cooking.currentCookingStep(recipe)
        cooking.startTimer(recipe)
        assertTrue(cooking.timerRunning)
        assertEquals((step?.timerMinutes ?: 0) * 60, cooking.timerSeconds)
    }

    @Test
    fun tickTimer_counts_down_and_stops_at_zero() {
        val cooking = CookingState().apply {
            timerRunning = true
            timerSeconds = 2
        }
        cooking.tickTimer()
        assertEquals(1, cooking.timerSeconds)
        assertTrue(cooking.timerRunning)
        cooking.tickTimer()
        assertEquals(0, cooking.timerSeconds)
        assertFalse(cooking.timerRunning)
    }

    @Test
    fun cookNext_resets_running_timer() {
        val cooking = CookingState().apply { timerRunning = true }
        cooking.cookNext()
        assertFalse(cooking.timerRunning)
    }

    @Test
    fun currentCookingStep_null_safe_past_the_end() {
        val cooking = CookingState()
        repeat(recipe.steps.size + 5) { cooking.cookNext() }
        // Clamped to the last valid step, never null/out-of-bounds.
        assertEquals(recipe.steps.last(), cooking.currentCookingStep(recipe))
    }
}
