import SwiftUI

/// Shared empty state for list screens, per Docs/08_DesignSystem.md.
struct EmptyStateView: View {
    var systemImage: String = "tray"
    var title: String
    var message: String? = nil

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: systemImage)
                .font(.system(size: 40))
                .foregroundStyle(AppColor.secondary)
            Text(title)
                .font(AppTypography.headline)
            if let message {
                Text(message)
                    .font(AppTypography.body)
                    .foregroundStyle(AppColor.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(AppSpacing.xl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
