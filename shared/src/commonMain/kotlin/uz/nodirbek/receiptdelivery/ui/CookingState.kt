package uz.nodirbek.receiptdelivery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.nodirbek.receiptdelivery.data.Recipe
import uz.nodirbek.receiptdelivery.data.Step

/** Cooking-mode timer/step state. Takes `recipe` as a parameter rather than owning a reference to it,
 *  so this class has no dependency on cart/recipe-selection state. */
class CookingState {
    var cookingStepIdx by mutableStateOf(0)
    var timerRunning by mutableStateOf(false)
    var timerSeconds by mutableStateOf(0)
    var rating by mutableStateOf(0)

    fun cookingDone(recipe: Recipe): Boolean = cookingStepIdx >= recipe.steps.size

    fun cookingDisplayIndex(recipe: Recipe): Int = minOf(cookingStepIdx + 1, recipe.steps.size)

    fun currentCookingStep(recipe: Recipe): Step? =
        recipe.steps.getOrNull(minOf(cookingStepIdx, recipe.steps.size - 1))

    fun timerLabel(recipe: Recipe): String {
        val step = currentCookingStep(recipe)
        return if (timerRunning) {
            val m = timerSeconds / 60
            val s = timerSeconds % 60
            val sLabel = if (s < 10) "0$s" else s.toString()
            "$m:$sLabel"
        } else if (step?.timerMinutes != null) {
            "Таймер ${step.timerMinutes} мин"
        } else ""
    }

    fun startTimer(recipe: Recipe) {
        val step = currentCookingStep(recipe)
        timerRunning = true
        timerSeconds = (step?.timerMinutes ?: 0) * 60
    }

    fun cookPrev() {
        cookingStepIdx = maxOf(0, cookingStepIdx - 1)
        timerRunning = false
    }

    fun cookNext() {
        cookingStepIdx += 1
        timerRunning = false
    }

    fun tickTimer() {
        if (timerRunning && timerSeconds > 0) {
            timerSeconds -= 1
            if (timerSeconds <= 0) timerRunning = false
        }
    }
}
