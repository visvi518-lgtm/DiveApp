import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel: LoginViewModel

    init(authSession: AuthSession) {
        _viewModel = StateObject(wrappedValue: LoginViewModel(authSession: authSession))
    }

    var body: some View {
        VStack(spacing: AppSpacing.xxl) {
            Spacer()

            VStack(spacing: AppSpacing.sm) {
                Image(systemName: "water.waves")
                    .font(.system(size: 48))
                    .foregroundStyle(AppColor.primary)
                Text("DiveApp")
                    .font(AppTypography.largeTitle)
                Text("다이빙 기록, 훈련, 커뮤니티를 한 곳에서")
                    .font(AppTypography.body)
                    .foregroundStyle(AppColor.secondary)
            }

            Spacer()

            VStack(spacing: AppSpacing.md) {
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .font(AppTypography.caption)
                        .foregroundStyle(AppColor.error)
                        .multilineTextAlignment(.center)
                }

                Button("네이버로 시작하기") {
                    Task { await viewModel.loginWithNaver() }
                }
                .buttonStyle(.diveAppPrimary)
                .disabled(viewModel.isLoggingIn)

                Button("구글로 시작하기") {
                    Task { await viewModel.loginWithGoogle() }
                }
                .buttonStyle(.diveAppSecondary)
                .disabled(viewModel.isLoggingIn)
            }
            .padding(.horizontal, AppSpacing.xl)
            .padding(.bottom, AppSpacing.xxl)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppColor.background)
        .overlay {
            if viewModel.isLoggingIn {
                LoadingView()
                    .background(.ultraThinMaterial)
            }
        }
    }
}
