package com.diveapp.android.service

import com.diveapp.android.model.TrainingRecordCreateRequest
import com.diveapp.android.model.TrainingRecordResponse
import com.diveapp.android.model.TrainingStatisticsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TrainingService {
    @POST("api/v1/trainings")
    suspend fun create(@Body body: TrainingRecordCreateRequest): TrainingRecordResponse

    @GET("api/v1/trainings")
    suspend fun list(): List<TrainingRecordResponse>

    @GET("api/v1/trainings/statistics")
    suspend fun statistics(): TrainingStatisticsResponse
}
