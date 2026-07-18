import Foundation

@MainActor
final class SplashViewModel: ObservableObject {
    private let authSession: AuthSession

    init(authSession: AuthSession) {
        self.authSession = authSession
    }

    func start() async {
        await authSession.bootstrap()
    }
}
