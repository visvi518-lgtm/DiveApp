package com.diveapp.android.ui.mypage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.core.auth.AuthSession
import kotlinx.coroutines.launch

class MyPageViewModel(private val authSession: AuthSession) : ViewModel() {
    val currentUser = authSession.currentUser

    var isLoggingOut by mutableStateOf(false)
        private set

    fun logout() {
        isLoggingOut = true
        viewModelScope.launch {
            try {
                authSession.logout()
            } finally {
                isLoggingOut = false
            }
        }
    }
}
