package com.diveapp.android.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.theme.AppColor
import com.diveapp.android.ui.theme.AppSpacing

@Composable
fun TrainingResultScreen(viewModel: TrainingViewModel, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            if (viewModel.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (viewModel.isCompleted) AppColor.success else AppColor.warning,
        )
        Text(
            if (viewModel.isCompleted) "훈련을 완료했습니다!" else "훈련이 중단되었습니다",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = AppSpacing.md),
        )
        Text(
            "완료 세트: ${viewModel.completedSets} / ${viewModel.totalSets}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )

        viewModel.saveErrorMessage?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = AppSpacing.md),
            )
        }

        if (viewModel.saveCompleted) {
            Text(
                "기록이 저장되었습니다.",
                color = AppColor.success,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = AppSpacing.md),
            )
            PrimaryButton(text = "확인", onClick = onDone, modifier = Modifier.padding(top = AppSpacing.xl))
        } else {
            PrimaryButton(
                text = "기록 저장",
                onClick = viewModel::saveResult,
                enabled = !viewModel.isSaving,
                modifier = Modifier.padding(top = AppSpacing.xl),
            )
        }
    }
}
