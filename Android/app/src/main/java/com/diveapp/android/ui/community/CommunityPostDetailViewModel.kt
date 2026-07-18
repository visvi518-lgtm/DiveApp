package com.diveapp.android.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.CommunityCommentResponse
import com.diveapp.android.model.CommunityPostResponse
import com.diveapp.android.repository.CommunityRepository
import kotlinx.coroutines.launch

sealed interface CommunityPostDetailUiState {
    data object Loading : CommunityPostDetailUiState
    data class Success(val post: CommunityPostResponse) : CommunityPostDetailUiState
    data class Error(val message: String) : CommunityPostDetailUiState
}

class CommunityPostDetailViewModel(
    private val repository: CommunityRepository,
    private val postId: String,
) : ViewModel() {
    var uiState by mutableStateOf<CommunityPostDetailUiState>(CommunityPostDetailUiState.Loading)
        private set
    var comments by mutableStateOf<List<CommunityCommentResponse>>(emptyList())
        private set
    var newComment by mutableStateOf("")
    var isPostingComment by mutableStateOf(false)
        private set
    var isPostDeleted by mutableStateOf(false)
        private set
    var actionErrorMessage by mutableStateOf<String?>(null)
        private set

    init {
        load()
    }

    fun load() {
        uiState = CommunityPostDetailUiState.Loading
        viewModelScope.launch {
            try {
                val post = repository.getPost(postId)
                val postComments = repository.listComments(postId)
                uiState = CommunityPostDetailUiState.Success(post)
                comments = postComments
            } catch (e: Exception) {
                uiState = CommunityPostDetailUiState.Error(e.message ?: "게시글을 불러오지 못했습니다.")
            }
        }
    }

    fun postComment() {
        if (newComment.isBlank() || isPostingComment) return
        isPostingComment = true
        viewModelScope.launch {
            try {
                repository.createComment(postId, newComment)
                newComment = ""
                comments = repository.listComments(postId)
            } catch (e: Exception) {
                actionErrorMessage = e.message ?: "댓글 작성에 실패했습니다."
            } finally {
                isPostingComment = false
            }
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                isPostDeleted = true
            } catch (e: Exception) {
                actionErrorMessage = e.message ?: "삭제에 실패했습니다."
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                repository.deleteComment(commentId)
                comments = repository.listComments(postId)
            } catch (e: Exception) {
                actionErrorMessage = e.message ?: "댓글 삭제에 실패했습니다."
            }
        }
    }
}
