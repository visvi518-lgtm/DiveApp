package com.diveapp.android.ui.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.CommunityRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.EmptyStateView
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostListScreen(
    repository: CommunityRepository,
    onAddClick: () -> Unit,
    onPostClick: (String) -> Unit,
) {
    val viewModel: CommunityPostListViewModel = viewModel(factory = ViewModelFactory { CommunityPostListViewModel(repository) })

    Scaffold(
        topBar = { TopAppBar(title = { Text("커뮤니티") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "글쓰기")
            }
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is CommunityPostListUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is CommunityPostListUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is CommunityPostListUiState.Success -> {
                if (state.posts.isEmpty()) {
                    EmptyStateView(
                        title = "등록된 게시글이 없습니다",
                        message = "오른쪽 아래 + 버튼으로 첫 글을 남겨보세요.",
                        icon = Icons.Filled.Forum,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onPostClick(post.id) },
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            post.author.nickname ?: "알 수 없음",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            "조회 ${post.viewCount} · 댓글 ${post.commentCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}
