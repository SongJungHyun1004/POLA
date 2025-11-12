package com.jinjinjara.pola.domain.usecase.search

import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.domain.repository.SearchRepository
import com.jinjinjara.pola.domain.usecase.BaseUseCase
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class SearchWithRagUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : BaseUseCase<String, Result<RagSearchResult>> {

    override suspend fun invoke(params: String): Result<RagSearchResult> {
        return searchRepository.searchWithRag(params)
    }
}
