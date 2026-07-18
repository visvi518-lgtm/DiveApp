package com.diveapp.android.ui.divelog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.model.DiveType
import com.diveapp.android.repository.DiveLogRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.DestructiveButton
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.SecondaryButton
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveLogDetailScreen(
    repository: DiveLogRepository,
    diveLogId: String,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: DiveLogDetailViewModel = viewModel(
        factory = ViewModelFactory { DiveLogDetailViewModel(repository, diveLogId) },
    )
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isDeleted) {
        if (viewModel.isDeleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("다이브 로그 상세") },
                navigationIcon = { TextButton(onClick = onBack) { Text("닫기") } },
            )
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is DiveLogDetailUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is DiveLogDetailUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is DiveLogDetailUiState.Success -> {
                val log = state.diveLog
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(AppSpacing.lg)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        if (log.diveType == DiveType.FREEDIVING) "프리다이빙" else "스쿠버다이빙",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text("${log.diveDate} · ${log.location.name}", style = MaterialTheme.typography.bodyLarge)

                    log.freediving?.let {
                        DetailRow("최대 수심", "${it.maxDepth}m")
                        DetailRow("다이빙 시간", "${it.diveTimeSeconds}초")
                    }
                    log.scuba?.let {
                        DetailRow("최대 수심", "${it.maxDepth}m")
                        DetailRow("다이빙 시간", "${it.diveTimeSeconds}초")
                        DetailRow("탱크 압력", "${it.tankPressureStart} → ${it.tankPressureEnd} bar")
                    }
                    log.memo?.takeIf { it.isNotBlank() }?.let { DetailRow("메모", it) }

                    SecondaryButton(text = "수정", onClick = onEditClick, modifier = Modifier.padding(top = AppSpacing.xl))
                    DestructiveButton(
                        text = "삭제",
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.padding(top = AppSpacing.md),
                    )

                    if (showDeleteConfirm) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("다이브 로그를 삭제할까요?") },
                            text = { Text("삭제한 로그는 복구할 수 없습니다.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteConfirm = false
                                    viewModel.delete()
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

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(top = AppSpacing.md)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
