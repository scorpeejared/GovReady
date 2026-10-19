package com.example.govready

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/** Every route and argument name lives here; nothing else spells them out. */
object Routes {
    const val HOME = "home"
    const val PURPOSE = "purpose"
    const val CHECKLIST = "checklist"
    const val SAVED = "saved"

    const val ARG_TRANSACTION = "transaction"
    const val ARG_EDIT = "edit"
    const val DETAILS = "details/{$ARG_TRANSACTION}?$ARG_EDIT={$ARG_EDIT}"

    fun details(name: String, edit: Boolean = false) = "details/${Uri.encode(name)}?$ARG_EDIT=$edit"
}

/**
 * A screen only accepts navigation once its transition has finished (RESUMED). Rapid or
 * repeated taps while it is animating out are ignored, so they can't stack duplicate
 * screens or pop the root off the back stack.
 */
private val NavBackStackEntry.accepting: Boolean
    get() = lifecycle.currentState == Lifecycle.State.RESUMED

/** Returns true if the navigation was accepted, so callers only change state for taps that count. */
fun NavController.navigateFrom(
    entry: NavBackStackEntry,
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
): Boolean {
    if (!entry.accepting) return false
    navigate(route) {
        launchSingleTop = true
        builder()
    }
    return true
}

fun NavController.backFrom(entry: NavBackStackEntry) {
    if (entry.accepting) popBackStack()
}
