import Foundation

@MainActor
final class MyPageViewModel: ObservableObject {
    @Published private(set) var email: String?
    @Published private(set) var nickname: String?
    @Published var isLoggingOut = false

    private let authSession: AuthSession

    init(authSession: AuthSession) {
        self.authSession = authSession
        email = authSession.currentUser?.email
        nickname = authSession.currentUser?.profile?.nickname
    }

    func logout() async {
        isLoggingOut = true
        defer { isLoggingOut = false }
        await authSession.logout()
    }
}
