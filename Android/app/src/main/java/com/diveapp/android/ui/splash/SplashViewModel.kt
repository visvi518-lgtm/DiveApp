package com.diveapp.android.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.core.auth.AuthSession
import kotlinx.coroutines.launch

class SplashViewModel(private val authSession: AuthSession) : ViewModel() {
    fun start() {
        viewModelScope.launch {
            authSession.bootstrap()
        }
    }
}
