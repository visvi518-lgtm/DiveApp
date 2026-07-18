package com.diveapp.android.model

import com.diveapp.android.core.network.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class AuthorResponse(
    val id: String,
    val nickname: String?,
)

@Serializable
data class CommunityPostCreateRequest(
    val title: String,
    val content: String,
)

@Serializable
data class CommunityPostUpdateRequest(
    val title: String? = null,
    val content: String? = null,
)

@Serializable
data class CommunityPostResponse(
    val id: String,
    val title: String,
    val content: String,
    val author: AuthorResponse,
    @SerialName("view_count") val viewCount: Int,
    @SerialName("like_count") val likeCount: Int,
    @SerialName("comment_count") val commentCount: Int,
    @SerialName("is_pinned") val isPinned: Boolean,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("created_at") val createdAt: OffsetDateTime,
)

@Serializable
data class CommunityPostListItem(
    val id: String,
    val title: String,
    val author: AuthorResponse,
    @SerialName("view_count") val viewCount: Int,
    @SerialName("comment_count") val commentCount: Int,
    @SerialName("is_pinned") val isPinned: Boolean,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("created_at") val createdAt: OffsetDateTime,
)

@Serializable
data class CommunityCommentCreateRequest(
    val content: String,
)

@Serializable
data class CommunityCommentResponse(
    val id: String,
    val content: String,
    val author: AuthorResponse,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("created_at") val createdAt: OffsetDateTime,
)
