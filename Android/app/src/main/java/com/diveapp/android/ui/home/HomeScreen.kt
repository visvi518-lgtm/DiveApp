package com.diveapp.android.ui.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.repository.InformationRepository
import com.diveapp.android.ui.information.InformationDetailScreen
import com.diveapp.android.ui.information.InformationListScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_ARTICLES = "articles"
private const val ROUTE_ARTICLE_DETAIL = "articles/{id}"

/** Home tab: main content + a nested Information (정보 게시판) stack, since
 * Information isn't its own bottom tab per Docs/03_UserFlow.md. */
@Composable
fun HomeScreen(authSession: AuthSession, informationRepository: InformationRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeMainScreen(authSession = authSession, onArticlesClick = { navController.navigate(ROUTE_ARTICLES) })
        }
        composable(ROUTE_ARTICLES) {
            InformationListScreen(
                repository = informationRepository,
                onArticleClick = { id -> navController.navigate("articles/$id") },
            )
        }
        composable(
            ROUTE_ARTICLE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            InformationDetailScreen(
                repository = informationRepository,
                articleId = id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
