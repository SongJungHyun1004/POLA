package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.domain.model.TagSearchFile
import com.jinjinjara.pola.util.Result

interface SearchRepository {
    suspend fun searchWithRag(query: String): Result<RagSearchResult>
    suspend fun getTagSuggestions(keyword: String): Result<List<String>>
    suspend fun searchFilesByTag(tag: String): Result<List<TagSearchFile>>
    suspend fun searchAll(keyword: String): Result<List<TagSearchFile>>
}
