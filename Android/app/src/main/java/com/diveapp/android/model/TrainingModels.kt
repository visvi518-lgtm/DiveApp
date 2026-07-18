package com.diveapp.android.model

import com.diveapp.android.core.network.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class TrainingRecordCreateRequest(
    @SerialName("total_sets") val totalSets: Int,
    @SerialName("completed_sets") val completedSets: Int,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("rest_time_seconds") val restTimeSeconds: Int,
    @SerialName("hold_time_seconds") val holdTimeSeconds: Int,
    @SerialName("rest_interval_seconds") val restIntervalSeconds: Int,
    @SerialName("hold_interval_seconds") val holdIntervalSeconds: Int,
)

@Serializable
data class TrainingRecordResponse(
    val id: String,
    @SerialName("total_sets") val totalSets: Int,
    @SerialName("completed_sets") val completedSets: Int,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("rest_time_seconds") val restTimeSeconds: Int,
    @SerialName("hold_time_seconds") val holdTimeSeconds: Int,
    @SerialName("rest_interval_seconds") val restIntervalSeconds: Int,
    @SerialName("hold_interval_seconds") val holdIntervalSeconds: Int,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("completed_at") val completedAt: OffsetDateTime,
)

@Serializable
data class TrainingStatisticsResponse(
    @SerialName("total_training_count") val totalTrainingCount: Int,
    @SerialName("completion_rate") val completionRate: Double,
    @SerialName("average_completed_sets") val averageCompletedSets: Double,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("last_training_at") val lastTrainingAt: OffsetDateTime?,
)
