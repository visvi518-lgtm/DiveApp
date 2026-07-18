package com.diveapp.android.repository

import com.diveapp.android.core.network.apiCall
import com.diveapp.android.model.InformationArticleListItem
import com.diveapp.android.model.InformationArticleResponse
import com.diveapp.android.service.InformationService

class InformationRepository(private val service: InformationService) {
    suspend fun list(query: String? = null): List<InformationArticleListItem> = apiCall { service.list(query) }

    suspend fun get(id: String): InformationArticleResponse = apiCall { service.get(id) }
}
