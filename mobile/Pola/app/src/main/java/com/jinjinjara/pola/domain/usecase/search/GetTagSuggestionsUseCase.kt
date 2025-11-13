package com.jinjinjara.pola.domain.usecase.search

import com.jinjinjara.pola.domain.repository.SearchRepository
import com.jinjinjara.pola.domain.usecase.BaseUseCase
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetTagSuggestionsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : BaseUseCase<String, Result<List<String>>> {

    override suspend fun invoke(params: String): Result<List<String>> {
        return searchRepository.getTagSuggestions(params)
    }
}