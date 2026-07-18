package com.diveapp.android.service

import com.diveapp.android.model.AccessTokenResponse
import com.diveapp.android.model.RefreshTokenRequest
import com.diveapp.android.model.SocialLoginRequest
import com.diveapp.android.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {
    @Headers("X-No-Auth: true")
    @POST("api/v1/auth/login/{provider}")
    suspend fun login(@Path("provider") provider: String, @Body body: SocialLoginRequest): TokenResponse

    @Headers("X-No-Auth: true")
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): AccessTokenResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>
}
