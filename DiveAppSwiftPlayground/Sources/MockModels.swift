import Foundation

struct MockUser: Identifiable, Equatable {
    let id = UUID()
    var nickname: String
    var email: String
    var level: String
    var totalDives: Int

    static let guest = MockUser(
        nickname: "게스트",
        email: "",
        level: "프로필 미설정",
        totalDives: 0
    )

    static let sample = MockUser(
        nickname: "Blue Diver",
        email: "blue.diver@example.com",
        level: "Advanced Open Water",
        totalDives: 42
    )
}

struct MockBanner: Identifiable {
    let id = UUID()
    var title: String
    var subtitle: String
    var systemImage: String

    static let samples = [
        MockBanner(title: "이번 주 추천 포인트", subtitle: "제주 문섬 수온 24도, 시야 양호", systemImage: "water.waves"),
        MockBanner(title: "장비 체크", subtitle: "입수 전 버디 체크를 잊지 마세요", systemImage: "checklist")
    ]
}

struct MockDiveLog: Identifiable {
    let id = UUID()
    var title: String
    var location: String
    var depth: String
    var duration: String
    var date: String
    var type: String

    static let samples = [
        MockDiveLog(title: "문섬 펀다이빙", location: "제주 서귀포", depth: "24m", duration: "42분", date: "7월 12일", type: "스쿠버"),
        MockDiveLog(title: "풀장 트레이닝", location: "잠실 다이빙풀", depth: "5m", duration: "60분", date: "7월 8일", type: "프리다이빙"),
        MockDiveLog(title: "야간 다이빙", location: "강릉 사천진", depth: "16m", duration: "35분", date: "6월 30일", type: "스쿠버")
    ]
}

struct MockTrainingRecord: Identifiable {
    let id = UUID()
    var title: String
    var detail: String
    var result: String

    static let samples = [
        MockTrainingRecord(title: "CO2 Table", detail: "8 rounds, breath hold 1:30", result: "완료"),
        MockTrainingRecord(title: "Static Apnea", detail: "Best 2:40", result: "기록 갱신")
    ]
}

struct MockPost: Identifiable {
    let id = UUID()
    var title: String
    var author: String
    var comments: Int

    static let samples = [
        MockPost(title: "초보 장비 구성 추천 부탁드려요", author: "newfin", comments: 8),
        MockPost(title: "이번 주말 제주 바다 상태 공유", author: "sea-note", comments: 5),
        MockPost(title: "프리다이빙 이퀄라이징 팁", author: "slowbreath", comments: 12)
    ]
}

struct MockCertificate: Identifiable {
    let id = UUID()
    var name: String
    var agency: String
    var issuedAt: String

    static let samples = [
        MockCertificate(name: "Open Water Diver", agency: "PADI", issuedAt: "2024.04"),
        MockCertificate(name: "Advanced Open Water", agency: "PADI", issuedAt: "2025.02")
    ]
}
