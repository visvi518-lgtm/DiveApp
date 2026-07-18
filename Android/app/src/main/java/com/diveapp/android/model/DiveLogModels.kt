package com.diveapp.android.model

import com.diveapp.android.core.network.LocalDateSerializer
import com.diveapp.android.core.network.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.OffsetDateTime

@Serializable
data class DiveLocationInput(
    val name: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("naver_place_id") val naverPlaceId: String? = null,
    val country: String? = null,
    val city: String? = null,
)

@Serializable
data class DiveLocationResponse(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val city: String?,
)

@Serializable
data class FreedivingDetailInput(
    @SerialName("max_depth") val maxDepth: Double,
    @SerialName("dive_time_seconds") val diveTimeSeconds: Int,
)

@Serializable
data class ScubaDetailInput(
    @SerialName("max_depth") val maxDepth: Double,
    @SerialName("dive_time_seconds") val diveTimeSeconds: Int,
    @SerialName("tank_pressure_start") val tankPressureStart: Int,
    @SerialName("tank_pressure_end") val tankPressureEnd: Int,
)

@Serializable
data class DivePhotoInput(
    @SerialName("image_url") val imageUrl: String,
    @SerialName("display_order") val displayOrder: Int = 1,
)

@Serializable
data class DiveLogCreateRequest(
    @SerialName("dive_type") val diveType: DiveType,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("dive_date") val diveDate: LocalDate,
    val location: DiveLocationInput,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val memo: String? = null,
    val freediving: FreedivingDetailInput? = null,
    val scuba: ScubaDetailInput? = null,
    val photos: List<DivePhotoInput> = emptyList(),
)

@Serializable
data class DiveLogUpdateRequest(
    val memo: String? = null,
    val freediving: FreedivingDetailInput? = null,
    val scuba: ScubaDetailInput? = null,
)

@Serializable
data class FreedivingDetailResponse(
    @SerialName("max_depth") val maxDepth: Double,
    @SerialName("dive_time_seconds") val diveTimeSeconds: Int,
)

@Serializable
data class ScubaDetailResponse(
    @SerialName("max_depth") val maxDepth: Double,
    @SerialName("dive_time_seconds") val diveTimeSeconds: Int,
    @SerialName("tank_pressure_start") val tankPressureStart: Int,
    @SerialName("tank_pressure_end") val tankPressureEnd: Int,
)

@Serializable
data class DivePhotoResponse(
    val id: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("display_order") val displayOrder: Int,
)

@Serializable
data class DiveLogResponse(
    val id: String,
    @SerialName("dive_type") val diveType: DiveType,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("dive_date") val diveDate: LocalDate,
    val location: DiveLocationResponse,
    val memo: String?,
    val freediving: FreedivingDetailResponse?,
    val scuba: ScubaDetailResponse?,
    val photos: List<DivePhotoResponse>,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("created_at") val createdAt: OffsetDateTime,
)

@Serializable
data class DiveLogListItem(
    val id: String,
    @SerialName("dive_type") val diveType: DiveType,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("dive_date") val diveDate: LocalDate,
    @SerialName("location_name") val locationName: String,
    @SerialName("cover_image_url") val coverImageUrl: String?,
    @SerialName("max_depth") val maxDepth: Double,
)

@Serializable
data class DiveLogStatisticsResponse(
    @SerialName("total_dive_count") val totalDiveCount: Int,
    @SerialName("max_depth_overall") val maxDepthOverall: Double,
    @SerialName("total_dive_time_seconds") val totalDiveTimeSeconds: Int,
    @SerialName("freediving_count") val freedivingCount: Int,
    @SerialName("scuba_count") val scubaCount: Int,
)
