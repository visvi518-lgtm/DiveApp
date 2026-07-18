package com.diveapp.android.service

import com.diveapp.android.model.InformationArticleListItem
import com.diveapp.android.model.InformationArticleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InformationService {
    @GET("api/v1/information/articles")
    suspend fun list(@Query("q") query: String? = null): List<InformationArticleListItem>

    @GET("api/v1/information/articles/{id}")
    suspend fun get(@Path("id") id: String): InformationArticleResponse
}
