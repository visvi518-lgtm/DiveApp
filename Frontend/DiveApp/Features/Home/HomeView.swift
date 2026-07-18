import SwiftUI

struct HomeView: View {
    @StateObject private var viewModel: HomeViewModel

    init(authSession: AuthSession) {
        _viewModel = StateObject(wrappedValue: HomeViewModel(authSession: authSession))
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: AppSpacing.lg) {
                    Text(greeting)
                        .font(AppTypography.title)
                        .padding(.horizontal, AppSpacing.lg)

                    EmptyStateView(
                        systemImage: "megaphone",
                        title: "배너",
                        message: "Phase 4에서 배너/정보 게시글이 여기에 표시됩니다."
                    )
                    .frame(height: 220)
                }
                .padding(.top, AppSpacing.lg)
            }
            .navigationTitle("홈")
        }
    }

    private var greeting: String {
        if let nickname = viewModel.nickname {
            return "\(nickname)님, 안녕하세요!"
        }
        return "안녕하세요!"
    }
}
