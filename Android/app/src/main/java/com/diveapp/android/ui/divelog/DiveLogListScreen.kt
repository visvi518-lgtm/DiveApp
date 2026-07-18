package com.diveapp.android.ui.divelog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.model.DiveType
import com.diveapp.android.repository.DiveLogRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.EmptyStateView
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.RemoteImage
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveLogListScreen(
    repository: DiveLogRepository,
    onAddClick: () -> Unit,
    onDiveLogClick: (String) -> Unit,
) {
    val viewModel: DiveLogListViewModel = viewModel(factory = ViewModelFactory { DiveLogListViewModel(repository) })

    Scaffold(
        topBar = { TopAppBar(title = { Text("다이브 로그") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "다이브 로그 작성")
            }
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is DiveLogListUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is DiveLogListUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is DiveLogListUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val stats = state.statistics
                    Card(modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg)) {
                        Column(modifier = Modifier.padding(AppSpacing.lg)) {
                            Text("총 ${stats.totalDiveCount}회 · 최대 수심 ${stats.maxDepthOverall}m", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "프리다이빙 ${stats.freedivingCount}회 · 스쿠버 ${stats.scubaCount}회",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = AppSpacing.xs),
                            )
                        }
                    }

                    if (state.logs.isEmpty()) {
                        EmptyStateView(
                            title = "등록된 다이브 로그가 없습니다",
                            message = "오른쪽 아래 + 버튼으로 첫 로그를 남겨보세요.",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        ) {
                            items(state.logs, key = { it.id }) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onDiveLogClick(log.id) },
                                ) {
                                    Row(modifier = Modifier.padding(AppSpacing.md)) {
                                        RemoteImage(
                                            url = log.coverImageUrl,
                                            modifier = Modifier.size(64.dp),
                                        )
                                        Column(modifier = Modifier.padding(start = AppSpacing.md)) {
                                            Text(
                                                if (log.diveType == DiveType.FREEDIVING) "프리다이빙" else "스쿠버다이빙",
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                            Text(log.locationName, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "${log.diveDate} · 최대 ${log.maxDepth}m",
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
}
