package com.diveapp.android.model

import kotlinx.serialization.Serializable

@Serializable
enum class AuthProvider {
    NAVER,
    GOOGLE,
}

@Serializable
enum class UserRole {
    USER,
    ADMIN,
}

@Serializable
enum class AccountStatus {
    ACTIVE,
    DORMANT,
    SUSPENDED,
    DELETED,
}

@Serializable
enum class DiveType {
    FREEDIVING,
    SCUBA,
}

@Serializable
enum class CertificationOrganization {
    AIDA,
    PADI,
    SSI,
    RAID,
    SDI,
    NAUI,
    CMAS,
}

@Serializable
enum class BannerType {
    NOTICE,
    EVENT,
    PROMOTION,
    INFORMATION,
}
