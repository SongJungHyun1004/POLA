package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.util.Result

interface SearchRepository {
    suspend fun searchWithRag(query: String): Result<RagSearchResult>
}
