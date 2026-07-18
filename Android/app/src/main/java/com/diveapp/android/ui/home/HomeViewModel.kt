package com.diveapp.android.ui.home

import androidx.lifecycle.ViewModel
import com.diveapp.android.core.auth.AuthSession

class HomeViewModel(authSession: AuthSession) : ViewModel() {
    val currentUser = authSession.currentUser
}
