package com.diveapp.android.ui.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.InformationRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.RemoteImage
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationDetailScreen(repository: InformationRepository, articleId: String, onBack: () -> Unit) {
    val viewModel: InformationDetailViewModel = viewModel(
        factory = ViewModelFactory { InformationDetailViewModel(repository, articleId) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("정보 게시글") },
                navigationIcon = { TextButton(onClick = onBack) { Text("닫기") } },
            )
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is InformationDetailUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is InformationDetailUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is InformationDetailUiState.Success -> {
                val article = state.article
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    RemoteImage(url = article.thumbnailImageUrl, modifier = Modifier.fillMaxWidth().height(200.dp))
                    Column(modifier = Modifier.padding(AppSpacing.lg)) {
                        Text(article.title, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            article.content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = AppSpacing.lg),
                        )
                    }
                }
            }
        }
    }
}
