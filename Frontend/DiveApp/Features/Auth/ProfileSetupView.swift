import SwiftUI

/// First-login profile setup (nickname is required; photo/phone are optional
/// and deferred to Phase 4's My Page screens).
struct ProfileSetupView: View {
    @StateObject private var viewModel: ProfileSetupViewModel
    @FocusState private var isNicknameFocused: Bool

    init(authSession: AuthSession, userRepository: UserRepository) {
        _viewModel = StateObject(
            wrappedValue: ProfileSetupViewModel(authSession: authSession, userRepository: userRepository)
        )
    }

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.lg) {
            VStack(alignment: .leading, spacing: AppSpacing.xs) {
                Text("프로필을 설정해주세요")
                    .font(AppTypography.title)
                Text("닉네임은 나중에 마이페이지에서 변경할 수 있어요.")
                    .font(AppTypography.body)
                    .foregroundStyle(AppColor.secondary)
            }

            TextField("닉네임 (2~30자)", text: $viewModel.nickname)
                .textFieldStyle(.roundedBorder)
                .focused($isNicknameFocused)

            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .font(AppTypography.caption)
                    .foregroundStyle(AppColor.error)
            }

            Spacer()

            Button("시작하기") {
                Task { await viewModel.save() }
            }
            .buttonStyle(PrimaryButtonStyle(isDisabled: !viewModel.isValid))
            .disabled(!viewModel.isValid || viewModel.isSaving)
        }
        .padding(AppSpacing.xl)
        .background(AppColor.background)
        .onAppear { isNicknameFocused = true }
        .overlay {
            if viewModel.isSaving {
                LoadingView()
                    .background(.ultraThinMaterial)
            }
        }
    }
}
