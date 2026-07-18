package com.diveapp.android.service

import com.diveapp.android.model.CommunityCommentCreateRequest
import com.diveapp.android.model.CommunityCommentResponse
import com.diveapp.android.model.CommunityPostCreateRequest
import com.diveapp.android.model.CommunityPostListItem
import com.diveapp.android.model.CommunityPostResponse
import com.diveapp.android.model.CommunityPostUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityService {
    @POST("api/v1/community/posts")
    suspend fun createPost(@Body body: CommunityPostCreateRequest): CommunityPostResponse

    @GET("api/v1/community/posts")
    suspend fun listPosts(@Query("q") query: String? = null): List<CommunityPostListItem>

    @GET("api/v1/community/posts/mine")
    suspend fun listMyPosts(): List<CommunityPostListItem>

    @GET("api/v1/community/posts/{id}")
    suspend fun getPost(@Path("id") id: String): CommunityPostResponse

    @PATCH("api/v1/community/posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body body: CommunityPostUpdateRequest): CommunityPostResponse

    @DELETE("api/v1/community/posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>

    @POST("api/v1/community/posts/{id}/comments")
    suspend fun createComment(@Path("id") postId: String, @Body body: CommunityCommentCreateRequest): CommunityCommentResponse

    @GET("api/v1/community/posts/{id}/comments")
    suspend fun listComments(@Path("id") postId: String): List<CommunityCommentResponse>

    @DELETE("api/v1/community/comments/{id}")
    suspend fun deleteComment(@Path("id") id: String): Response<Unit>
}
