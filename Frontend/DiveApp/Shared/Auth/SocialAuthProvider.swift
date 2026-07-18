import Foundation

enum SocialAuthError: Error, LocalizedError {
    case sdkNotConfigured(AuthProvider)
    case cancelled

    var errorDescription: String? {
        switch self {
        case .sdkNotConfigured(let provider):
            return "\(provider.rawValue) 로그인 SDK가 아직 연결되지 않았습니다."
        case .cancelled:
            return "로그인이 취소되었습니다."
        }
    }
}

/// Wraps a provider's native login SDK and returns the raw token the backend
/// verifies (Naver access token / Google ID token).
@MainActor
protocol SocialAuthProviding {
    var provider: AuthProvider { get }
    func signIn() async throws -> String
}

/// Placeholder until the Naver login SDK is added and registered with a real
/// client ID/secret (see Report backlog). Throws so the login flow can surface
/// a clear message instead of silently failing.
@MainActor
final class NaverSocialAuthProvider: SocialAuthProviding {
    let provider: AuthProvider = .naver

    func signIn() async throws -> String {
        throw SocialAuthError.sdkNotConfigured(provider)
    }
}

/// Placeholder until GoogleSignIn-iOS is added via SPM and configured with a
/// real client ID (see Report backlog).
@MainActor
final class GoogleSocialAuthProvider: SocialAuthProviding {
    let provider: AuthProvider = .google

    func signIn() async throws -> String {
        throw SocialAuthError.sdkNotConfigured(provider)
    }
}
