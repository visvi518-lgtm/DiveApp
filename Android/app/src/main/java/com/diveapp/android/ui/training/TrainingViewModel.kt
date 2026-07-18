package com.diveapp.android.ui.training

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diveapp.android.model.TrainingRecordCreateRequest
import com.diveapp.android.repository.TrainingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TrainingStep { SETUP, RUNNING, RESULT }
enum class TrainingPhase { REST, HOLD }

/** CO2 Table training: the timer itself runs entirely on the client
 * (Docs/02_Requirements.md 3.3) — the server only stores the outcome. */
class TrainingViewModel(private val repository: TrainingRepository) : ViewModel() {
    // Program setup (Docs defaults: 8 sets, 2:00 rest, 1:00 hold, -15s/+10s per set)
    var totalSets by mutableStateOf(8)
    var restTimeSeconds by mutableStateOf(120)
    var holdTimeSeconds by mutableStateOf(60)
    var restIntervalSeconds by mutableStateOf(-15)
    var holdIntervalSeconds by mutableStateOf(10)

    var step by mutableStateOf(TrainingStep.SETUP)
        private set
    var currentSetIndex by mutableStateOf(0)
        private set
    var currentPhase by mutableStateOf(TrainingPhase.REST)
        private set
    var remainingSeconds by mutableStateOf(0)
        private set
    var isPaused by mutableStateOf(false)
        private set
    var completedSets by mutableStateOf(0)
        private set
    var isCompleted by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set
    var saveErrorMessage by mutableStateOf<String?>(null)
        private set
    var saveCompleted by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    val isSetupValid: Boolean
        get() = totalSets in 5..20 && restTimeSeconds > 0 && holdTimeSeconds > 0

    fun start() {
        if (!isSetupValid) return
        step = TrainingStep.RUNNING
        currentSetIndex = 0
        completedSets = 0
        isPaused = false
        beginPhase(TrainingPhase.REST)
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun stopEarly() {
        timerJob?.cancel()
        finish(completed = false)
    }

    fun restartSetup() {
        timerJob?.cancel()
        step = TrainingStep.SETUP
        saveCompleted = false
        saveErrorMessage = null
    }

    fun saveResult() {
        if (isSaving) return
        saveErrorMessage = null
        isSaving = true
        viewModelScope.launch {
            try {
                repository.create(
                    TrainingRecordCreateRequest(
                        totalSets = totalSets,
                        completedSets = completedSets,
                        isCompleted = isCompleted,
                        restTimeSeconds = restTimeSeconds,
                        holdTimeSeconds = holdTimeSeconds,
                        restIntervalSeconds = restIntervalSeconds,
                        holdIntervalSeconds = holdIntervalSeconds,
                    ),
                )
                saveCompleted = true
            } catch (e: Exception) {
                saveErrorMessage = e.message ?: "훈련 기록 저장에 실패했습니다."
            } finally {
                isSaving = false
            }
        }
    }

    private fun restSecondsFor(setIndex: Int) = (restTimeSeconds + setIndex * restIntervalSeconds).coerceAtLeast(5)

    private fun holdSecondsFor(setIndex: Int) = (holdTimeSeconds + setIndex * holdIntervalSeconds).coerceAtLeast(5)

    private fun beginPhase(phase: TrainingPhase) {
        currentPhase = phase
        remainingSeconds = if (phase == TrainingPhase.REST) restSecondsFor(currentSetIndex) else holdSecondsFor(currentSetIndex)
        runTimer()
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000)
                if (isPaused) continue
                remainingSeconds -= 1
            }
            onPhaseFinished()
        }
    }

    private fun onPhaseFinished() {
        if (currentPhase == TrainingPhase.REST) {
            beginPhase(TrainingPhase.HOLD)
            return
        }

        completedSets = currentSetIndex + 1
        if (currentSetIndex + 1 >= totalSets) {
            finish(completed = true)
        } else {
            currentSetIndex += 1
            beginPhase(TrainingPhase.REST)
        }
    }

    private fun finish(completed: Boolean) {
        timerJob?.cancel()
        isCompleted = completed
        step = TrainingStep.RESULT
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
