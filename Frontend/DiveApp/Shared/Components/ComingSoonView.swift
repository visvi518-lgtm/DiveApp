import SwiftUI

/// Placeholder for tabs whose real screens land in Phase 4
/// (Docs/07_Screens.md), so the tab structure is testable end to end now.
struct ComingSoonView: View {
    let title: String
    var systemImage: String = "hammer"

    var body: some View {
        NavigationStack {
            EmptyStateView(systemImage: systemImage, title: title, message: "다음 단계에서 구현될 화면입니다.")
                .navigationTitle(title)
        }
    }
}
