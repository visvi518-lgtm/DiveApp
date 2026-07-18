package com.diveapp.android.core.network

import com.diveapp.android.core.auth.TokenStorage
import com.diveapp.android.model.AccessTokenResponse
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Route

/**
 * On a 401, exchanges the stored refresh token for a new access token and
 * retries once. Uses a bare OkHttpClient (no interceptors) to avoid recursing
 * back into itself. If the refresh call itself fails, the session is cleared
 * and [onSessionExpired] is invoked so the UI can fall back to the login screen.
 */
class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val onSessionExpired: () -> Unit,
) : Authenticator {
    private val plainClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        if (responseCount(response) > 1) {
            // Already retried once; give up to avoid an infinite loop.
            return null
        }

        val refreshToken = tokenStorage.refreshToken ?: run {
            onSessionExpired()
            return null
        }

        val newAccessToken = runCatching { refreshSynchronously(refreshToken) }.getOrNull()
        if (newAccessToken == null) {
            tokenStorage.clear()
            onSessionExpired()
            return null
        }

        tokenStorage.accessToken = newAccessToken
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun refreshSynchronously(refreshToken: String): String? {
        val body = """{"refresh_token":"$refreshToken"}""".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(ApiConfig.BASE_URL + "api/v1/auth/refresh")
            .post(body)
            .build()

        plainClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val raw = response.body?.string() ?: return null
            return json.decodeFromString(AccessTokenResponse.serializer(), raw).accessToken
        }
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
