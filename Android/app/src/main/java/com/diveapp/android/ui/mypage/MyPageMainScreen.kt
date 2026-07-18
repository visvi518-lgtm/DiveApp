package com.diveapp.android.ui.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.AppTextButton
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageMainScreen(authSession: AuthSession, onCertificatesClick: () -> Unit) {
    val viewModel: MyPageViewModel = viewModel(factory = ViewModelFactory { MyPageViewModel(authSession) })
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("마이페이지") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.lg)) {
            Text(currentUser?.profile?.nickname ?: "닉네임 없음", style = MaterialTheme.typography.titleMedium)
            currentUser?.email?.let { email ->
                Text(
                    email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.lg)
                    .clickable(onClick = onCertificatesClick),
            ) {
                Text(
                    "자격증 관리",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(AppSpacing.lg),
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.lg))

            AppTextButton(
                text = "로그아웃",
                onClick = viewModel::logout,
                enabled = !viewModel.isLoggingOut,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
