import SwiftUI

enum AppColor {
    static let primary = Color(red: 0.05, green: 0.42, blue: 0.74)
    static let ocean = Color(red: 0.03, green: 0.58, blue: 0.65)
    static let coral = Color(red: 0.93, green: 0.36, blue: 0.30)
    static let kelp = Color(red: 0.12, green: 0.45, blue: 0.32)
    static let background = Color(.systemBackground)
    static let surface = Color(.secondarySystemBackground)
    static let secondary = Color.secondary
    static let error = Color.red
}

enum AppTypography {
    static let largeTitle = Font.largeTitle.weight(.bold)
    static let title = Font.title2.weight(.semibold)
    static let headline = Font.headline
    static let body = Font.body
    static let caption = Font.caption
}

enum AppSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 24
    static let xxl: CGFloat = 32
}

enum AppCornerRadius {
    static let small: CGFloat = 8
    static let medium: CGFloat = 12
    static let large: CGFloat = 20
}
