import Foundation

struct ProfileSetupRequest: Encodable {
    let nickname: String
    let profileImageURL: String?
    let phoneNumber: String?
}

struct UserProfileResponse: Decodable {
    let nickname: String
    let profileImageURL: String?
    let phoneNumber: String?
    let bio: String?
}

struct CurrentUser: Decodable {
    let id: UUID
    let email: String
    let provider: AuthProvider
    let role: UserRole
    let accountStatus: AccountStatus
    let profile: UserProfileResponse?
}
