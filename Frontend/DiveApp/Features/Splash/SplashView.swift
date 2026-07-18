import SwiftUI

/// First screen shown while AuthSession checks for a stored session
/// (Docs/03_UserFlow.md: Splash -> Auto Login Check -> Home / Login).
struct SplashView: View {
    @StateObject private var viewModel: SplashViewModel

    init(authSession: AuthSession) {
        _viewModel = StateObject(wrappedValue: SplashViewModel(authSession: authSession))
    }

    var body: some View {
        VStack(spacing: AppSpacing.lg) {
            Image(systemName: "water.waves")
                .font(.system(size: 56))
                .foregroundStyle(AppColor.primary)
            Text("DiveApp")
                .font(AppTypography.largeTitle)
            ProgressView()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppColor.background)
        .task {
            await viewModel.start()
        }
    }
}
