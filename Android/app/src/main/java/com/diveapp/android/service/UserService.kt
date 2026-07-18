package com.diveapp.android.service

import com.diveapp.android.model.CurrentUser
import com.diveapp.android.model.ProfileSetupRequest
import com.diveapp.android.model.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @GET("api/v1/users/me")
    suspend fun fetchCurrentUser(): CurrentUser

    @POST("api/v1/users/me/profile")
    suspend fun setupProfile(@Body body: ProfileSetupRequest): UserProfileResponse
}
