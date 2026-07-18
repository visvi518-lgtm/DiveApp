import SwiftUI
import UIKit

/// Semantic color roles from Docs/08_DesignSystem.md. Real brand colors are
/// not yet defined ("실제 색상값은 추후 정의한다") — these map to system
/// colors as neutral placeholders that already adapt to light/dark mode.
enum AppColor {
    static let primary = Color.accentColor
    static let secondary = Color.secondary
    static let background = Color(.systemBackground)
    static let surface = Color(.secondarySystemBackground)
    static let error = Color.red
    static let warning = Color.orange
    static let success = Color.green
    static let information = Color.blue
}
