package com.diveapp.android.ui.divelog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.DiveLogListItem
import com.diveapp.android.model.DiveLogStatisticsResponse
import com.diveapp.android.repository.DiveLogRepository
import kotlinx.coroutines.launch

sealed interface DiveLogListUiState {
    data object Loading : DiveLogListUiState
    data class Success(val logs: List<DiveLogListItem>, val statistics: DiveLogStatisticsResponse) : DiveLogListUiState
    data class Error(val message: String) : DiveLogListUiState
}

class DiveLogListViewModel(private val repository: DiveLogRepository) : ViewModel() {
    var uiState by mutableStateOf<DiveLogListUiState>(DiveLogListUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = DiveLogListUiState.Loading
        viewModelScope.launch {
            uiState = try {
                DiveLogListUiState.Success(repository.list(), repository.statistics())
            } catch (e: Exception) {
                DiveLogListUiState.Error(e.message ?: "다이브 로그를 불러오지 못했습니다.")
            }
        }
    }
}
