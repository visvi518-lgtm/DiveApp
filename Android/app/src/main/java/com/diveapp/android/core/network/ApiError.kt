package com.diveapp.android.core.network

import kotlinx.serialization.Serializable

/** Mirrors the backend's standardized error body: {"error": {"code", "message"}}. */
@Serializable
data class ServerErrorBody(val error: Detail) {
    @Serializable
    data class Detail(val code: String, val message: String)
}

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) : ApiException("네트워크 연결을 확인해주세요.", cause)

    class Decoding(cause: Throwable) : ApiException("데이터를 처리하는 중 문제가 발생했습니다.", cause)

    class Server(val statusCode: Int, val code: String, override val message: String) : ApiException(message)

    object Unauthorized : ApiException("로그인이 만료되었습니다. 다시 로그인해주세요.")
}
