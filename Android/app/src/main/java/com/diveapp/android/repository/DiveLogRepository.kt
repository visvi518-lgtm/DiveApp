package com.diveapp.android.repository

import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.DiveLogCreateRequest
import com.diveapp.android.model.DiveLogListItem
import com.diveapp.android.model.DiveLogResponse
import com.diveapp.android.model.DiveLogStatisticsResponse
import com.diveapp.android.model.DiveLogUpdateRequest
import com.diveapp.android.service.DiveLogService

class DiveLogRepository(private val service: DiveLogService) {
    suspend fun create(body: DiveLogCreateRequest): DiveLogResponse = apiCall { service.create(body) }

    suspend fun list(): List<DiveLogListItem> = apiCall { service.list() }

    suspend fun statistics(): DiveLogStatisticsResponse = apiCall { service.statistics() }

    suspend fun get(id: String): DiveLogResponse = apiCall { service.get(id) }

    suspend fun update(id: String, body: DiveLogUpdateRequest): DiveLogResponse = apiCall { service.update(id, body) }

    suspend fun delete(id: String) {
        apiCall { service.delete(id) }
    }
}
