package com.diveapp.android.ui.training

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.TrainingRecordResponse
import com.diveapp.android.model.TrainingStatisticsResponse
import com.diveapp.android.repository.TrainingRepository
import kotlinx.coroutines.launch

sealed interface TrainingHistoryUiState {
    data object Loading : TrainingHistoryUiState
    data class Success(
        val records: List<TrainingRecordResponse>,
        val statistics: TrainingStatisticsResponse,
    ) : TrainingHistoryUiState
    data class Error(val message: String) : TrainingHistoryUiState
}

class TrainingHistoryViewModel(private val repository: TrainingRepository) : ViewModel() {
    var uiState by mutableStateOf<TrainingHistoryUiState>(TrainingHistoryUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = TrainingHistoryUiState.Loading
        viewModelScope.launch {
            uiState = try {
                val records = repository.list()
                val stats = repository.statistics()
                TrainingHistoryUiState.Success(records, stats)
            } catch (e: Exception) {
                TrainingHistoryUiState.Error(e.message ?: "훈련 기록을 불러오지 못했습니다.")
            }
        }
    }
}
