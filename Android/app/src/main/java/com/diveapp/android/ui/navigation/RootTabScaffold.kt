package com.diveapp.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diveapp.android.core.AppContainer
import com.diveapp.android.ui.community.CommunityScreen
import com.diveapp.android.ui.divelog.DiveLogScreen
import com.diveapp.android.ui.home.HomeScreen
import com.diveapp.android.ui.mypage.MyPageScreen
import com.diveapp.android.ui.training.TrainingScreen

private enum class RootTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Filled.Home),
    DIVE_LOG("dive_log", "다이브 로그", Icons.AutoMirrored.Filled.MenuBook),
    TRAINING("training", "CO₂ Table", Icons.Filled.Timer),
    COMMUNITY("community", "커뮤니티", Icons.Filled.Groups),
    MY_PAGE("my_page", "마이페이지", Icons.Filled.Person),
}

/** Main tab structure (Docs/03_UserFlow.md Home / Dive Log / CO2 Table /
 * Community / My Page). Each tab owns its own nested nav graph for its
 * list/detail/create/edit screens. */
@Composable
fun RootTabScaffold(container: AppContainer) {
    val navController = rememberNavController()
    val authSession = container.authSession

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                RootTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RootTab.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(RootTab.HOME.route) { HomeScreen(authSession, container.informationRepository) }
            composable(RootTab.DIVE_LOG.route) { DiveLogScreen(container.diveLogRepository) }
            composable(RootTab.TRAINING.route) { TrainingScreen(container.trainingRepository) }
            composable(RootTab.COMMUNITY.route) { CommunityScreen(container.communityRepository, authSession) }
            composable(RootTab.MY_PAGE.route) { MyPageScreen(authSession, container.certificateRepository) }
        }
    }
}
