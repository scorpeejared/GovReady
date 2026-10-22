package com.example.govready

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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

/** The navigation graph: every screen, and every place one screen leads to another. */
@Composable
fun GovReadyApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { entry ->
            HomeScreen(
                saved = vm.saved,
                inProgress = vm.details,
                checkedCount = vm.checked.size,
                onCheckRequirements = { nav.navigateFrom(entry, Routes.PURPOSE) },
                onPopularClick = { nav.navigateFrom(entry, Routes.details(it)) },
                onInProgressClick = { nav.navigateFrom(entry, Routes.CHECKLIST) },
                onSeeAll = { nav.navigateFrom(entry, Routes.SAVED) },
                onSavedClick = {
                    if (nav.navigateFrom(entry, Routes.CHECKLIST)) vm.openSaved(it)
                }
            )
        }
        composable(Routes.PURPOSE) { entry ->
            PurposeScreen(
                onBack = { nav.backFrom(entry) },
                onSelect = { nav.navigateFrom(entry, Routes.details(it)) }
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(
                navArgument(Routes.ARG_TRANSACTION) { type = NavType.StringType },
                navArgument(Routes.ARG_EDIT) { type = NavType.BoolType; defaultValue = false }
            )
        ) { entry ->
            val name = entry.arguments?.getString(Routes.ARG_TRANSACTION).orEmpty()
            val edit = entry.arguments?.getBoolean(Routes.ARG_EDIT) ?: false
            DetailsScreen(
                transaction = name,
                initial = if (edit) vm.details else null,
                onBack = { nav.backFrom(entry) },
                onGenerate = { details ->
                    // A fresh result is a clean surface: Home -> Checklist, replacing any older one.
                    if (nav.navigateFrom(entry, Routes.CHECKLIST) { popUpTo(Routes.HOME) }) vm.start(details)
                }
            )
        }
        composable(Routes.CHECKLIST) { entry ->
            ChecklistScreen(
                vm = vm,
                onBack = { nav.backFrom(entry) },
                onEditDetails = {
                    vm.details?.let { nav.navigateFrom(entry, Routes.details(it.transaction, edit = true)) }
                }
            )
        }
        composable(Routes.SAVED) { entry ->
            SavedScreen(
                saved = vm.saved,
                onBack = { nav.backFrom(entry) },
                onOpen = {
                    if (nav.navigateFrom(entry, Routes.CHECKLIST)) vm.openSaved(it)
                }
            )
        }
    }
}
