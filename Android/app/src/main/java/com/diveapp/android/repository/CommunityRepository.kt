package com.diveapp.android.repository

import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.CommunityCommentCreateRequest
import com.diveapp.android.model.CommunityCommentResponse
import com.diveapp.android.model.CommunityPostCreateRequest
import com.diveapp.android.model.CommunityPostListItem
import com.diveapp.android.model.CommunityPostResponse
import com.diveapp.android.model.CommunityPostUpdateRequest
import com.diveapp.android.service.CommunityService

class CommunityRepository(private val service: CommunityService) {
    suspend fun createPost(title: String, content: String): CommunityPostResponse =
        apiCall { service.createPost(CommunityPostCreateRequest(title, content)) }

    suspend fun listPosts(query: String? = null): List<CommunityPostListItem> = apiCall { service.listPosts(query) }

    suspend fun listMyPosts(): List<CommunityPostListItem> = apiCall { service.listMyPosts() }

    suspend fun getPost(id: String): CommunityPostResponse = apiCall { service.getPost(id) }

    suspend fun updatePost(id: String, title: String, content: String): CommunityPostResponse =
        apiCall { service.updatePost(id, CommunityPostUpdateRequest(title, content)) }

    suspend fun deletePost(id: String) {
        apiCall { service.deletePost(id) }
    }

    suspend fun createComment(postId: String, content: String): CommunityCommentResponse =
        apiCall { service.createComment(postId, CommunityCommentCreateRequest(content)) }

    suspend fun listComments(postId: String): List<CommunityCommentResponse> = apiCall { service.listComments(postId) }

    suspend fun deleteComment(id: String) {
        apiCall { service.deleteComment(id) }
    }
}
