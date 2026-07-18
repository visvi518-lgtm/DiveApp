package com.diveapp.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.core.auth.GoogleSocialAuthProvider
import com.diveapp.android.core.auth.NaverSocialAuthProvider
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.components.SecondaryButton
import com.diveapp.android.ui.theme.AppSpacing

@Composable
fun LoginScreen(
    authSession: AuthSession,
    naverProvider: NaverSocialAuthProvider,
    googleProvider: GoogleSocialAuthProvider,
) {
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory { LoginViewModel(authSession, naverProvider, googleProvider) },
    )
    val activity = LocalContext.current as Activity

    Column(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.padding(top = AppSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("DiveApp", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = AppSpacing.sm))
            Text(
                "다이빙 기록, 훈련, 커뮤니티를 한 곳에서",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            viewModel.errorMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = AppSpacing.md),
                )
            }

            PrimaryButton(
                text = "네이버로 시작하기",
                onClick = { viewModel.loginWithNaver(activity) },
                enabled = !viewModel.isLoggingIn,
            )
            SecondaryButton(
                text = "구글로 시작하기",
                onClick = { viewModel.loginWithGoogle(activity) },
                enabled = !viewModel.isLoggingIn,
                modifier = Modifier.padding(top = AppSpacing.md),
            )
        }
    }
}
