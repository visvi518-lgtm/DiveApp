import Foundation

enum APIError: Error, LocalizedError {
    case invalidResponse
    case network(Error)
    case decoding(Error)
    case server(statusCode: Int, code: String, message: String)
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "서버 응답을 처리할 수 없습니다."
        case .network:
            return "네트워크 연결을 확인해주세요."
        case .decoding:
            return "데이터를 처리하는 중 문제가 발생했습니다."
        case .server(_, _, let message):
            return message
        case .unauthorized:
            return "로그인이 만료되었습니다. 다시 로그인해주세요."
        }
    }
}

/// Mirrors the backend's standardized error body: {"error": {"code", "message"}}.
struct ServerErrorBody: Decodable {
    struct Detail: Decodable {
        let code: String
        let message: String
    }

    let error: Detail
}
