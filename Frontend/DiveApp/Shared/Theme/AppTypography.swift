import SwiftUI

/// Maps Docs/08_DesignSystem.md's typography roles to system text styles so
/// Dynamic Type is supported for free.
enum AppTypography {
    static let largeTitle = Font.largeTitle.weight(.bold)
    static let title = Font.title.weight(.semibold)
    static let headline = Font.headline
    static let body = Font.body
    static let caption = Font.caption
}
