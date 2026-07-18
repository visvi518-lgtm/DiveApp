import Foundation

final class UserService {
    private let client: APIClient

    init(client: APIClient) {
        self.client = client
    }

    func fetchCurrentUser() async throws -> CurrentUser {
        try await client.send(Endpoint(path: "/api/v1/users/me"))
    }

    func setupProfile(nickname: String, profileImageURL: String?, phoneNumber: String?) async throws -> UserProfileResponse {
        let endpoint = Endpoint(
            path: "/api/v1/users/me/profile",
            method: .post,
            body: ProfileSetupRequest(nickname: nickname, profileImageURL: profileImageURL, phoneNumber: phoneNumber)
        )
        return try await client.send(endpoint)
    }
}
