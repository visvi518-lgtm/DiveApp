package com.diveapp.android.ui.information

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.InformationArticleListItem
import com.diveapp.android.repository.InformationRepository
import kotlinx.coroutines.launch

sealed interface InformationListUiState {
    data object Loading : InformationListUiState
    data class Success(val articles: List<InformationArticleListItem>) : InformationListUiState
    data class Error(val message: String) : InformationListUiState
}

class InformationListViewModel(private val repository: InformationRepository) : ViewModel() {
    var uiState by mutableStateOf<InformationListUiState>(InformationListUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = InformationListUiState.Loading
        viewModelScope.launch {
            uiState = try {
                InformationListUiState.Success(repository.list())
            } catch (e: Exception) {
                InformationListUiState.Error(e.message ?: "정보를 불러오지 못했습니다.")
            }
        }
    }
}
