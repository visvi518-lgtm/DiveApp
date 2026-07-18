import SwiftUI

/// Switches between Splash / Login / Profile Setup / the main tab flow based
/// on AuthSession.state (Docs/03_UserFlow.md Authentication flow).
struct AppRootView: View {
    @ObservedObject var authSession: AuthSession
    let userRepository: UserRepository

    var body: some View {
        Group {
            switch authSession.state {
            case .bootstrapping:
                SplashView(authSession: authSession)
            case .unauthenticated:
                LoginView(authSession: authSession)
            case .needsProfileSetup:
                ProfileSetupView(authSession: authSession, userRepository: userRepository)
            case .authenticated:
                RootTabView()
                    .environmentObject(authSession)
            }
        }
        .animation(.default, value: authSession.state)
    }
}
