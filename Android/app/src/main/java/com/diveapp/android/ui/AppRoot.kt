package com.diveapp.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.diveapp.android.core.AppContainer
import com.diveapp.android.core.auth.AuthState
import com.diveapp.android.ui.auth.LoginScreen
import com.diveapp.android.ui.auth.ProfileSetupScreen
import com.diveapp.android.ui.navigation.RootTabScaffold
import com.diveapp.android.ui.splash.SplashScreen

/** Switches between Splash / Login / Profile Setup / the main tab flow based
 * on AuthSession.state (Docs/03_UserFlow.md Authentication flow). */
@Composable
fun AppRoot(container: AppContainer) {
    val authSession = container.authSession
    val state by authSession.state.collectAsState()

    when (state) {
        AuthState.BOOTSTRAPPING -> SplashScreen(authSession)
        AuthState.UNAUTHENTICATED -> LoginScreen(authSession, container.naverProvider, container.googleProvider)
        AuthState.NEEDS_PROFILE_SETUP -> ProfileSetupScreen(authSession, container.userRepository)
        AuthState.AUTHENTICATED -> RootTabScaffold(container)
    }
}
