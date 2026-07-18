package com.diveapp.android.repository

import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.CertificateCreateRequest
import com.diveapp.android.model.CertificateResponse
import com.diveapp.android.service.CertificateService

class CertificateRepository(private val service: CertificateService) {
    suspend fun create(body: CertificateCreateRequest): CertificateResponse = apiCall { service.create(body) }

    suspend fun list(): List<CertificateResponse> = apiCall { service.list() }

    suspend fun get(id: String): CertificateResponse = apiCall { service.get(id) }

    /** Always sends the full form, not a sparse diff — see CertificateService. */
    suspend fun update(id: String, body: CertificateCreateRequest): CertificateResponse =
        apiCall { service.update(id, body) }

    suspend fun delete(id: String) {
        apiCall { service.delete(id) }
    }
}
