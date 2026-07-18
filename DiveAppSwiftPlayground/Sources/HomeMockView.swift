import SwiftUI

struct HomeMockView: View {
    @EnvironmentObject private var appState: MockAppState

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: AppSpacing.lg) {
                    VStack(alignment: .leading, spacing: AppSpacing.xs) {
                        Text("\(appState.currentUser.nickname)님, 안녕하세요")
                            .font(AppTypography.title)
                        Text("오늘도 안전한 다이빙을 준비해볼까요?")
                            .font(AppTypography.body)
                            .foregroundStyle(AppColor.secondary)
                    }

                    HStack(spacing: AppSpacing.md) {
                        MetricChip(title: "총 로그", value: "\(appState.diveLogs.count)", color: AppColor.primary)
                        MetricChip(title: "누적 다이브", value: "\(appState.currentUser.totalDives)", color: AppColor.ocean)
                    }

                    ForEach(MockBanner.samples) { banner in
                        BannerCard(banner: banner)
                    }

                    EmptyStateView(
                        systemImage: "calendar.badge.clock",
                        title: "다가오는 일정",
                        message: "실제 캘린더 연동은 제외하고 UI 상태만 표시합니다."
                    )
                }
                .padding(AppSpacing.lg)
            }
            .navigationTitle("홈")
        }
    }
}
