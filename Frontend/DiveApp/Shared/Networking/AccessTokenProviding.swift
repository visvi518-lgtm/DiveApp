import Foundation

/// Lets APIClient attach auth headers and recover from expired tokens without
/// depending on AuthSession directly (AuthSession depends on the services that
/// depend on APIClient, so the reverse dependency would be circular).
@MainActor
protocol AccessTokenProviding: AnyObject {
    func currentAccessToken() -> String?
    func refreshAccessToken() async throws -> String
    func handleUnauthorized() async
}
