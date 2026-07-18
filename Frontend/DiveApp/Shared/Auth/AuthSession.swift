import Foundation

enum AuthState: Equatable {
    case bootstrapping
    case unauthenticated
    case needsProfileSetup
    case authenticated
}

/// Single source of truth for auth state, observed by AppRootView to switch
/// between Splash / Login / Profile Setup / the main tab flow.
@MainActor
final class AuthSession: ObservableObject, AccessTokenProviding {
    @Published private(set) var state: AuthState = .bootstrapping
    @Published private(set) var currentUser: CurrentUser?

    private let authRepository: AuthRepository
    private let userRepository: UserRepository

    init(authRepository: AuthRepository, userRepository: UserRepository) {
        self.authRepository = authRepository
        self.userRepository = userRepository
    }

    func bootstrap() async {
        guard authRepository.hasStoredSession else {
            state = .unauthenticated
            return
        }
        do {
            _ = try await authRepository.refreshAccessToken()
            try await loadCurrentUser()
        } catch {
            await handleUnauthorized()
        }
    }

    func login(provider: AuthProvider, providerToken: String) async throws {
        let isNewUser = try await authRepository.login(provider: provider, providerToken: providerToken)
        if isNewUser {
            state = .needsProfileSetup
        } else {
            try await loadCurrentUser()
        }
    }

    func completeProfileSetup() async {
        do {
            try await loadCurrentUser()
        } catch {
            state = .needsProfileSetup
        }
    }

    func logout() async {
        await authRepository.logout()
        currentUser = nil
        state = .unauthenticated
    }

    private func loadCurrentUser() async throws {
        let user = try await userRepository.fetchCurrentUser()
        currentUser = user
        state = user.profile == nil ? .needsProfileSetup : .authenticated
    }

    // MARK: - AccessTokenProviding

    func currentAccessToken() -> String? {
        authRepository.currentAccessToken
    }

    func refreshAccessToken() async throws -> String {
        try await authRepository.refreshAccessToken()
    }

    func handleUnauthorized() async {
        await authRepository.logout()
        currentUser = nil
        state = .unauthenticated
    }
}
