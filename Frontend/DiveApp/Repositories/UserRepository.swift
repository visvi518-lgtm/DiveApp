import Foundation

final class UserRepository {
    private let userService: UserService

    init(userService: UserService) {
        self.userService = userService
    }

    func fetchCurrentUser() async throws -> CurrentUser {
        try await userService.fetchCurrentUser()
    }

    func setupProfile(nickname: String, profileImageURL: String?, phoneNumber: String?) async throws -> UserProfileResponse {
        try await userService.setupProfile(nickname: nickname, profileImageURL: profileImageURL, phoneNumber: phoneNumber)
    }
}
