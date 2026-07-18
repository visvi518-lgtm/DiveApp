import Foundation

enum AppConfig {
    /// Points at a local backend during development. Replace with the deployed
    /// Render URL via a build configuration before shipping.
    static let apiBaseURL = URL(string: "http://localhost:8000")!
}
