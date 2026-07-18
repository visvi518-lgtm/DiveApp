package com.diveapp.android.ui.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.repository.CommunityRepository

private const val ROUTE_LIST = "list"
private const val ROUTE_CREATE = "create"
private const val ROUTE_DETAIL = "detail/{id}"
private const val ROUTE_EDIT = "edit/{id}"

/** Community tab: Board(단일 게시판) -> Post List -> Post Detail -> Write/Edit
 * (Docs/03_UserFlow.md Community flow). */
@Composable
fun CommunityScreen(repository: CommunityRepository, authSession: AuthSession) {
    val navController = rememberNavController()
    val currentUser by authSession.currentUser.collectAsState()

    NavHost(navController = navController, startDestination = ROUTE_LIST) {
        composable(ROUTE_LIST) {
            CommunityPostListScreen(
                repository = repository,
                onAddClick = { navController.navigate(ROUTE_CREATE) },
                onPostClick = { id -> navController.navigate("detail/$id") },
            )
        }
        composable(ROUTE_CREATE) {
            CommunityPostFormScreen(
                repository = repository,
                postId = null,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            CommunityPostDetailScreen(
                repository = repository,
                postId = id,
                currentUserId = currentUser?.id,
                onEditClick = { navController.navigate("edit/$id") },
                onDeleted = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_EDIT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            CommunityPostFormScreen(
                repository = repository,
                postId = id,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
