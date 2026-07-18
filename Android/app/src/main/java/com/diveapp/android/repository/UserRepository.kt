package com.diveapp.android.repository

import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.CurrentUser
import com.diveapp.android.model.ProfileSetupRequest
import com.diveapp.android.model.UserProfileResponse
import com.diveapp.android.service.UserService

class UserRepository(private val userService: UserService) {
    suspend fun fetchCurrentUser(): CurrentUser = apiCall { userService.fetchCurrentUser() }

    suspend fun setupProfile(nickname: String, profileImageUrl: String?, phoneNumber: String?): UserProfileResponse =
        apiCall { userService.setupProfile(ProfileSetupRequest(nickname, profileImageUrl, phoneNumber)) }
}
