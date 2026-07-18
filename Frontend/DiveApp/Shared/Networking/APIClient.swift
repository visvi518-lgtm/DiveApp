import Foundation

final class APIClient {
    private let baseURL: URL
    private let session: URLSession
    weak var tokenProvider: AccessTokenProviding?

    init(baseURL: URL = AppConfig.apiBaseURL, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.session = session
    }

    @discardableResult
    func send<T: Decodable>(_ endpoint: Endpoint) async throws -> T {
        let data = try await execute(endpoint, allowRefreshRetry: true)
        do {
            return try JSONDecoder.diveAppDefault.decode(T.self, from: data)
        } catch {
            throw APIError.decoding(error)
        }
    }

    func send(_ endpoint: Endpoint) async throws {
        _ = try await execute(endpoint, allowRefreshRetry: true)
    }

    private func execute(_ endpoint: Endpoint, allowRefreshRetry: Bool) async throws -> Data {
        var request = try buildRequest(for: endpoint)

        if endpoint.requiresAuth {
            guard let token = await tokenProvider?.currentAccessToken() else {
                throw APIError.unauthorized
            }
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        let (data, response): (Data, URLResponse)
        do {
            (data, response) = try await session.data(for: request)
        } catch {
            throw APIError.network(error)
        }

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        if httpResponse.statusCode == 401, endpoint.requiresAuth, allowRefreshRetry {
            do {
                _ = try await tokenProvider?.refreshAccessToken()
            } catch {
                await tokenProvider?.handleUnauthorized()
                throw APIError.unauthorized
            }
            return try await execute(endpoint, allowRefreshRetry: false)
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            if httpResponse.statusCode == 401 {
                await tokenProvider?.handleUnauthorized()
                throw APIError.unauthorized
            }
            if let body = try? JSONDecoder().decode(ServerErrorBody.self, from: data) {
                throw APIError.server(statusCode: httpResponse.statusCode, code: body.error.code, message: body.error.message)
            }
            throw APIError.server(statusCode: httpResponse.statusCode, code: "UNKNOWN", message: "요청을 처리하지 못했습니다.")
        }

        return data
    }

    private func buildRequest(for endpoint: Endpoint) throws -> URLRequest {
        guard var components = URLComponents(url: baseURL.appendingPathComponent(endpoint.path), resolvingAgainstBaseURL: false) else {
            throw APIError.invalidResponse
        }
        if !endpoint.queryItems.isEmpty {
            components.queryItems = endpoint.queryItems
        }
        guard let url = components.url else {
            throw APIError.invalidResponse
        }

        var request = URLRequest(url: url)
        request.httpMethod = endpoint.method.rawValue

        if let body = endpoint.body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONEncoder.diveAppDefault.encode(AnyEncodable(body))
        }

        return request
    }
}

/// Type-erasing wrapper so Endpoint can carry a heterogeneous `Encodable` body.
private struct AnyEncodable: Encodable {
    private let encodeClosure: (Encoder) throws -> Void

    init(_ wrapped: Encodable) {
        encodeClosure = wrapped.encode
    }

    func encode(to encoder: Encoder) throws {
        try encodeClosure(encoder)
    }
}
