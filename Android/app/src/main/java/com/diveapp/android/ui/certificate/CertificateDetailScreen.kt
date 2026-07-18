package com.diveapp.android.ui.certificate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.diveapp.android.repository.CertificateRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.DestructiveButton
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.SecondaryButton
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateDetailScreen(
    repository: CertificateRepository,
    certificateId: String,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: CertificateDetailViewModel = viewModel(
        factory = ViewModelFactory { CertificateDetailViewModel(repository, certificateId) },
    )
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isDeleted) {
        if (viewModel.isDeleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("자격증 상세") },
                navigationIcon = { TextButton(onClick = onBack) { Text("닫기") } },
            )
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is CertificateDetailUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is CertificateDetailUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is CertificateDetailUiState.Success -> {
                val certificate = state.certificate
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.lg)) {
                    Text("${certificate.organization.name} · ${certificate.certificationLevel}", style = MaterialTheme.typography.headlineMedium)

                    DetailRow("자격증 번호", certificate.certificationNumber)
                    DetailRow("발급일", certificate.issueDate?.toString())
                    DetailRow("만료일", certificate.expirationDate?.toString())
                    DetailRow("강사", certificate.instructor)
                    DetailRow("다이브 센터", certificate.diveCenter)
                    DetailRow("메모", certificate.memo)

                    SecondaryButton(
                        text = "수정",
                        onClick = onEditClick,
                        modifier = Modifier.padding(top = AppSpacing.xl),
                    )
                    DestructiveButton(
                        text = "삭제",
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.padding(top = AppSpacing.md),
                    )

                    if (showDeleteConfirm) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("자격증을 삭제할까요?") },
                            text = { Text("삭제한 자격증은 복구할 수 없습니다.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteConfirm = false
                                    viewModel.delete()
                                }) { Text("삭제") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column(modifier = Modifier.padding(top = AppSpacing.md)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
