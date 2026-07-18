package com.diveapp.android.ui.auth

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.core.auth.SocialAuthProviding
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authSession: AuthSession,
    private val naverProvider: SocialAuthProviding,
    private val googleProvider: SocialAuthProviding,
) : ViewModel() {
    var isLoggingIn by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loginWithNaver(activity: Activity) = login(naverProvider, activity)

    fun loginWithGoogle(activity: Activity) = login(googleProvider, activity)

    private fun login(provider: SocialAuthProviding, activity: Activity) {
        errorMessage = null
        isLoggingIn = true
        viewModelScope.launch {
            try {
                val providerToken = provider.signIn(activity)
                authSession.login(provider.provider, providerToken)
            } catch (e: Exception) {
                errorMessage = e.message ?: "로그인에 실패했습니다."
            } finally {
                isLoggingIn = false
            }
        }
    }
}
