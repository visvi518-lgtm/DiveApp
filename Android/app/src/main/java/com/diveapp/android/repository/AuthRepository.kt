package com.diveapp.android.repository

import com.diveapp.android.core.auth.TokenStorage
import com.diveapp.android.core.network.ApiException
import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.AuthProvider
import com.diveapp.android.model.RefreshTokenRequest
import com.diveapp.android.model.SocialLoginRequest
import com.diveapp.android.service.AuthService

class AuthRepository(
    private val authService: AuthService,
    private val tokenStorage: TokenStorage,
) {
    /** Returns whether this is the user's first login (caller should route to Profile Setup). */
    suspend fun login(provider: AuthProvider, providerToken: String): Boolean {
        val tokens = apiCall { authService.login(provider.name, SocialLoginRequest(providerToken)) }
        tokenStorage.save(tokens.accessToken, tokens.refreshToken)
        return tokens.isNewUser
    }

    suspend fun refreshAccessToken(): String {
        val refreshToken = tokenStorage.refreshToken ?: throw ApiException.Unauthorized
        val response = apiCall { authService.refresh(RefreshTokenRequest(refreshToken)) }
        tokenStorage.accessToken = response.accessToken
        return response.accessToken
    }

    suspend fun logout() {
        runCatching { apiCall { authService.logout() } }
        tokenStorage.clear()
    }

    /** Clears local session state without calling the API — used when the
     * server has already rejected the refresh token (see TokenAuthenticator). */
    fun clearLocalSession() {
        tokenStorage.clear()
    }

    val hasStoredSession: Boolean
        get() = tokenStorage.refreshToken != null
}
