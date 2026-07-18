package com.diveapp.android.ui.divelog

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diveapp.android.repository.DiveLogRepository

private const val ROUTE_LIST = "list"
private const val ROUTE_CREATE = "create"
private const val ROUTE_DETAIL = "detail/{id}"
private const val ROUTE_EDIT = "edit/{id}"

/** Dive Log tab: its own nested nav graph for List -> Detail -> Edit / Create
 * (Docs/03_UserFlow.md Dive Log flow). */
@Composable
fun DiveLogScreen(repository: DiveLogRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_LIST) {
        composable(ROUTE_LIST) {
            DiveLogListScreen(
                repository = repository,
                onAddClick = { navController.navigate(ROUTE_CREATE) },
                onDiveLogClick = { id -> navController.navigate("detail/$id") },
            )
        }
        composable(ROUTE_CREATE) {
            DiveLogCreateScreen(
                repository = repository,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            DiveLogDetailScreen(
                repository = repository,
                diveLogId = id,
                onEditClick = { navController.navigate("edit/$id") },
                onDeleted = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_EDIT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            DiveLogEditScreen(
                repository = repository,
                diveLogId = id,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
