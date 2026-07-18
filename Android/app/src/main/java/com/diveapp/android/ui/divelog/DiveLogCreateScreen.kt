package com.diveapp.android.ui.divelog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.model.DiveType
import com.diveapp.android.repository.DiveLogRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppSpacing
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveLogCreateScreen(repository: DiveLogRepository, onSaved: () -> Unit, onBack: () -> Unit) {
    val viewModel: DiveLogCreateViewModel = viewModel(factory = ViewModelFactory { DiveLogCreateViewModel(repository) })

    LaunchedEffect(viewModel.saveCompleted) {
        if (viewModel.saveCompleted) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("다이브 로그 작성") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.lg)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("종류", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.selectableGroup().padding(top = AppSpacing.xs)) {
                DiveTypeOption("프리다이빙", viewModel.diveType == DiveType.FREEDIVING) { viewModel.diveType = DiveType.FREEDIVING }
                DiveTypeOption("스쿠버다이빙", viewModel.diveType == DiveType.SCUBA) { viewModel.diveType = DiveType.SCUBA }
            }

            var showDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = viewModel.diveDate.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("날짜") },
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("선택") } },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )
            if (showDatePicker) {
                val state = rememberDatePickerState(
                    initialSelectedDateMillis = viewModel.diveDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            state.selectedDateMillis?.let {
                                viewModel.diveDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            }
                            showDatePicker = false
                        }) { Text("확인") }
                    },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } },
                ) {
                    DatePicker(state = state)
                }
            }

            OutlinedTextField(
                value = viewModel.locationName,
                onValueChange = { viewModel.locationName = it },
                label = { Text("다이빙 장소") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )
            Row(modifier = Modifier.padding(top = AppSpacing.md)) {
                OutlinedTextField(
                    value = viewModel.latitude,
                    onValueChange = { viewModel.latitude = it },
                    label = { Text("위도") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = viewModel.longitude,
                    onValueChange = { viewModel.longitude = it },
                    label = { Text("경도") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).padding(start = AppSpacing.md),
                )
            }
            OutlinedTextField(
                value = viewModel.city,
                onValueChange = { viewModel.city = it },
                label = { Text("도시 (선택)") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

            if (viewModel.diveType == DiveType.FREEDIVING) {
                OutlinedTextField(
                    value = viewModel.freedivingMaxDepth,
                    onValueChange = { viewModel.freedivingMaxDepth = it },
                    label = { Text("최대 수심 (m)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                )
                OutlinedTextField(
                    value = viewModel.freedivingTimeSeconds,
                    onValueChange = { viewModel.freedivingTimeSeconds = it },
                    label = { Text("다이빙 시간 (초)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                )
            } else {
                OutlinedTextField(
                    value = viewModel.scubaMaxDepth,
                    onValueChange = { viewModel.scubaMaxDepth = it },
                    label = { Text("최대 수심 (m)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                )
                OutlinedTextField(
                    value = viewModel.scubaTimeSeconds,
                    onValueChange = { viewModel.scubaTimeSeconds = it },
                    label = { Text("다이빙 시간 (초)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                )
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

@Composable
private fun DiveTypeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = AppSpacing.lg)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = AppSpacing.xs))
    }
}
