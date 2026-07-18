package com.diveapp.android.ui.divelog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.DiveLocationInput
import com.diveapp.android.model.DiveLogCreateRequest
import com.diveapp.android.model.DiveType
import com.diveapp.android.model.FreedivingDetailInput
import com.diveapp.android.model.ScubaDetailInput
import com.diveapp.android.repository.DiveLogRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class DiveLogCreateViewModel(private val repository: DiveLogRepository) : ViewModel() {
    var diveType by mutableStateOf(DiveType.FREEDIVING)
    var diveDate by mutableStateOf(LocalDate.now())
    var locationName by mutableStateOf("")
    var city by mutableStateOf("")
    var country by mutableStateOf("")
    var latitude by mutableStateOf("")
    var longitude by mutableStateOf("")
    var memo by mutableStateOf("")

    // Freediving fields
    var freedivingMaxDepth by mutableStateOf("")
    var freedivingTimeSeconds by mutableStateOf("")

    // Scuba fields
    var scubaMaxDepth by mutableStateOf("")
    var scubaTimeSeconds by mutableStateOf("")
    var tankPressureStart by mutableStateOf("")
    var tankPressureEnd by mutableStateOf("")

    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var saveCompleted by mutableStateOf(false)
        private set

    val isValid: Boolean
        get() {
            val hasLocation = locationName.isNotBlank() && latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null
            val hasDetail = if (diveType == DiveType.FREEDIVING) {
                freedivingMaxDepth.toDoubleOrNull() != null && freedivingTimeSeconds.toIntOrNull() != null
            } else {
                scubaMaxDepth.toDoubleOrNull() != null &&
                    scubaTimeSeconds.toIntOrNull() != null &&
                    tankPressureStart.toIntOrNull() != null &&
                    tankPressureEnd.toIntOrNull() != null
            }
            return hasLocation && hasDetail
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
                val body = DiveLogCreateRequest(
                    diveType = diveType,
                    diveDate = diveDate,
                    location = DiveLocationInput(
                        name = locationName,
                        latitude = latitude.toDouble(),
                        longitude = longitude.toDouble(),
                        country = country.ifBlank { null },
                        city = city.ifBlank { null },
                    ),
                    memo = memo.ifBlank { null },
                    freediving = if (diveType == DiveType.FREEDIVING) {
                        FreedivingDetailInput(freedivingMaxDepth.toDouble(), freedivingTimeSeconds.toInt())
                    } else {
                        null
                    },
                    scuba = if (diveType == DiveType.SCUBA) {
                        ScubaDetailInput(
                            maxDepth = scubaMaxDepth.toDouble(),
                            diveTimeSeconds = scubaTimeSeconds.toInt(),
                            tankPressureStart = tankPressureStart.toInt(),
                            tankPressureEnd = tankPressureEnd.toInt(),
                        )
                    } else {
                        null
                    },
                )
                repository.create(body)
                saveCompleted = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "저장에 실패했습니다."
            } finally {
                isSaving = false
            }
        }
    }
}
