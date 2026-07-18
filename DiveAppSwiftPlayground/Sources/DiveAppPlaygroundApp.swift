import SwiftUI

@main
struct DiveAppPlaygroundApp: App {
    @StateObject private var appState = MockAppState()

    var body: some Scene {
        WindowGroup {
            PlaygroundRootView()
                .environmentObject(appState)
        }
    }
}

struct PlaygroundRootView: View {
    @EnvironmentObject private var appState: MockAppState

    var body: some View {
        Group {
            switch appState.sessionState {
            case .signedOut:
                LoginMockView()
            case .needsProfileSetup:
                ProfileSetupMockView()
            case .signedIn:
                MainTabMockView()
            }
        }
        .animation(.default, value: appState.sessionState)
    }
}
