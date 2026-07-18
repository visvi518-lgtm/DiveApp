package com.diveapp.android.core.auth

import com.diveapp.android.model.AuthProvider
import com.diveapp.android.model.CurrentUser
import com.diveapp.android.repository.AuthRepository
import com.diveapp.android.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthState {
    BOOTSTRAPPING,
    UNAUTHENTICATED,
    NEEDS_PROFILE_SETUP,
    AUTHENTICATED,
}

/**
 * Single source of truth for auth state, observed by the navigation root to
 * switch between Splash / Login / Profile Setup / the main tab flow.
 */
class AuthSession(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val externalScope: CoroutineScope,
) {
    private val _state = MutableStateFlow(AuthState.BOOTSTRAPPING)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    suspend fun bootstrap() {
        if (!authRepository.hasStoredSession) {
            _state.value = AuthState.UNAUTHENTICATED
            return
        }
        try {
            authRepository.refreshAccessToken()
            loadCurrentUser()
        } catch (e: Exception) {
            authRepository.clearLocalSession()
            _currentUser.value = null
            _state.value = AuthState.UNAUTHENTICATED
        }
    }

    suspend fun login(provider: AuthProvider, providerToken: String) {
        val isNewUser = authRepository.login(provider, providerToken)
        if (isNewUser) {
            _state.value = AuthState.NEEDS_PROFILE_SETUP
        } else {
            loadCurrentUser()
        }
    }

    suspend fun completeProfileSetup() {
        try {
            loadCurrentUser()
        } catch (e: Exception) {
            _state.value = AuthState.NEEDS_PROFILE_SETUP
        }
    }

    suspend fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _state.value = AuthState.UNAUTHENTICATED
    }

    /** Called synchronously from [com.diveapp.android.core.network.TokenAuthenticator]
     * on a background thread when the refresh token itself is rejected. */
    fun handleSessionExpired() {
        externalScope.launch {
            _currentUser.value = null
            _state.value = AuthState.UNAUTHENTICATED
        }
    }

    private suspend fun loadCurrentUser() {
        val user = userRepository.fetchCurrentUser()
        _currentUser.value = user
        _state.value = if (user.profile == null) AuthState.NEEDS_PROFILE_SETUP else AuthState.AUTHENTICATED
    }
}
