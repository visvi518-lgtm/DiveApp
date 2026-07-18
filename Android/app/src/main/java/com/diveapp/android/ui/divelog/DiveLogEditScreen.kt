package com.diveapp.android.ui.divelog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.model.DiveType
import com.diveapp.android.repository.DiveLogRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveLogEditScreen(repository: DiveLogRepository, diveLogId: String, onSaved: () -> Unit, onBack: () -> Unit) {
    val viewModel: DiveLogEditViewModel = viewModel(
        factory = ViewModelFactory { DiveLogEditViewModel(repository, diveLogId) },
    )

    LaunchedEffect(viewModel.saveCompleted) {
        if (viewModel.saveCompleted) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("다이브 로그 수정") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { padding ->
        if (viewModel.isLoading) {
            LoadingView(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.lg)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = viewModel.maxDepth,
                onValueChange = { viewModel.maxDepth = it },
                label = { Text("최대 수심 (m)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.timeSeconds,
                onValueChange = { viewModel.timeSeconds = it },
                label = { Text("다이빙 시간 (초)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

            if (viewModel.diveType == DiveType.SCUBA) {
                Row(modifier = Modifier.padding(top = AppSpacing.md)) {
                    OutlinedTextField(
                        value = viewModel.tankPressureStart,
                        onValueChange = { viewModel.tankPressureStart = it },
                        label = { Text("시작 압력 (bar)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = viewModel.tankPressureEnd,
                        onValueChange = { viewModel.tankPressureEnd = it },
                        label = { Text("종료 압력 (bar)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(start = AppSpacing.md),
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.memo,
                onValueChange = { viewModel.memo = it },
                label = { Text("메모 (선택)") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
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
