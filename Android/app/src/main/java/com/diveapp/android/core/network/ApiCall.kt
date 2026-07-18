package com.diveapp.android.core.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Runs a Retrofit suspend call and normalizes every failure into [ApiException],
 * parsing the backend's standardized `{"error": {code, message}}` body when present.
 */
suspend fun <T> apiCall(block: suspend () -> T): T {
    try {
        return block()
    } catch (e: HttpException) {
        if (e.code() == 401) throw ApiException.Unauthorized
        val body = e.response()?.errorBody()?.string()
        val parsed = body?.let { runCatching { errorJson.decodeFromString(ServerErrorBody.serializer(), it) }.getOrNull() }
        if (parsed != null) {
            throw ApiException.Server(e.code(), parsed.error.code, parsed.error.message)
        }
        throw ApiException.Server(e.code(), "UNKNOWN", "요청을 처리하지 못했습니다.")
    } catch (e: IOException) {
        throw ApiException.Network(e)
    } catch (e: ApiException) {
        throw e
    } catch (e: Exception) {
        throw ApiException.Decoding(e)
    }
}
