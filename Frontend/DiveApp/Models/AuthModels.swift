import Foundation

struct SocialLoginRequest: Encodable {
    let token: String
}

struct TokenResponse: Decodable {
    let accessToken: String
    let refreshToken: String
    let tokenType: String
    let isNewUser: Bool
}

struct RefreshTokenRequest: Encodable {
    let refreshToken: String
}

struct AccessTokenResponse: Decodable {
    let accessToken: String
    let tokenType: String
}
