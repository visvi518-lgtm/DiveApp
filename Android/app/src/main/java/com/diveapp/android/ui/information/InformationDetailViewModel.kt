package com.diveapp.android.ui.information

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.InformationArticleResponse
import com.diveapp.android.repository.InformationRepository
import kotlinx.coroutines.launch

sealed interface InformationDetailUiState {
    data object Loading : InformationDetailUiState
    data class Success(val article: InformationArticleResponse) : InformationDetailUiState
    data class Error(val message: String) : InformationDetailUiState
}

class InformationDetailViewModel(
    private val repository: InformationRepository,
    private val articleId: String,
) : ViewModel() {
    var uiState by mutableStateOf<InformationDetailUiState>(InformationDetailUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = InformationDetailUiState.Loading
        viewModelScope.launch {
            uiState = try {
                InformationDetailUiState.Success(repository.get(articleId))
            } catch (e: Exception) {
                InformationDetailUiState.Error(e.message ?: "게시글을 불러오지 못했습니다.")
            }
        }
    }
}
