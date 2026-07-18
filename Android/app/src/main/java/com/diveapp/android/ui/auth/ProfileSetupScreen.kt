package com.diveapp.android.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.repository.UserRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppSpacing

@Composable
fun ProfileSetupScreen(authSession: AuthSession, userRepository: UserRepository) {
    val viewModel: ProfileSetupViewModel = viewModel(
        factory = ViewModelFactory { ProfileSetupViewModel(authSession, userRepository) },
    )

    Column(modifier = Modifier.fillMaxSize().padding(AppSpacing.xl)) {
        Text("프로필을 설정해주세요", style = MaterialTheme.typography.headlineMedium)
        Text(
            "닉네임은 나중에 마이페이지에서 변경할 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.xs, bottom = AppSpacing.lg),
        )

        OutlinedTextField(
            value = viewModel.nickname,
            onValueChange = { viewModel.nickname = it },
            label = { Text("닉네임 (2~30자)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        viewModel.errorMessage?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "시작하기",
            onClick = viewModel::save,
            enabled = viewModel.isValid && !viewModel.isSaving,
        )
    }
}
