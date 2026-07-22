package com.diveapp.android.ui.divelog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
private const val ROUTE_LOCATION_PICKER = "location-picker"

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
        composable(ROUTE_CREATE) { backStackEntry ->
            val pickedSiteName by backStackEntry.savedStateHandle.getStateFlow<String?>("picked_site_name", null).collectAsState()
            val pickedLatitude by backStackEntry.savedStateHandle.getStateFlow<String?>("picked_latitude", null).collectAsState()
            val pickedLongitude by backStackEntry.savedStateHandle.getStateFlow<String?>("picked_longitude", null).collectAsState()

            DiveLogCreateScreen(
                repository = repository,
                pickedSiteName = pickedSiteName,
                pickedLatitude = pickedLatitude,
                pickedLongitude = pickedLongitude,
                onPickedLocationConsumed = {
                    backStackEntry.savedStateHandle["picked_site_name"] = null
                    backStackEntry.savedStateHandle["picked_latitude"] = null
                    backStackEntry.savedStateHandle["picked_longitude"] = null
                },
                onPickLocationClick = { siteName, latitude, longitude ->
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("initial_site_name", siteName)
                        set("initial_latitude", latitude)
                        set("initial_longitude", longitude)
                    }
                    navController.navigate(ROUTE_LOCATION_PICKER)
                },
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_LOCATION_PICKER) {
            val initialSiteName = navController.previousBackStackEntry?.savedStateHandle?.get<String>("initial_site_name").orEmpty()
            val initialLatitude = navController.previousBackStackEntry?.savedStateHandle?.get<String>("initial_latitude")?.toDoubleOrNull()
            val initialLongitude = navController.previousBackStackEntry?.savedStateHandle?.get<String>("initial_longitude")?.toDoubleOrNull()

            DiveLogLocationPickerScreen(
                initialSiteName = initialSiteName,
                initialLatitude = initialLatitude,
                initialLongitude = initialLongitude,
                onConfirm = { picked ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("picked_site_name", picked.siteName)
                        set("picked_latitude", picked.latitude.toString())
                        set("picked_longitude", picked.longitude.toString())
                    }
                    navController.popBackStack()
                },
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
