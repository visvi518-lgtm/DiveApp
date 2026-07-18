package com.diveapp.android.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.diveapp.android.ui.components.DestructiveButton
import com.diveapp.android.ui.components.SecondaryButton
import com.diveapp.android.ui.theme.AppSpacing

@Composable
fun TrainingRunningScreen(viewModel: TrainingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "SET ${viewModel.currentSetIndex + 1} / ${viewModel.totalSets}",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            if (viewModel.currentPhase == TrainingPhase.REST) "휴식" else "숨참기",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = AppSpacing.md),
        )

        Text(
            formatSeconds(viewModel.remainingSeconds),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = AppSpacing.lg),
        )

        LinearProgressIndicator(
            progress = { (viewModel.currentSetIndex + 1).toFloat() / viewModel.totalSets },
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xl),
        )

        SecondaryButton(
            text = if (viewModel.isPaused) "재개" else "일시정지",
            onClick = viewModel::togglePause,
            modifier = Modifier.padding(top = AppSpacing.xxl),
        )
        DestructiveButton(
            text = "종료",
            onClick = viewModel::stopEarly,
            modifier = Modifier.padding(top = AppSpacing.md),
        )
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
