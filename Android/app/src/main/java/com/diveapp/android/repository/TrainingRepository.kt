package com.diveapp.android.repository

import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.TrainingRecordCreateRequest
import com.diveapp.android.model.TrainingRecordResponse
import com.diveapp.android.model.TrainingStatisticsResponse
import com.diveapp.android.service.TrainingService

class TrainingRepository(private val service: TrainingService) {
    suspend fun create(body: TrainingRecordCreateRequest): TrainingRecordResponse = apiCall { service.create(body) }

    suspend fun list(): List<TrainingRecordResponse> = apiCall { service.list() }

    suspend fun statistics(): TrainingStatisticsResponse = apiCall { service.statistics() }
}
