package com.diveapp.android.service

import com.diveapp.android.model.CertificateCreateRequest
import com.diveapp.android.model.CertificateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CertificateService {
    @POST("api/v1/certificates")
    suspend fun create(@Body body: CertificateCreateRequest): CertificateResponse

    @GET("api/v1/certificates")
    suspend fun list(): List<CertificateResponse>

    @GET("api/v1/certificates/{id}")
    suspend fun get(@Path("id") id: String): CertificateResponse

    /** The backend accepts a partial update, but this app always submits the
     * full form (see CertificateRepository), so the create-shaped body is
     * reused rather than duplicating an almost-identical class. */
    @PATCH("api/v1/certificates/{id}")
    suspend fun update(@Path("id") id: String, @Body body: CertificateCreateRequest): CertificateResponse

    @DELETE("api/v1/certificates/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
