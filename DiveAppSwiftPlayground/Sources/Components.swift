import SwiftUI

struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppSpacing.md)
            .background(AppColor.primary)
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.medium))
            .opacity(configuration.isPressed ? 0.8 : 1)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppSpacing.md)
            .background(AppColor.surface)
            .foregroundStyle(AppColor.primary)
            .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.medium))
            .overlay(
                RoundedRectangle(cornerRadius: AppCornerRadius.medium)
                    .strokeBorder(AppColor.primary, lineWidth: 1)
            )
            .opacity(configuration.isPressed ? 0.8 : 1)
    }
}

extension ButtonStyle where Self == PrimaryButtonStyle {
    static var diveAppPrimary: PrimaryButtonStyle { PrimaryButtonStyle() }
}

extension ButtonStyle where Self == SecondaryButtonStyle {
    static var diveAppSecondary: SecondaryButtonStyle { SecondaryButtonStyle() }
}

struct EmptyStateView: View {
    var systemImage: String
    var title: String
    var message: String

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: systemImage)
                .font(.system(size: 38))
                .foregroundStyle(AppColor.secondary)
            Text(title)
                .font(AppTypography.headline)
            Text(message)
                .font(AppTypography.body)
                .foregroundStyle(AppColor.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(AppSpacing.xl)
        .frame(maxWidth: .infinity)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.medium))
    }
}

struct MetricChip: View {
    var title: String
    var value: String
    var color: Color = AppColor.primary

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.xs) {
            Text(title)
                .font(AppTypography.caption)
                .foregroundStyle(AppColor.secondary)
            Text(value)
                .font(AppTypography.headline)
                .foregroundStyle(color)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(AppSpacing.md)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.small))
    }
}

struct BannerCard: View {
    var banner: MockBanner

    var body: some View {
        HStack(spacing: AppSpacing.md) {
            Image(systemName: banner.systemImage)
                .font(.system(size: 34))
                .foregroundStyle(.white)
                .frame(width: 58, height: 58)
                .background(AppColor.ocean)
                .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.medium))

            VStack(alignment: .leading, spacing: AppSpacing.xs) {
                Text(banner.title)
                    .font(AppTypography.headline)
                Text(banner.subtitle)
                    .font(AppTypography.body)
                    .foregroundStyle(AppColor.secondary)
            }
            Spacer()
        }
        .padding(AppSpacing.lg)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.medium))
    }
}
