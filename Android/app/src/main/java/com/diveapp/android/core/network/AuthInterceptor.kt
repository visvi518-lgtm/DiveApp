package com.diveapp.android.core.network

import com.diveapp.android.core.auth.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the current access token to every request unless it's tagged with
 * the [NO_AUTH_HEADER] header (see the `@Headers("X-No-Auth: true")` calls on
 * AuthService's login/refresh methods).
 */
class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (original.header(NO_AUTH_HEADER) != null) {
            val stripped = original.newBuilder().removeHeader(NO_AUTH_HEADER).build()
            return chain.proceed(stripped)
        }

        val token = tokenStorage.accessToken
        val authorized = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }
        return chain.proceed(authorized)
    }

    companion object {
        const val NO_AUTH_HEADER = "X-No-Auth"
    }
}
