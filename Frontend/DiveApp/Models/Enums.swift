import Foundation

enum AuthProvider: String, Codable {
    case naver = "NAVER"
    case google = "GOOGLE"
}

enum UserRole: String, Codable {
    case user = "USER"
    case admin = "ADMIN"
}

enum AccountStatus: String, Codable {
    case active = "ACTIVE"
    case dormant = "DORMANT"
    case suspended = "SUSPENDED"
    case deleted = "DELETED"
}
