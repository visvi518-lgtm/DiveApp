package com.diveapp.android.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.CommunityPostListItem
import com.diveapp.android.repository.CommunityRepository
import kotlinx.coroutines.launch

sealed interface CommunityPostListUiState {
    data object Loading : CommunityPostListUiState
    data class Success(val posts: List<CommunityPostListItem>) : CommunityPostListUiState
    data class Error(val message: String) : CommunityPostListUiState
}

class CommunityPostListViewModel(private val repository: CommunityRepository) : ViewModel() {
    var uiState by mutableStateOf<CommunityPostListUiState>(CommunityPostListUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = CommunityPostListUiState.Loading
        viewModelScope.launch {
            uiState = try {
                CommunityPostListUiState.Success(repository.listPosts())
            } catch (e: Exception) {
                CommunityPostListUiState.Error(e.message ?: "게시글을 불러오지 못했습니다.")
            }
        }
    }
}
