import SwiftUI

struct CommunityMockView: View {
    @EnvironmentObject private var appState: MockAppState

    var body: some View {
        NavigationStack {
            List {
                Section {
                    EmptyStateView(
                        systemImage: "person.3",
                        title: "커뮤니티 Mock",
                        message: "게시글 작성, 댓글 저장, 서버 동기화는 제외하고 목록 UI만 확인합니다."
                    )
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
                }

                Section("인기 글") {
                    ForEach(appState.posts) { post in
                        NavigationLink {
                            CommunityDetailMockView(post: post)
                        } label: {
                            VStack(alignment: .leading, spacing: AppSpacing.xs) {
                                Text(post.title)
                                    .font(AppTypography.headline)
                                Text("@\(post.author) · 댓글 \(post.comments)")
                                    .font(AppTypography.caption)
                                    .foregroundStyle(AppColor.secondary)
                            }
                            .padding(.vertical, AppSpacing.xs)
                        }
                    }
                }
            }
            .navigationTitle("커뮤니티")
        }
    }
}

struct CommunityDetailMockView: View {
    var post: MockPost

    var body: some View {
        List {
            Section {
                Text(post.title)
                    .font(AppTypography.title)
                Text("작성자 @\(post.author)")
                    .font(AppTypography.caption)
                    .foregroundStyle(AppColor.secondary)
            }

            Section("댓글") {
                ForEach(0..<post.comments, id: \.self) { index in
                    Text("Mock 댓글 \(index + 1)")
                }
            }
        }
        .navigationTitle("게시글")
    }
}
