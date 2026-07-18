import SwiftUI

struct MainTabMockView: View {
    var body: some View {
        TabView {
            HomeMockView()
                .tabItem { Label("홈", systemImage: "house") }

            DiveLogMockView()
                .tabItem { Label("로그", systemImage: "book.closed") }

            TrainingMockView()
                .tabItem { Label("훈련", systemImage: "timer") }

            CommunityMockView()
                .tabItem { Label("커뮤니티", systemImage: "person.3") }

            MyPageMockView()
                .tabItem { Label("마이", systemImage: "person.crop.circle") }
        }
    }
}
