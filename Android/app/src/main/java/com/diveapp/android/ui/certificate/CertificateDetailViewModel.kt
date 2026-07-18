package com.diveapp.android.ui.certificate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.CertificateResponse
import com.diveapp.android.repository.CertificateRepository
import kotlinx.coroutines.launch

sealed interface CertificateDetailUiState {
    data object Loading : CertificateDetailUiState
    data class Success(val certificate: CertificateResponse) : CertificateDetailUiState
    data class Error(val message: String) : CertificateDetailUiState
}

class CertificateDetailViewModel(
    private val repository: CertificateRepository,
    private val certificateId: String,
) : ViewModel() {
    var uiState by mutableStateOf<CertificateDetailUiState>(CertificateDetailUiState.Loading)
        private set

    var isDeleted by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        uiState = CertificateDetailUiState.Loading
        viewModelScope.launch {
            uiState = try {
                CertificateDetailUiState.Success(repository.get(certificateId))
            } catch (e: Exception) {
                CertificateDetailUiState.Error(e.message ?: "자격증 정보를 불러오지 못했습니다.")
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            try {
                repository.delete(certificateId)
                isDeleted = true
            } catch (e: Exception) {
                uiState = CertificateDetailUiState.Error(e.message ?: "삭제에 실패했습니다.")
            }
        }
    }
}
