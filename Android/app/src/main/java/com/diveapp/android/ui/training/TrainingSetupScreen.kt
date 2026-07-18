package com.diveapp.android.ui.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSetupScreen(viewModel: TrainingViewModel, onHistoryClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CO₂ Table") },
                actions = { TextButton(onClick = onHistoryClick) { Text("기록") } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.lg)) {
            Text("세트 수: ${viewModel.totalSets}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = viewModel.totalSets.toFloat(),
                onValueChange = { viewModel.totalSets = it.toInt() },
                valueRange = 5f..20f,
                steps = 14,
            )

            OutlinedTextField(
                value = viewModel.restTimeSeconds.toString(),
                onValueChange = { viewModel.restTimeSeconds = it.toIntOrNull() ?: 0 },
                label = { Text("휴식 시간 (초)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.lg),
            )
            OutlinedTextField(
                value = viewModel.holdTimeSeconds.toString(),
                onValueChange = { viewModel.holdTimeSeconds = it.toIntOrNull() ?: 0 },
                label = { Text("숨참기 시간 (초)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

            Row(modifier = Modifier.padding(top = AppSpacing.md)) {
                OutlinedTextField(
                    value = viewModel.restIntervalSeconds.toString(),
                    onValueChange = { viewModel.restIntervalSeconds = it.toIntOrNull() ?: 0 },
                    label = { Text("세트당 휴식 변화(초)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = viewModel.holdIntervalSeconds.toString(),
                    onValueChange = { viewModel.holdIntervalSeconds = it.toIntOrNull() ?: 0 },
                    label = { Text("세트당 숨참기 변화(초)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(start = AppSpacing.md),
                )
            }

            if (!viewModel.isSetupValid) {
                Text(
                    "세트 수는 5~20, 휴식/숨참기 시간은 0보다 커야 합니다.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = AppSpacing.md),
                )
            }

            PrimaryButton(
                text = "훈련 시작",
                onClick = viewModel::start,
                enabled = viewModel.isSetupValid,
                modifier = Modifier.padding(top = AppSpacing.xl),
            )
        }
    }
}
