import Foundation

final class AuthRepository {
    private let authService: AuthService
    private let tokenStorage: TokenStorage

    init(authService: AuthService, tokenStorage: TokenStorage) {
        self.authService = authService
        self.tokenStorage = tokenStorage
    }

    /// Returns whether this is the user's first login (client should route to Profile Setup).
    @discardableResult
    func login(provider: AuthProvider, providerToken: String) async throws -> Bool {
        let tokens = try await authService.login(provider: provider, token: providerToken)
        tokenStorage.save(accessToken: tokens.accessToken, refreshToken: tokens.refreshToken)
        return tokens.isNewUser
    }

    func refreshAccessToken() async throws -> String {
        guard let refreshToken = tokenStorage.refreshToken else {
            throw APIError.unauthorized
        }
        let response = try await authService.refresh(refreshToken: refreshToken)
        tokenStorage.accessToken = response.accessToken
        return response.accessToken
    }

    func logout() async {
        try? await authService.logout()
        tokenStorage.clear()
    }

    var hasStoredSession: Bool {
        tokenStorage.refreshToken != nil
    }

    var currentAccessToken: String? {
        tokenStorage.accessToken
    }
}
