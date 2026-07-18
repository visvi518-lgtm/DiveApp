package com.diveapp.android.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileSetupViewModel(
    private val authSession: AuthSession,
    private val userRepository: UserRepository,
) : ViewModel() {
    var nickname by mutableStateOf("")
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val isValid: Boolean
        get() = nickname.length in 2..30

    fun save() {
        if (!isValid) {
            errorMessage = "닉네임은 2자 이상 30자 이하로 입력해주세요."
            return
        }
        errorMessage = null
        isSaving = true
        viewModelScope.launch {
            try {
                userRepository.setupProfile(nickname, null, null)
                authSession.completeProfileSetup()
            } catch (e: Exception) {
                errorMessage = e.message ?: "프로필 저장에 실패했습니다."
            } finally {
                isSaving = false
            }
        }
    }
}
