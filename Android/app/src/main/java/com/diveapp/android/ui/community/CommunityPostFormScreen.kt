package com.diveapp.android.ui.community

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.CommunityRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostFormScreen(
    repository: CommunityRepository,
    postId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: CommunityPostFormViewModel = viewModel(
        factory = ViewModelFactory { CommunityPostFormViewModel(repository, postId) },
    )

    LaunchedEffect(viewModel.saveCompleted) {
        if (viewModel.saveCompleted) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "게시글 수정" else "글쓰기") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { padding ->
        if (viewModel.isLoading) {
            LoadingView(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.lg)) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.content,
                onValueChange = { viewModel.content = it },
                label = { Text("내용") },
                modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = AppSpacing.md),
            )

            viewModel.errorMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = AppSpacing.md),
                )
            }

            PrimaryButton(
                text = "저장",
                onClick = viewModel::save,
                enabled = viewModel.isValid && !viewModel.isSaving,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}
