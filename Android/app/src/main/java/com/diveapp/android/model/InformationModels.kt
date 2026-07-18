package com.diveapp.android.model

import com.diveapp.android.core.network.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class InformationArticleListItem(
    val id: String,
    val title: String,
    @SerialName("thumbnail_image_url") val thumbnailImageUrl: String?,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("published_at") val publishedAt: OffsetDateTime?,
)

@Serializable
data class InformationArticleResponse(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("thumbnail_image_url") val thumbnailImageUrl: String?,
    @SerialName("view_count") val viewCount: Int,
    @SerialName("is_published") val isPublished: Boolean,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("published_at") val publishedAt: OffsetDateTime?,
)
