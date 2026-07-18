package com.diveapp.android.ui.mypage

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.repository.CertificateRepository
import com.diveapp.android.ui.certificate.CertificateDetailScreen
import com.diveapp.android.ui.certificate.CertificateFormScreen
import com.diveapp.android.ui.certificate.CertificateListScreen

private const val ROUTE_MYPAGE = "mypage"
private const val ROUTE_CERT_LIST = "certificates"
private const val ROUTE_CERT_ADD = "certificates/add"
private const val ROUTE_CERT_DETAIL = "certificates/{id}"
private const val ROUTE_CERT_EDIT = "certificates/{id}/edit"

/** My Page tab: identity summary/logout + a nested Certification stack
 * (Docs/07_Screens.md "My Certifications"). */
@Composable
fun MyPageScreen(authSession: AuthSession, certificateRepository: CertificateRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_MYPAGE) {
        composable(ROUTE_MYPAGE) {
            MyPageMainScreen(authSession = authSession, onCertificatesClick = { navController.navigate(ROUTE_CERT_LIST) })
        }
        composable(ROUTE_CERT_LIST) {
            CertificateListScreen(
                repository = certificateRepository,
                onAddClick = { navController.navigate(ROUTE_CERT_ADD) },
                onCertificateClick = { id -> navController.navigate("certificates/$id") },
            )
        }
        composable(ROUTE_CERT_ADD) {
            CertificateFormScreen(
                repository = certificateRepository,
                certificateId = null,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_CERT_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            CertificateDetailScreen(
                repository = certificateRepository,
                certificateId = id,
                onEditClick = { navController.navigate("certificates/$id/edit") },
                onDeleted = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_CERT_EDIT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            CertificateFormScreen(
                repository = certificateRepository,
                certificateId = id,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
