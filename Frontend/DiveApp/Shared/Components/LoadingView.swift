import SwiftUI

/// Shared loading indicator so every screen renders the same waiting state.
struct LoadingView: View {
    var message: String? = nil

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            ProgressView()
            if let message {
                Text(message)
                    .font(AppTypography.caption)
                    .foregroundStyle(AppColor.secondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
