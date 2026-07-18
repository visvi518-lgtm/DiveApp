import SwiftUI

enum MockSessionState: Equatable {
    case signedOut
    case needsProfileSetup
    case signedIn
}

@MainActor
final class MockAppState: ObservableObject {
    @Published var sessionState: MockSessionState = .signedOut
    @Published var currentUser = MockUser.guest
    @Published var diveLogs = MockDiveLog.samples
    @Published var posts = MockPost.samples
    @Published var certificates = MockCertificate.samples
    @Published var trainingRecords = MockTrainingRecord.samples

    func login() {
        currentUser = .sample
        sessionState = .signedIn
    }

    func loginAsNewUser() {
        currentUser = .guest
        sessionState = .needsProfileSetup
    }

    func completeProfile(nickname: String, level: String) {
        currentUser = MockUser(
            nickname: nickname.isEmpty ? "새 다이버" : nickname,
            email: "new.diver@example.com",
            level: level,
            totalDives: 0
        )
        sessionState = .signedIn
    }

    func logout() {
        currentUser = .guest
        sessionState = .signedOut
    }

    func addDiveLog(title: String, location: String) {
        let log = MockDiveLog(
            title: title.isEmpty ? "새 다이빙 로그" : title,
            location: location.isEmpty ? "장소 미정" : location,
            depth: "18m",
            duration: "36분",
            date: "오늘",
            type: "스쿠버"
        )
        diveLogs.insert(log, at: 0)
    }
}
