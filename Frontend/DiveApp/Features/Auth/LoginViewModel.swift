import Foundation

@MainActor
final class LoginViewModel: ObservableObject {
    @Published var isLoggingIn = false
    @Published var errorMessage: String?

    private let authSession: AuthSession
    private let naverProvider: SocialAuthProviding
    private let googleProvider: SocialAuthProviding

    init(
        authSession: AuthSession,
        naverProvider: SocialAuthProviding = NaverSocialAuthProvider(),
        googleProvider: SocialAuthProviding = GoogleSocialAuthProvider()
    ) {
        self.authSession = authSession
        self.naverProvider = naverProvider
        self.googleProvider = googleProvider
    }

    func loginWithNaver() async {
        await login(using: naverProvider)
    }

    func loginWithGoogle() async {
        await login(using: googleProvider)
    }

    private func login(using provider: SocialAuthProviding) async {
        errorMessage = nil
        isLoggingIn = true
        defer { isLoggingIn = false }

        do {
            let providerToken = try await provider.signIn()
            try await authSession.login(provider: provider.provider, providerToken: providerToken)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
