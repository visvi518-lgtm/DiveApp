import Foundation

/// Persists the access/refresh token pair in the Keychain across app launches.
final class TokenStorage {
    private enum Key {
        static let accessToken = "com.diveapp.accessToken"
        static let refreshToken = "com.diveapp.refreshToken"
    }

    var accessToken: String? {
        get { KeychainHelper.read(forKey: Key.accessToken) }
        set {
            if let newValue {
                KeychainHelper.save(newValue, forKey: Key.accessToken)
            } else {
                KeychainHelper.delete(forKey: Key.accessToken)
            }
        }
    }

    var refreshToken: String? {
        get { KeychainHelper.read(forKey: Key.refreshToken) }
        set {
            if let newValue {
                KeychainHelper.save(newValue, forKey: Key.refreshToken)
            } else {
                KeychainHelper.delete(forKey: Key.refreshToken)
            }
        }
    }

    func save(accessToken: String, refreshToken: String) {
        self.accessToken = accessToken
        self.refreshToken = refreshToken
    }

    func clear() {
        accessToken = nil
        refreshToken = nil
    }
}
