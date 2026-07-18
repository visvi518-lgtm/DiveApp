package com.diveapp.android.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.EmptyStateView
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainScreen(authSession: AuthSession, onArticlesClick: () -> Unit) {
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactory { HomeViewModel(authSession) })
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("홈") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val nickname = currentUser?.profile?.nickname
            Text(
                if (nickname != null) "${nickname}님, 안녕하세요!" else "안녕하세요!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(AppSpacing.lg),
            )

            EmptyStateView(
                title = "배너",
                message = "배너 노출 기능은 추후 제공될 예정입니다.",
                modifier = Modifier.height(160.dp).fillMaxWidth().padding(horizontal = AppSpacing.lg),
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.lg)
                    .clickable(onClick = onArticlesClick),
            ) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    Text("정보 게시판", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "프리다이빙 · 스쿠버다이빙 관련 소식을 확인해보세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    )
                }
            }
        }
    }
}
