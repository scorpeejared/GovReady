package com.example.govready

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GovReadyTheme {
                GovReadyApp()
            }
        }
    }
}

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
