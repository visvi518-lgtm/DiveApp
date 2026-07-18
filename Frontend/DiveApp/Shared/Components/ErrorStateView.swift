import SwiftUI

/// Shared error state for failed network requests, per Docs/08_DesignSystem.md.
struct ErrorStateView: View {
    var message: String
    var retryTitle: String = "다시 시도"
    var onRetry: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 40))
                .foregroundStyle(AppColor.error)
            Text(message)
                .font(AppTypography.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(AppColor.secondary)
            if let onRetry {
                Button(retryTitle, action: onRetry)
                    .buttonStyle(.diveAppSecondary)
                    .padding(.horizontal, AppSpacing.xl)
            }
        }
        .padding(AppSpacing.xl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
