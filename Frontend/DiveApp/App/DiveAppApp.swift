import SwiftUI

@main
struct DiveAppApp: App {
    @StateObject private var authSession: AuthSession
    private let userRepository: UserRepository

    init() {
        let apiClient = APIClient()

        let authService = AuthService(client: apiClient)
        let userService = UserService(client: apiClient)

        let tokenStorage = TokenStorage()
        let authRepository = AuthRepository(authService: authService, tokenStorage: tokenStorage)
        let userRepository = UserRepository(userService: userService)

        let authSession = AuthSession(authRepository: authRepository, userRepository: userRepository)
        apiClient.tokenProvider = authSession

        _authSession = StateObject(wrappedValue: authSession)
        self.userRepository = userRepository
    }

    var body: some Scene {
        WindowGroup {
            AppRootView(authSession: authSession, userRepository: userRepository)
        }
    }
}
