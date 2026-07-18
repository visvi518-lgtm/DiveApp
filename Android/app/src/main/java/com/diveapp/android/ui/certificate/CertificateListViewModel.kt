package com.diveapp.android.ui.certificate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.CertificateResponse
import com.diveapp.android.repository.CertificateRepository
import kotlinx.coroutines.launch

sealed interface CertificateListUiState {
    data object Loading : CertificateListUiState
    data class Success(val certificates: List<CertificateResponse>) : CertificateListUiState
    data class Error(val message: String) : CertificateListUiState
}

class CertificateListViewModel(private val repository: CertificateRepository) : ViewModel() {
    var uiState by mutableStateOf<CertificateListUiState>(CertificateListUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = CertificateListUiState.Loading
        viewModelScope.launch {
            uiState = try {
                CertificateListUiState.Success(repository.list())
            } catch (e: Exception) {
                CertificateListUiState.Error(e.message ?: "자격증을 불러오지 못했습니다.")
            }
        }
    }
}
