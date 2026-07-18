import SwiftUI

struct MyPageMockView: View {
    @EnvironmentObject private var appState: MockAppState

    var body: some View {
        NavigationStack {
            List {
                Section {
                    VStack(alignment: .leading, spacing: AppSpacing.sm) {
                        HStack(spacing: AppSpacing.md) {
                            Image(systemName: "person.crop.circle.fill")
                                .font(.system(size: 48))
                                .foregroundStyle(AppColor.primary)

                            VStack(alignment: .leading, spacing: AppSpacing.xs) {
                                Text(appState.currentUser.nickname)
                                    .font(AppTypography.headline)
                                Text(appState.currentUser.email)
                                    .font(AppTypography.caption)
                                    .foregroundStyle(AppColor.secondary)
                            }
                        }

                        HStack(spacing: AppSpacing.md) {
                            MetricChip(title: "레벨", value: appState.currentUser.level, color: AppColor.ocean)
                            MetricChip(title: "누적", value: "\(appState.currentUser.totalDives)", color: AppColor.kelp)
                        }
                    }
                    .padding(.vertical, AppSpacing.sm)
                }

                Section("자격증") {
                    ForEach(appState.certificates) { certificate in
                        VStack(alignment: .leading, spacing: AppSpacing.xs) {
                            Text(certificate.name)
                                .font(AppTypography.headline)
                            Text("\(certificate.agency) · \(certificate.issuedAt)")
                                .font(AppTypography.caption)
                                .foregroundStyle(AppColor.secondary)
                        }
                    }
                }

                Section {
                    Button(role: .destructive) {
                        appState.logout()
                    } label: {
                        Text("로그아웃")
                    }
                }
            }
            .navigationTitle("마이페이지")
        }
    }
}
