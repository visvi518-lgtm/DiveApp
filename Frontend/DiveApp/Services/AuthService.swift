import Foundation

final class AuthService {
    private let client: APIClient

    init(client: APIClient) {
        self.client = client
    }

    func login(provider: AuthProvider, token: String) async throws -> TokenResponse {
        let endpoint = Endpoint(
            path: "/api/v1/auth/login/\(provider.rawValue)",
            method: .post,
            body: SocialLoginRequest(token: token),
            requiresAuth: false
        )
        return try await client.send(endpoint)
    }

    func refresh(refreshToken: String) async throws -> AccessTokenResponse {
        let endpoint = Endpoint(
            path: "/api/v1/auth/refresh",
            method: .post,
            body: RefreshTokenRequest(refreshToken: refreshToken),
            requiresAuth: false
        )
        return try await client.send(endpoint)
    }

    func logout() async throws {
        let endpoint = Endpoint(path: "/api/v1/auth/logout", method: .post)
        try await client.send(endpoint)
    }
}
