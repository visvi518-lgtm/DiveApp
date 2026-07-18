import SwiftUI
import Foundation

struct TrainingMockView: View {
    @EnvironmentObject private var appState: MockAppState
    @State private var isTimerRunning = false
    @State private var selectedRound = 1
    @State private var startDate: Date?
    @State private var accumulatedTime: TimeInterval = 0
    @State private var displayedTime: TimeInterval = 0

    private let timer = Timer.publish(every: 0.01, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack {
            List {
                Section {
                    VStack(spacing: AppSpacing.md) {
                        Image(systemName: "timer")
                            .font(.system(size: 44))
                            .foregroundStyle(AppColor.coral)

                        Text(isTimerRunning ? "CO2 Table 진행 중" : "CO2 Table 준비")
                            .font(AppTypography.title)

                        Text(formattedTime(displayedTime))
                            .font(.system(size: 48, weight: .bold, design: .monospaced))
                            .foregroundStyle(isTimerRunning ? AppColor.coral : AppColor.primary)
                            .frame(maxWidth: .infinity)
                            .contentTransition(.numericText())

                        Stepper("라운드 \(selectedRound)", value: $selectedRound, in: 1...8)

                        HStack(spacing: AppSpacing.md) {
                            Button(isTimerRunning ? "중지" : "시작") {
                                toggleTimer()
                            }
                            .buttonStyle(.diveAppPrimary)

                            Button("초기화") {
                                resetTimer()
                            }
                            .buttonStyle(.diveAppSecondary)
                        }
                    }
                    .padding(.vertical, AppSpacing.md)
                }

                Section("최근 훈련") {
                    ForEach(appState.trainingRecords) { record in
                        VStack(alignment: .leading, spacing: AppSpacing.xs) {
                            Text(record.title)
                                .font(AppTypography.headline)
                            Text(record.detail)
                                .font(AppTypography.caption)
                                .foregroundStyle(AppColor.secondary)
                            Text(record.result)
                                .font(AppTypography.caption)
                                .foregroundStyle(AppColor.kelp)
                        }
                        .padding(.vertical, AppSpacing.xs)
                    }
                }
            }
            .navigationTitle("훈련")
        }
        .onReceive(timer) { now in
            guard isTimerRunning, let startDate else { return }
            displayedTime = accumulatedTime + now.timeIntervalSince(startDate)
        }
    }

    private func toggleTimer() {
        if isTimerRunning {
            accumulatedTime = displayedTime
            startDate = nil
            isTimerRunning = false
        } else {
            startDate = Date()
            isTimerRunning = true
        }
    }

    private func resetTimer() {
        isTimerRunning = false
        startDate = nil
        accumulatedTime = 0
        displayedTime = 0
    }

    private func formattedTime(_ time: TimeInterval) -> String {
        let totalCentiseconds = Int((time * 100).rounded())
        let minutes = totalCentiseconds / 6000
        let seconds = (totalCentiseconds / 100) % 60
        let centiseconds = totalCentiseconds % 100
        return String(format: "%02d:%02d.%02d", minutes, seconds, centiseconds)
    }
}
