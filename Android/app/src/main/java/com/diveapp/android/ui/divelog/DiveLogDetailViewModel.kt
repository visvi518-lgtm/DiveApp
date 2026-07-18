package com.diveapp.android.ui.divelog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.DiveLogResponse
import com.diveapp.android.repository.DiveLogRepository
import kotlinx.coroutines.launch

sealed interface DiveLogDetailUiState {
    data object Loading : DiveLogDetailUiState
    data class Success(val diveLog: DiveLogResponse) : DiveLogDetailUiState
    data class Error(val message: String) : DiveLogDetailUiState
}

class DiveLogDetailViewModel(
    private val repository: DiveLogRepository,
    private val diveLogId: String,
) : ViewModel() {
    var uiState by mutableStateOf<DiveLogDetailUiState>(DiveLogDetailUiState.Loading)
        private set
    var isDeleted by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        uiState = DiveLogDetailUiState.Loading
        viewModelScope.launch {
            uiState = try {
                DiveLogDetailUiState.Success(repository.get(diveLogId))
            } catch (e: Exception) {
                DiveLogDetailUiState.Error(e.message ?: "다이브 로그를 불러오지 못했습니다.")
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            try {
                repository.delete(diveLogId)
                isDeleted = true
            } catch (e: Exception) {
                uiState = DiveLogDetailUiState.Error(e.message ?: "삭제에 실패했습니다.")
            }
        }
    }
}
