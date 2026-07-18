import SwiftUI

/// Button styles from Docs/08_DesignSystem.md: Primary / Secondary / Destructive / Text.
/// Apply with `.buttonStyle(.diveAppPrimary)` etc.

struct PrimaryButtonStyle: ButtonStyle {
    var isDisabled: Bool = false

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppSpacing.md)
            .background(isDisabled ? AppColor.primary.opacity(0.4) : AppColor.primary)
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

struct DestructiveButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppSpacing.md)
            .background(AppColor.error)
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: AppCornerRadius.medium))
            .opacity(configuration.isPressed ? 0.8 : 1)
    }
}

struct TextOnlyButtonStyle: ButtonStyle {
    var tint: Color = AppColor.primary

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.body)
            .foregroundStyle(tint)
            .opacity(configuration.isPressed ? 0.6 : 1)
    }
}

extension ButtonStyle where Self == PrimaryButtonStyle {
    static var diveAppPrimary: PrimaryButtonStyle { PrimaryButtonStyle() }
}

extension ButtonStyle where Self == SecondaryButtonStyle {
    static var diveAppSecondary: SecondaryButtonStyle { SecondaryButtonStyle() }
}

extension ButtonStyle where Self == DestructiveButtonStyle {
    static var diveAppDestructive: DestructiveButtonStyle { DestructiveButtonStyle() }
}

extension ButtonStyle where Self == TextOnlyButtonStyle {
    static var diveAppText: TextOnlyButtonStyle { TextOnlyButtonStyle() }

    static func diveAppText(tint: Color) -> TextOnlyButtonStyle {
        TextOnlyButtonStyle(tint: tint)
    }
}
