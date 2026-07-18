package com.diveapp.android.ui.certificate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.model.CertificationOrganization
import com.diveapp.android.repository.CertificateRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateFormScreen(
    repository: CertificateRepository,
    certificateId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: CertificateFormViewModel = viewModel(
        factory = ViewModelFactory { CertificateFormViewModel(repository, certificateId) },
    )

    LaunchedEffect(viewModel.saveCompleted) {
        if (viewModel.saveCompleted) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "자격증 수정" else "자격증 추가") },
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
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = viewModel.organization.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("발급 기관") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    CertificationOrganization.entries.forEach { org ->
                        DropdownMenuItem(
                            text = { Text(org.name) },
                            onClick = {
                                viewModel.organization = org
                                expanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.certificationLevel,
                onValueChange = { viewModel.certificationLevel = it },
                label = { Text("등급 (예: Open Water Diver)") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

            OutlinedTextField(
                value = viewModel.certificationNumber,
                onValueChange = { viewModel.certificationNumber = it },
                label = { Text("자격증 번호 (선택)") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

            DateField(
                label = "발급일 (선택)",
                date = viewModel.issueDate,
                onDateSelected = { viewModel.issueDate = it },
                modifier = Modifier.padding(top = AppSpacing.md),
            )

            DateField(
                label = "만료일 (선택)",
                date = viewModel.expirationDate,
                onDateSelected = { viewModel.expirationDate = it },
                modifier = Modifier.padding(top = AppSpacing.md),
            )

            OutlinedTextField(
                value = viewModel.instructor,
                onValueChange = { viewModel.instructor = it },
                label = { Text("강사 (선택)") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

            OutlinedTextField(
                value = viewModel.diveCenter,
                onValueChange = { viewModel.diveCenter = it },
                label = { Text("다이브 센터 (선택)") },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
            )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.toString() ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            TextButton(onClick = { showDialog = true }) { Text("선택") }
        },
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    onDateSelected(millis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() })
                    showDialog = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("취소") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
