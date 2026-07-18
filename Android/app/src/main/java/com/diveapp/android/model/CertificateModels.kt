package com.diveapp.android.model

import com.diveapp.android.core.network.LocalDateSerializer
import com.diveapp.android.core.network.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.OffsetDateTime

@Serializable
data class CertificateCreateRequest(
    val organization: CertificationOrganization,
    @SerialName("certification_level") val certificationLevel: String,
    @SerialName("certification_number") val certificationNumber: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("issue_date") val issueDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("expiration_date") val expirationDate: LocalDate? = null,
    val instructor: String? = null,
    @SerialName("dive_center") val diveCenter: String? = null,
    @SerialName("certificate_image_url") val certificateImageUrl: String? = null,
    val memo: String? = null,
)

@Serializable
data class CertificateResponse(
    val id: String,
    val organization: CertificationOrganization,
    @SerialName("certification_level") val certificationLevel: String,
    @SerialName("certification_number") val certificationNumber: String?,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("issue_date") val issueDate: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("expiration_date") val expirationDate: LocalDate?,
    val instructor: String?,
    @SerialName("dive_center") val diveCenter: String?,
    @SerialName("certificate_image_url") val certificateImageUrl: String?,
    val memo: String?,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updated_at") val updatedAt: OffsetDateTime,
)
