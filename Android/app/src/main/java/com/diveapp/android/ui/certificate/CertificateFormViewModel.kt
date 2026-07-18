package com.diveapp.android.ui.certificate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.CertificateCreateRequest
import com.diveapp.android.model.CertificationOrganization
import com.diveapp.android.repository.CertificateRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Shared by both Add Certificate and Edit Certificate — [certificateId] is
 * null in create mode. */
class CertificateFormViewModel(
    private val repository: CertificateRepository,
    private val certificateId: String?,
) : ViewModel() {
    var organization by mutableStateOf(CertificationOrganization.PADI)
    var certificationLevel by mutableStateOf("")
    var certificationNumber by mutableStateOf("")
    var instructor by mutableStateOf("")
    var diveCenter by mutableStateOf("")
    var memo by mutableStateOf("")
    var issueDate by mutableStateOf<LocalDate?>(null)
    var expirationDate by mutableStateOf<LocalDate?>(null)

    var isLoading by mutableStateOf(certificateId != null)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var saveCompleted by mutableStateOf(false)
        private set

    val isEditMode: Boolean get() = certificateId != null

    init {
        certificateId?.let(::loadExisting)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            try {
                val certificate = repository.get(id)
                organization = certificate.organization
                certificationLevel = certificate.certificationLevel
                certificationNumber = certificate.certificationNumber.orEmpty()
                instructor = certificate.instructor.orEmpty()
                diveCenter = certificate.diveCenter.orEmpty()
                memo = certificate.memo.orEmpty()
                issueDate = certificate.issueDate
                expirationDate = certificate.expirationDate
            } catch (e: Exception) {
                errorMessage = e.message ?: "자격증 정보를 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    val isValid: Boolean
        get() = certificationLevel.isNotBlank()

    fun save() {
        if (!isValid) {
            errorMessage = "등급을 입력해주세요."
            return
        }
        errorMessage = null
        isSaving = true
        viewModelScope.launch {
            try {
                val body = CertificateCreateRequest(
                    organization = organization,
                    certificationLevel = certificationLevel,
                    certificationNumber = certificationNumber.ifBlank { null },
                    issueDate = issueDate,
                    expirationDate = expirationDate,
                    instructor = instructor.ifBlank { null },
                    diveCenter = diveCenter.ifBlank { null },
                    certificateImageUrl = null,
                    memo = memo.ifBlank { null },
                )
                if (certificateId != null) {
                    repository.update(certificateId, body)
                } else {
                    repository.create(body)
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
