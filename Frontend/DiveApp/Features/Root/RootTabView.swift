import SwiftUI

/// Main tab structure (Docs/03_UserFlow.md Home / Dive Log / CO2 Table /
/// Community / My Page). Only Home and My Page have real content in Phase 3;
/// the rest are placeholders until Phase 4 implements each feature's screens.
struct RootTabView: View {
    @EnvironmentObject private var authSession: AuthSession

    var body: some View {
        TabView {
            HomeView(authSession: authSession)
                .tabItem { Label("홈", systemImage: "house") }

            DiveLogPlaceholderView()
                .tabItem { Label("다이브 로그", systemImage: "book.closed") }

            TrainingPlaceholderView()
                .tabItem { Label("CO₂ Table", systemImage: "timer") }

            CommunityPlaceholderView()
                .tabItem { Label("커뮤니티", systemImage: "person.3") }

            MyPageView(authSession: authSession)
                .tabItem { Label("마이페이지", systemImage: "person.crop.circle") }
        }
    }
}
