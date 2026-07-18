package com.diveapp.android.ui.training

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.TrainingRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.EmptyStateView
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.theme.AppSpacing
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingHistoryScreen(repository: TrainingRepository, onBack: () -> Unit) {
    val viewModel: TrainingHistoryViewModel = viewModel(factory = ViewModelFactory { TrainingHistoryViewModel(repository) })
    val formatter = remember(::createFormatter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("훈련 기록") },
                navigationIcon = { TextButton(onClick = onBack) { Text("닫기") } },
            )
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is TrainingHistoryUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is TrainingHistoryUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is TrainingHistoryUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val stats = state.statistics
                    Card(modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg)) {
                        Column(modifier = Modifier.padding(AppSpacing.lg)) {
                            Text("통계", style = MaterialTheme.typography.titleMedium)
                            Row(modifier = Modifier.padding(top = AppSpacing.sm), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("총 훈련 횟수: ${stats.totalTrainingCount}")
                            }
                            Text("완료율: ${(stats.completionRate * 100).toInt()}%")
                            Text("평균 완료 세트: ${"%.1f".format(stats.averageCompletedSets)}")
                        }
                    }

                    if (state.records.isEmpty()) {
                        EmptyStateView(title = "훈련 기록이 없습니다", icon = Icons.Filled.History)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            items(state.records, key = { it.id }) { record ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(record.completedAt.format(formatter))
                                        Text(
                                            "${record.completedSets}/${record.totalSets} 세트" +
                                                if (record.isCompleted) " · 완료" else " · 중단",
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

private fun createFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
