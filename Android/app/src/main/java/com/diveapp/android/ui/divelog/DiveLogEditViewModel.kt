package com.diveapp.android.ui.divelog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.DiveLogUpdateRequest
import com.diveapp.android.model.DiveType
import com.diveapp.android.model.FreedivingDetailInput
import com.diveapp.android.model.ScubaDetailInput
import com.diveapp.android.repository.DiveLogRepository
import kotlinx.coroutines.launch

/** Mirrors the backend's update capability: only memo and type-specific
 * numbers can change — dive_type/date/location are fixed after creation. */
class DiveLogEditViewModel(
    private val repository: DiveLogRepository,
    private val diveLogId: String,
) : ViewModel() {
    var diveType by mutableStateOf(DiveType.FREEDIVING)
        private set
    var memo by mutableStateOf("")
    var maxDepth by mutableStateOf("")
    var timeSeconds by mutableStateOf("")
    var tankPressureStart by mutableStateOf("")
    var tankPressureEnd by mutableStateOf("")

    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var saveCompleted by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            try {
                val log = repository.get(diveLogId)
                diveType = log.diveType
                memo = log.memo.orEmpty()
                log.freediving?.let {
                    maxDepth = it.maxDepth.toString()
                    timeSeconds = it.diveTimeSeconds.toString()
                }
                log.scuba?.let {
                    maxDepth = it.maxDepth.toString()
                    timeSeconds = it.diveTimeSeconds.toString()
                    tankPressureStart = it.tankPressureStart.toString()
                    tankPressureEnd = it.tankPressureEnd.toString()
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "다이브 로그를 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    val isValid: Boolean
        get() {
            val depthOk = maxDepth.toDoubleOrNull() != null
            val timeOk = timeSeconds.toIntOrNull() != null
            return if (diveType == DiveType.FREEDIVING) {
                depthOk && timeOk
            } else {
                depthOk && timeOk && tankPressureStart.toIntOrNull() != null && tankPressureEnd.toIntOrNull() != null
            }
        }

    fun save() {
        if (!isValid) {
            errorMessage = "필수 항목을 모두 입력해주세요."
            return
        }
        errorMessage = null
        isSaving = true
        viewModelScope.launch {
            try {
                val body = DiveLogUpdateRequest(
                    memo = memo.ifBlank { null },
                    freediving = if (diveType == DiveType.FREEDIVING) {
                        FreedivingDetailInput(maxDepth.toDouble(), timeSeconds.toInt())
                    } else {
                        null
                    },
                    scuba = if (diveType == DiveType.SCUBA) {
                        ScubaDetailInput(maxDepth.toDouble(), timeSeconds.toInt(), tankPressureStart.toInt(), tankPressureEnd.toInt())
                    } else {
                        null
                    },
                )
                repository.update(diveLogId, body)
                saveCompleted = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "저장에 실패했습니다."
            } finally {
                isSaving = false
            }
        }
    }
}
