package com.diveapp.android.ui.certificate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.CertificateRepository
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.EmptyStateView
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.LoadingView
import com.diveapp.android.ui.theme.AppSpacing
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateListScreen(
    repository: CertificateRepository,
    onAddClick: () -> Unit,
    onCertificateClick: (String) -> Unit,
) {
    val viewModel: CertificateListViewModel = viewModel(
        factory = ViewModelFactory { CertificateListViewModel(repository) },
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("자격증 관리") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "자격증 추가")
            }
        },
    ) { padding ->
        when (val state = viewModel.uiState) {
            is CertificateListUiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is CertificateListUiState.Error -> ErrorStateView(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is CertificateListUiState.Success -> {
                if (state.certificates.isEmpty()) {
                    EmptyStateView(
                        title = "등록된 자격증이 없습니다",
                        message = "오른쪽 아래 + 버튼으로 자격증을 추가해보세요.",
                        icon = Icons.Filled.WorkspacePremium,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        items(state.certificates, key = { it.id }) { certificate ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onCertificateClick(certificate.id) },
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                                    Text(
                                        "${certificate.organization.name} · ${certificate.certificationLevel}",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    certificate.diveCenter?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = AppSpacing.xs),
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
