package com.diveapp.android.ui.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.repository.TrainingRepository
import com.diveapp.android.ui.ViewModelFactory

/** Top-level CO2 Table tab: Setup -> Running -> Result flow, plus a History
 * screen reachable from Setup (Docs/03_UserFlow.md CO2 Table flow). */
@Composable
fun TrainingScreen(repository: TrainingRepository) {
    val viewModel: TrainingViewModel = viewModel(factory = ViewModelFactory { TrainingViewModel(repository) })
    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        TrainingHistoryScreen(repository = repository, onBack = { showHistory = false })
        return
    }

    when (viewModel.step) {
        TrainingStep.SETUP -> TrainingSetupScreen(viewModel, onHistoryClick = { showHistory = true })
        TrainingStep.RUNNING -> TrainingRunningScreen(viewModel)
        TrainingStep.RESULT -> TrainingResultScreen(viewModel, onDone = viewModel::restartSetup)
    }
}
