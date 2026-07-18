package com.diveapp.android.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.theme.AppSpacing

@Composable
fun SplashScreen(authSession: AuthSession) {
    val viewModel: SplashViewModel = viewModel(factory = ViewModelFactory { SplashViewModel(authSession) })

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("DiveApp", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = AppSpacing.lg))
        CircularProgressIndicator(modifier = Modifier.padding(top = AppSpacing.lg))
    }
}
