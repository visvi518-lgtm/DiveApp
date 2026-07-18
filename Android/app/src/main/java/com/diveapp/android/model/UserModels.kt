package com.diveapp.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileSetupRequest(
    val nickname: String,
    @SerialName("profile_image_url") val profileImageURL: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
)

@Serializable
data class UserProfileResponse(
    val nickname: String,
    @SerialName("profile_image_url") val profileImageURL: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    val bio: String?,
)

@Serializable
data class CurrentUser(
    val id: String,
    val email: String,
    val provider: AuthProvider,
    val role: UserRole,
    @SerialName("account_status") val accountStatus: AccountStatus,
    val profile: UserProfileResponse?,
)
