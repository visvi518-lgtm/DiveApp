import Foundation

@MainActor
final class ProfileSetupViewModel: ObservableObject {
    @Published var nickname = ""
    @Published var isSaving = false
    @Published var errorMessage: String?

    private let authSession: AuthSession
    private let userRepository: UserRepository

    init(authSession: AuthSession, userRepository: UserRepository) {
        self.authSession = authSession
        self.userRepository = userRepository
    }

    var isValid: Bool {
        (2...30).contains(nickname.count)
    }

    func save() async {
        guard isValid else {
            errorMessage = "닉네임은 2자 이상 30자 이하로 입력해주세요."
            return
        }

        errorMessage = nil
        isSaving = true
        defer { isSaving = false }

        do {
            _ = try await userRepository.setupProfile(nickname: nickname, profileImageURL: nil, phoneNumber: nil)
            await authSession.completeProfileSetup()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
