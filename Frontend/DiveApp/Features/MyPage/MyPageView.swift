import SwiftUI

/// Foundation-level My Page: identity summary + logout. Full profile editing
/// and certification list land in Phase 4 (Docs/07_Screens.md).
struct MyPageView: View {
    @StateObject private var viewModel: MyPageViewModel

    init(authSession: AuthSession) {
        _viewModel = StateObject(wrappedValue: MyPageViewModel(authSession: authSession))
    }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    VStack(alignment: .leading, spacing: AppSpacing.xs) {
                        Text(viewModel.nickname ?? "닉네임 없음")
                            .font(AppTypography.headline)
                        if let email = viewModel.email {
                            Text(email)
                                .font(AppTypography.caption)
                                .foregroundStyle(AppColor.secondary)
                        }
                    }
                    .padding(.vertical, AppSpacing.xs)
                }

                Section {
                    Button("로그아웃") {
                        Task { await viewModel.logout() }
                    }
                    .buttonStyle(.diveAppText(tint: AppColor.error))
                    .disabled(viewModel.isLoggingOut)
                }
            }
            .navigationTitle("마이페이지")
        }
    }
}
