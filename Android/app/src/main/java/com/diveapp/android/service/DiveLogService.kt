package com.diveapp.android.service

import com.diveapp.android.model.DiveLogCreateRequest
import com.diveapp.android.model.DiveLogListItem
import com.diveapp.android.model.DiveLogResponse
import com.diveapp.android.model.DiveLogStatisticsResponse
import com.diveapp.android.model.DiveLogUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DiveLogService {
    @POST("api/v1/dive-logs")
    suspend fun create(@Body body: DiveLogCreateRequest): DiveLogResponse

    @GET("api/v1/dive-logs")
    suspend fun list(): List<DiveLogListItem>

    @GET("api/v1/dive-logs/statistics")
    suspend fun statistics(): DiveLogStatisticsResponse

    @GET("api/v1/dive-logs/{id}")
    suspend fun get(@Path("id") id: String): DiveLogResponse

    @PATCH("api/v1/dive-logs/{id}")
    suspend fun update(@Path("id") id: String, @Body body: DiveLogUpdateRequest): DiveLogResponse

    @DELETE("api/v1/dive-logs/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
