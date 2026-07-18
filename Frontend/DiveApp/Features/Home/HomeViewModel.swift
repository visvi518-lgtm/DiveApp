import Foundation

@MainActor
final class HomeViewModel: ObservableObject {
    @Published private(set) var nickname: String?

    private let authSession: AuthSession

    init(authSession: AuthSession) {
        self.authSession = authSession
        nickname = authSession.currentUser?.profile?.nickname
    }
}
