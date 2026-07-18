import SwiftUI

struct ProfileSetupMockView: View {
    @EnvironmentObject private var appState: MockAppState
    @State private var nickname = ""
    @State private var level = "Open Water"

    private let levels = [
        "Open Water",
        "Advanced Open Water",
        "Rescue Diver",
        "Freediver"
    ]

    var body: some View {
        NavigationStack {
            Form {
                Section("기본 정보") {
                    TextField("닉네임", text: $nickname)
                    Picker("레벨", selection: $level) {
                        ForEach(levels, id: \.self) { level in
                            Text(level)
                        }
                    }
                }

                Section {
                    Button("프로필 완료") {
                        appState.completeProfile(nickname: nickname, level: level)
                    }
                }
            }
            .navigationTitle("프로필 설정")
        }
    }
}
