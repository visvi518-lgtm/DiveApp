package com.diveapp.android.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.repository.CommunityRepository
import kotlinx.coroutines.launch

/** Shared by Write Post and Edit Post — [postId] is null in create mode. */
class CommunityPostFormViewModel(
    private val repository: CommunityRepository,
    private val postId: String?,
) : ViewModel() {
    var title by mutableStateOf("")
    var content by mutableStateOf("")

    var isLoading by mutableStateOf(postId != null)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var saveCompleted by mutableStateOf(false)
        private set

    val isEditMode: Boolean get() = postId != null

    init {
        postId?.let { id ->
            viewModelScope.launch {
                try {
                    val post = repository.getPost(id)
                    title = post.title
                    content = post.content
                } catch (e: Exception) {
                    errorMessage = e.message ?: "게시글을 불러오지 못했습니다."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val isValid: Boolean
        get() = title.isNotBlank() && content.isNotBlank()

    fun save() {
        if (!isValid) {
            errorMessage = "제목과 내용을 입력해주세요."
            return
        }
        errorMessage = null
        isSaving = true
        viewModelScope.launch {
            try {
                if (postId != null) {
                    repository.updatePost(postId, title, content)
                } else {
                    repository.createPost(title, content)
                }
                saveCompleted = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "저장에 실패했습니다."
            } finally {
                isSaving = false
            }
        }
    }
}
