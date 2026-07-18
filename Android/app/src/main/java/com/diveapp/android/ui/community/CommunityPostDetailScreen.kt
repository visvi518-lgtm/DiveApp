package com.diveapp.android.ui.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.CommunityRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.AppTextButton
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen(
    repository: CommunityRepository,
    postId: String,
    currentUserId: String?,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: CommunityPostDetailViewModel = viewModel(
        factory = ViewModelFactory { CommunityPostDetailViewModel(repository, postId) },
    )
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isPostDeleted) {
        if (viewModel.isPostDeleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글") },
                navigationIcon = { TextButton(onClick = onBack) { Text("닫기") } },
            )
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is CommunityPostDetailUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is CommunityPostDetailUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is CommunityPostDetailUiState.Success -> {
                val post = state.post
                val isAuthor = currentUserId != null && currentUserId == post.author.id

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(AppSpacing.lg)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(post.title, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${post.author.nickname ?: "알 수 없음"} · 조회 ${post.viewCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    )
                    Text(post.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = AppSpacing.lg))

                    if (isAuthor) {
                        Row(modifier = Modifier.padding(top = AppSpacing.md)) {
                            AppTextButton(text = "수정", onClick = onEditClick)
                            AppTextButton(
                                text = "삭제",
                                onClick = { showDeleteConfirm = true },
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.lg))

                    Text("댓글 ${viewModel.comments.size}", style = MaterialTheme.typography.titleMedium)
                    viewModel.comments.forEach { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    comment.author.nickname ?: "알 수 없음",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (currentUserId != null && currentUserId == comment.author.id) {
                                AppTextButton(
                                    text = "삭제",
                                    onClick = { viewModel.deleteComment(comment.id) },
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.padding(top = AppSpacing.lg)) {
                        OutlinedTextField(
                            value = viewModel.newComment,
                            onValueChange = { viewModel.newComment = it },
                            label = { Text("댓글 작성") },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = viewModel::postComment,
                            enabled = viewModel.newComment.isNotBlank() && !viewModel.isPostingComment,
                        ) { Text("등록") }
                    }

                    viewModel.actionErrorMessage?.let { message ->
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = AppSpacing.sm),
                        )
                    }

                    if (showDeleteConfirm) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("게시글을 삭제할까요?") },
                            text = { Text("삭제한 게시글은 복구할 수 없습니다.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteConfirm = false
                                    viewModel.deletePost()
                                }) { Text("삭제") }
                            },
                            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") } },
                        )
                    }
                }
            }
        }
    }
}
