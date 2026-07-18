package com.diveapp.android.ui.information

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.InformationRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.EmptyStateView
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.RemoteImage
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationListScreen(repository: InformationRepository, onArticleClick: (String) -> Unit) {
    val viewModel: InformationListViewModel = viewModel(factory = ViewModelFactory { InformationListViewModel(repository) })

    Scaffold(topBar = { TopAppBar(title = { Text("정보 게시판") }) }) { padding ->
        when (val state = viewModel.uiState) {
            is InformationListUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is InformationListUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is InformationListUiState.Success -> {
                if (state.articles.isEmpty()) {
                    EmptyStateView(
                        title = "등록된 정보글이 없습니다",
                        icon = Icons.AutoMirrored.Filled.Article,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        items(state.articles, key = { it.id }) { article ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onArticleClick(article.id) },
                            ) {
                                Column {
                                    RemoteImage(url = article.thumbnailImageUrl, modifier = Modifier.fillMaxWidth().height(140.dp))
                                    Text(
                                        article.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(AppSpacing.lg),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
